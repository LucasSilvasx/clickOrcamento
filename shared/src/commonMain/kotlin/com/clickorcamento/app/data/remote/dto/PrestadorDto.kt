package com.clickorcamento.app.data.remote.dto

import com.clickorcamento.app.domain.model.DadosPrestador
import com.clickorcamento.app.domain.model.Prestador
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

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

/**
 * Corpo do INSERT/UPDATE na tabela `prestador`.
 *
 * Montado como `JsonObject` (e não como `@Serializable`) de propósito: num
 * UPDATE, campo apagado pelo usuário precisa ir explícito no corpo para
 * limpar a coluna. Propriedade nula de classe pode ser omitida pelo
 * serializador (e aí o valor antigo ficaria no banco); num JsonObject, o
 * campo sempre vai.
 *
 * Campo em branco vai como texto vazio (`""`), NÃO como `null`: no banco todas
 * as colunas de texto opcionais são `NOT NULL DEFAULT ''`. Mandar `null`
 * dá erro 23502 (not-null violation). No domínio continua `null` = "em branco".
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
