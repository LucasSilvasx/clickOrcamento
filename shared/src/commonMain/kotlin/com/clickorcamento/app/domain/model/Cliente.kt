package com.clickorcamento.app.domain.model

/**
 * Quem recebe o orçamento. Todas as colunas de texto são `NOT NULL DEFAULT ''`
 * no banco, então aqui também não há `null`: campo em branco = `""`.
 */
data class Cliente(
    val id: String,
    val razaoSocial: String,
    val cnpj: String,
    val endereco: String,
    /** Inscrição estadual. Texto livre: o formato muda por estado e pode ser "ISENTO". */
    val inscricaoEstadual: String,
    val telefone: String,
    val email: String
)

/** O que o usuário digita ao criar ou editar um cliente (o id nasce no banco). */
data class DadosCliente(
    val razaoSocial: String,
    val cnpj: String,
    val endereco: String,
    /** Inscrição estadual. Texto livre: o formato muda por estado e pode ser "ISENTO". */
    val inscricaoEstadual: String,
    val telefone: String,
    val email: String
)
