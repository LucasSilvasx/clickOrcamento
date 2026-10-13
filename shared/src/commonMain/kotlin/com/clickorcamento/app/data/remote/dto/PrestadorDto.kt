package com.clickorcamento.app.data.remote.dto

import com.clickorcamento.app.domain.model.Prestador
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Espelho da tabela `prestador`. O @SerialName liga o nome da coluna no banco
 * (snake_case) ao nome da propriedade em Kotlin (camelCase).
 *
 * Colunas que não declaramos (usuario_id, created_at, updated_at) são
 * ignoradas na leitura — o supabase-kt já configura o JSON para isso.
 */
@Serializable
data class PrestadorDto(
    val id: String,
    val apelido: String,
    @SerialName("nome_razao_social") val nomeRazaoSocial: String,
    @SerialName("cpf_cnpj") val cpfCnpj: String? = null,
    val endereco: String? = null,
    val telefone: String? = null,
    val email: String? = null,
    @SerialName("forma_pagamento_padrao") val formaPagamentoPadrao: String? = null,
    @SerialName("prazo_pagamento_padrao") val prazoPagamentoPadrao: String? = null,
    @SerialName("observacoes_padrao") val observacoesPadrao: String? = null,
    val padrao: Boolean = false
) {
    fun paraDominio() = Prestador(
        id = id,
        apelido = apelido,
        nomeRazaoSocial = nomeRazaoSocial,
        cpfCnpj = cpfCnpj,
        endereco = endereco,
        telefone = telefone,
        email = email,
        formaPagamentoPadrao = formaPagamentoPadrao,
        prazoPagamentoPadrao = prazoPagamentoPadrao,
        observacoesPadrao = observacoesPadrao,
        padrao = padrao
    )
}
