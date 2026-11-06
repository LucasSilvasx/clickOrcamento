package com.clickorcamento.app.data.remote.dto

import com.clickorcamento.app.domain.model.DadosPrestador
import com.clickorcamento.app.domain.model.Prestador
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Espelho da tabela `prestador`. Colunas não declaradas são ignoradas na leitura. */
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

/**
 * Corpo do INSERT/UPDATE na tabela `prestador`.
 *
 * `JsonObject` de propósito: num UPDATE, campo apagado precisa ir explícito para limpar a
 * coluna, e propriedade nula de classe `@Serializable` pode ser omitida pelo serializador.
 *
 * Campo em branco vai como `""`, não como `null`: as colunas de texto opcionais são
 * `NOT NULL DEFAULT ''` e `null` daria erro 23502. No domínio continua `null` = em branco.
 *
 * `padrao` só entra no corpo quando informado (criação). Na edição fica de fora.
 */
fun DadosPrestador.paraJson(padrao: Boolean? = null): JsonObject = buildJsonObject {
    put("apelido", apelido)
    put("nome_razao_social", nomeRazaoSocial)
    put("cpf_cnpj", cpfCnpj.orEmpty())
    put("endereco", endereco.orEmpty())
    put("telefone", telefone.orEmpty())
    put("email", email.orEmpty())
    put("forma_pagamento_padrao", formaPagamentoPadrao.orEmpty())
    put("prazo_pagamento_padrao", prazoPagamentoPadrao.orEmpty())
    put("observacoes_padrao", observacoesPadrao.orEmpty())
    if (padrao != null) put("padrao", padrao)
}
