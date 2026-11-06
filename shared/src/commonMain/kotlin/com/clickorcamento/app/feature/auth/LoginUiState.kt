package com.clickorcamento.app.feature.auth

enum class ModoAuth { ENTRAR, CADASTRAR }

data class LoginUiState(
    val modo: ModoAuth = ModoAuth.ENTRAR,
    val email: String = "",
    val senha: String = "",
    val confirmacaoSenha: String = "",
    val senhaVisivel: Boolean = false,
    /** Só vale no cadastro: quem já tem conta aceitou ao criá-la. */
    val aceitouTermos: Boolean = false,
    val carregando: Boolean = false,
    val erro: String? = null,
    /** Mensagem de sucesso do cadastro (pedindo confirmação por e-mail) */
    val aviso: String? = null,
    /**
     * Segunda etapa do cadastro: o Supabase mandou um link de confirmação por
     * e-mail e a tela passa a orientar o usuário em vez do formulário normal.
     */
    val aguardandoConfirmacao: Boolean = false
) {
    private val emailValido: Boolean
        get() = email.contains("@") && email.contains(".") && email.length > 5

    private val senhaValida: Boolean
        get() = senha.length >= 6

    private val confirmacaoValida: Boolean
        get() = modo == ModoAuth.ENTRAR || confirmacaoSenha == senha

    private val termosValidos: Boolean
        get() = modo == ModoAuth.ENTRAR || aceitouTermos

    val podeEnviar: Boolean
        get() = !carregando && emailValido && senhaValida && confirmacaoValida && termosValidos

    val textoBotao: String
        get() = if (modo == ModoAuth.ENTRAR) "Entrar" else "Criar conta"
}