package com.clickorcamento.app.data.remote.dto

import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosCliente
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Espelho da tabela `cliente`. Colunas de texto são NOT NULL DEFAULT '' no banco. */
@Serializable
data class ClienteDto(
    val id: String,
    @SerialName("razao_social") val razaoSocial: String = "",
    val cnpj: String = "",
    val endereco: String = "",
    @SerialName("inscricao_estadual") val inscricaoEstadual: String = "",
    val telefone: String = "",
    val email: String = ""
) {
    fun paraDominio() = Cliente(
        id = id,
        razaoSocial = razaoSocial,
        cnpj = cnpj,
        endereco = endereco,
        inscricaoEstadual = inscricaoEstadual,
        telefone = telefone,
        email = email
    )
}

/**
 * Corpo do INSERT/UPDATE. `JsonObject` pelo mesmo motivo do prestador: campo
 * apagado precisa ir explícito (como `""`) para limpar a coluna.
 */
fun DadosCliente.paraJson(): JsonObject = buildJsonObject {
    put("razao_social", razaoSocial)
    put("cnpj", cnpj)
    put("endereco", endereco)
    put("inscricao_estadual", inscricaoEstadual)
    put("telefone", telefone)
    put("email", email)
}
