package com.clickorcamento.app.feature.configuracoes

import com.clickorcamento.app.core.format.limparCpfCnpj
import com.clickorcamento.app.core.format.limparTelefone
import com.clickorcamento.app.domain.model.DadosPrestador
import com.clickorcamento.app.domain.model.Prestador

/**
 * Estado do formulário de empresa (criar e editar). Todos os campos são texto
 * puro, como o usuário digita; a conversão para o domínio acontece em
 * [paraDados]. Vive dentro do [ConfiguracoesUiState]: `null` lá = formulário fechado.
 */
data class FormularioEmpresa(
    /** `null` = empresa nova; preenchido = editando essa empresa. */
    val id: String? = null,
    val apelido: String = "",
    val nomeRazaoSocial: String = "",
    val cpfCnpj: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val email: String = "",
    val formaPagamento: String = "",
    val prazoPagamento: String = "",
    val observacoes: String = "",
    val salvando: Boolean = false,
    val erro: String? = null
) {
    val editando: Boolean
        get() = id != null

    val titulo: String
        get() = if (editando) "Editar empresa" else "Nova empresa"

    /** Só apelido e nome/razão social são obrigatórios. */
    val podeSalvar: Boolean
        get() = !salvando && apelido.isNotBlank() && nomeRazaoSocial.isNotBlank()

    /** Tira espaços das pontas e transforma campo opcional em branco em `null`. */
    fun paraDados() = DadosPrestador(
        apelido = apelido.trim(),
        nomeRazaoSocial = nomeRazaoSocial.trim(),
        cpfCnpj = cpfCnpj.opcional(),
        endereco = endereco.opcional(),
        telefone = telefone.opcional(),
        email = email.opcional(),
        formaPagamentoPadrao = formaPagamento.opcional(),
        prazoPagamentoPadrao = prazoPagamento.opcional(),
        observacoesPadrao = observacoes.opcional()
    )

    companion object {
        fun de(empresa: Prestador) = FormularioEmpresa(
            id = empresa.id,
            apelido = empresa.apelido,
            nomeRazaoSocial = empresa.nomeRazaoSocial,
            // Só o miolo (sem pontuação): a máscara do campo desenha o resto.
            cpfCnpj = limparCpfCnpj(empresa.cpfCnpj.orEmpty()),
            endereco = empresa.endereco.orEmpty(),
            telefone = limparTelefone(empresa.telefone.orEmpty()),
            email = empresa.email.orEmpty(),
            formaPagamento = empresa.formaPagamentoPadrao.orEmpty(),
            prazoPagamento = empresa.prazoPagamentoPadrao.orEmpty(),
            observacoes = empresa.observacoesPadrao.orEmpty()
        )
    }
}

private fun String.opcional(): String? = trim().ifEmpty { null }
