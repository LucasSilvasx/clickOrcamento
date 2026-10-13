package com.clickorcamento.app.domain.model

/**
 * Quem presta o serviço (a "empresa" nas telas). Um usuário pode ter várias
 * (ex.: Marcenaria e Serralheria); exatamente uma é a padrão.
 *
 * É o model de DOMÍNIO: sem anotação de framework. O formato que viaja para o
 * Supabase (nomes de coluna em snake_case) fica em data/remote/dto/PrestadorDto.kt.
 * Assim, se o banco mudar, só o DTO e o repository sentem.
 *
 * Os campos "padrão" são as condições que serão COPIADAS para cada orçamento novo.
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
