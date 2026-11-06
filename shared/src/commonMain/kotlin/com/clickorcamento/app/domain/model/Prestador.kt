package com.clickorcamento.app.domain.model

/**
 * Quem presta o serviço (a "empresa" nas telas). Um usuário pode ter várias
 * (ex.: Marcenaria e Serralheria); exatamente uma é a padrão.
 *
 * Os campos "padrão" são as condições copiadas para cada orçamento novo.
 */
data class Prestador(
    val id: String,
    val apelido: String,
    val nomeRazaoSocial: String,
    val cpfCnpj: String?,
    val endereco: String?,
    val telefone: String?,
    val email: String?,
    val formaPagamentoPadrao: String?,
    val prazoPagamentoPadrao: String?,
    val observacoesPadrao: String?,
    val padrao: Boolean
)
