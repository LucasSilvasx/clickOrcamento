package com.clickorcamento.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.data.remote.supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O cliente vem por parâmetro, com valor padrão apontando para a
 * instância global. Quando o Koin entrar, o padrão sai e a injeção
 * passa a ser explícita — e aí dá pra testar com um cliente falso.
 */
class LoginViewModel(
    private val cliente: SupabaseClient = supabase
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

    fun alternarModo() = _uiState.update {
        it.copy(
            modo = if (it.modo == ModoAuth.ENTRAR) ModoAuth.CADASTRAR else ModoAuth.ENTRAR,
            confirmacaoSenha = "",
            erro = null,
            aviso = null
        )
    }

    fun enviar() {
        val estado = _uiState.value
        if (!estado.podeEnviar) return

        _uiState.update { it.copy(carregando = true, erro = null, aviso = null) }

        // viewModelScope cancela sozinho se a tela for destruída —
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
                    }
                    "Conta criada. Confirme o e-mail que enviamos e depois entre."
                }
            }.onSuccess { mensagem ->
                _uiState.update {
                    it.copy(
                        carregando = false,
                        aviso = mensagem,
                        // Volta pro modo entrar depois do cadastro
                        modo = if (mensagem != null) ModoAuth.ENTRAR else it.modo,
                        senha = if (mensagem != null) "" else it.senha,
                        confirmacaoSenha = ""
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(carregando = false, erro = traduzirErro(e))
                }
            }
        }
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
            original.contains("rate limit", true) ->
                "Muitas tentativas. Aguarde um pouco."
            else -> "Não foi possível conectar. $original"
        }
    }
}