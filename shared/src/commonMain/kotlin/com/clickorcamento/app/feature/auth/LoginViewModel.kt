package com.clickorcamento.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.core.legal.DocumentosLegais
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * O cliente é injetado pelo Koin (ver di/AppModule.kt). Como ele chega
 * pelo construtor, dá pra testar este ViewModel com um cliente falso.
 */
class LoginViewModel(
    private val cliente: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Ao digitar, limpamos erro e aviso: mensagem antiga ao lado de
    // um campo que o usuário já corrigiu é confuso.
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

        // viewModelScope cancela sozinho se a tela for destruída, o que
        // evita atualizar estado de algo que não existe mais.
        viewModelScope.launch {
            runCatching {
                if (estado.modo == ModoAuth.ENTRAR) {
                    cliente.auth.signInWith(Email) {
                        email = estado.email
                        password = estado.senha
                    }
                    null  // sucesso no login não gera aviso: a tela troca sozinha
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
                _uiState.update {
                    it.copy(
                        carregando = false,
                        aviso = mensagem,
                        // Cadastro feito: a tela passa a orientar a confirmação por e-mail.
                        // Voltamos o modo para ENTRAR para que, se o usuário
                        // desistir da confirmação, caia no login normal.
                        aguardandoConfirmacao = mensagem != null,
                        modo = if (mensagem != null) ModoAuth.ENTRAR else it.modo,
                        // A senha fica na memória durante a etapa de confirmação:
                        // é ela que usamos no "Já confirmei, entrar". Sai em voltarParaLogin().
                        confirmacaoSenha = ""
                    )
                }
            }.onFailure { e ->
                // Conta criada antes, mas nunca confirmada: em vez de só dar
                // erro, levamos o usuário para a etapa de confirmação e pedimos um novo e-mail.
                if (e.message.orEmpty().contains("Email not confirmed", true)) {
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
            runCatching {
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
            runCatching {
                cliente.auth.signInWith(Email) {
                    email = estado.email
                    password = estado.senha
                }
            }.onSuccess {
                _uiState.update { it.copy(carregando = false) }
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
     * O Supabase devolve mensagens em inglês e técnicas.
     * Traduzimos as mais comuns; o resto passa direto pra não
     * esconder informação de diagnóstico.
     */
    private fun traduzirErro(e: Throwable): String {
        val original = e.message.orEmpty()
        return when {
            original.contains("Invalid login credentials", true) ->
                "E-mail ou senha incorretos."
            original.contains("Email not confirmed", true) ->
                "E-mail ainda não confirmado. Verifique sua caixa de entrada."
            original.contains("User already registered", true) ->
                "Já existe uma conta com esse e-mail."
            original.contains("weak", true) || original.contains("password", true) ->
                "Senha muito fraca. Use pelo menos 6 caracteres."
            original.contains("rate limit", true) ||
                original.contains("only request this after", true) ->
                "Muitas tentativas. Aguarde cerca de um minuto."
            original.contains("expired", true) || original.contains("invalid", true) ->
                "Dados inválidos ou expirados. Confira o e-mail e tente novamente."
            else -> "Não foi possível conectar. $original"
        }
    }
}