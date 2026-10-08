package com.clickorcamento.app.feature.auth

enum class ModoAuth { ENTRAR, CADASTRAR }

/**
 * Todo o estado da tela num objeto só. Com estado espalhado em vários
 * StateFlow, a tela pode recompor num instante onde "email já mudou mas
 * erro ainda não" — estado inconsistente. Um objeto único sempre
 * representa um momento coerente.
 */
data class LoginUiState(
    val modo: ModoAuth = ModoAuth.ENTRAR,
    val email: String = "",
    val senha: String = "",
    val confirmacaoSenha: String = "",
    val senhaVisivel: Boolean = false,
    val carregando: Boolean = false,
    val erro: String? = null,
    /** Mensagem de sucesso do cadastro (pedindo confirmação por e-mail) */
    val aviso: String? = null
) {
    // Validação mora aqui, não na tela. A tela só lê o resultado.
    private val emailValido: Boolean
        get() = email.contains("@") && email.contains(".") && email.length > 5

    private val senhaValida: Boolean
        get() = senha.length >= 6

    private val confirmacaoValida: Boolean
        get() = modo == ModoAuth.ENTRAR || confirmacaoSenha == senha

    /** Regra do botão principal. Fica aqui pra não duplicar na UI. */
    val podeEnviar: Boolean
        get() = !carregando && emailValido && senhaValida && confirmacaoValida

    val textoBotao: String
        get() = if (modo == ModoAuth.ENTRAR) "Entrar" else "Criar conta"
}