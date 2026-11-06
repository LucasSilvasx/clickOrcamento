package com.clickorcamento.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.core.erro.tentar
import com.clickorcamento.app.core.legal.DocumentosLegais
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LoginViewModel(
    private val cliente: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Ao digitar, limpa erro e aviso: já não descrevem o que está no campo.
    fun aoMudarEmail(valor: String) =
        _uiState.update { it.copy(email = valor.trim(), erro = null, aviso = null) }

    fun aoMudarSenha(valor: String) =
        _uiState.update { it.copy(senha = valor, erro = null, aviso = null) }

    fun aoMudarConfirmacao(valor: String) =
        _uiState.update { it.copy(confirmacaoSenha = valor, erro = null) }

    fun alternarVisibilidadeSenha() =
        _uiState.update { it.copy(senhaVisivel = !it.senhaVisivel) }

    fun aoMudarAceiteTermos(aceitou: Boolean) =
        _uiState.update { it.copy(aceitouTermos = aceitou, erro = null) }

    fun alternarModo() = _uiState.update {
        it.copy(
            modo = if (it.modo == ModoAuth.ENTRAR) ModoAuth.CADASTRAR else ModoAuth.ENTRAR,
            confirmacaoSenha = "",
            aceitouTermos = false,
            erro = null,
            aviso = null
        )
    }

    fun enviar() {
        val estado = _uiState.value
        if (!estado.podeEnviar) return

        _uiState.update { it.copy(carregando = true, erro = null, aviso = null) }

        viewModelScope.launch {
            tentar {
                if (estado.modo == ModoAuth.ENTRAR) {
                    cliente.auth.signInWith(Email) {
                        email = estado.email
                        password = estado.senha
                    }
                    null // sucesso no login não gera aviso: a tela troca sozinha
                } else {
                    cliente.auth.signUpWith(Email) {
                        email = estado.email
                        password = estado.senha
                        // Registra qual texto foi aceito; a data é a do próprio cadastro.
                        data = buildJsonObject { put("termos_versao", DocumentosLegais.VERSAO) }
                    }
                    "Enviamos um e-mail de confirmação para ${estado.email}."
                }
            }.onSuccess { mensagem ->
                if (mensagem == null) {
                    limparFormulario()
                } else {
                    _uiState.update {
                        it.copy(
                            carregando = false,
                            aviso = mensagem,
                            aguardandoConfirmacao = true,
                            // Quem desistir da confirmação cai no login normal.
                            modo = ModoAuth.ENTRAR,
                            // A senha fica na memória durante a etapa de confirmação: é a usada
                            // no "Já confirmei, entrar". Sai em voltarParaLogin().
                            confirmacaoSenha = ""
                        )
                    }
                }
            }.onFailure { e ->
                // Conta criada antes e nunca confirmada: em vez de só dar erro, leva para a
                // etapa de confirmação e pede um novo e-mail.
                if ((e as? AuthRestException)?.errorCode == AuthErrorCode.EmailNotConfirmed) {
                    _uiState.update {
                        it.copy(carregando = false, aguardandoConfirmacao = true)
                    }
                    reenviarConfirmacao()
                } else {
                    _uiState.update {
                        it.copy(carregando = false, erro = traduzirErro(e))
                    }
                }
            }
        }
    }

    fun reenviarConfirmacao() {
        val email = _uiState.value.email
        _uiState.update { it.copy(carregando = true, erro = null, aviso = null) }

        viewModelScope.launch {
            tentar {
                cliente.auth.resendEmail(OtpType.Email.SIGNUP, email)
            }.onSuccess {
                _uiState.update {
                    it.copy(carregando = false, aviso = "Enviamos um novo e-mail de confirmação para $email.")
                }
            }.onFailure { e ->
                _uiState.update { it.copy(carregando = false, erro = traduzirErro(e)) }
            }
        }
    }

    /**
     * Caminho sem domínio/SMTP próprio: o link do e-mail confirma a conta no
     * Supabase (mesmo que a página final dê erro), então basta entrar de novo
     * com o e-mail e a senha que o usuário já digitou no cadastro.
     */
    fun entrarAposConfirmar() {
        val estado = _uiState.value
        if (estado.carregando || estado.senha.length < 6) return

        _uiState.update { it.copy(carregando = true, erro = null, aviso = null) }

        viewModelScope.launch {
            tentar {
                cliente.auth.signInWith(Email) {
                    email = estado.email
                    password = estado.senha
                }
            }.onSuccess {
                limparFormulario()
            }.onFailure { e ->
                _uiState.update { it.copy(carregando = false, erro = traduzirErro(e)) }
            }
        }
    }

    /** Desiste da etapa de confirmação e volta para o formulário de login. */
    fun voltarParaLogin() = _uiState.update {
        it.copy(aguardandoConfirmacao = false, senha = "", erro = null, aviso = null)
    }

    /**
     * Este ViewModel sobrevive ao logout, porque fica na raiz do app. Sem zerar o estado depois
     * de entrar, a senha continuaria preenchida para quem usar o aparelho em seguida.
     */
    private fun limparFormulario() {
        _uiState.value = LoginUiState()
    }

    /**
     * Decide pelo código de erro e não pelo texto da exceção: o texto traz também a URL da
     * requisição, e a URL do login contém "password".
     */
    private fun traduzirErro(e: Throwable): String = when ((e as? AuthRestException)?.errorCode) {
        AuthErrorCode.InvalidCredentials -> "E-mail ou senha incorretos."
        AuthErrorCode.EmailNotConfirmed -> "E-mail ainda não confirmado. Verifique sua caixa de entrada."
        AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> "Já existe uma conta com esse e-mail."
        AuthErrorCode.WeakPassword -> "Senha muito fraca. Use pelo menos 6 caracteres."
        AuthErrorCode.EmailAddressInvalid -> "E-mail inválido. Confira o endereço digitado."
        AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit ->
            "Muitas tentativas. Aguarde alguns minutos e tente de novo."
        else ->
            if (e is RestException) "O servidor não conseguiu atender agora. Tente de novo em instantes."
            else "Não foi possível conectar. Verifique a conexão e tente de novo."
    }
}