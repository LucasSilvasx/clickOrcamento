package com.clickorcamento.app.domain.model

/**
 * Os campos que o usuário digita ao criar ou editar uma empresa.
 *
 * É um "Prestador sem id e sem `padrao`": o id nasce no banco e o `padrao`
 * não se edita no formulário (tem ação própria: "Definir como padrão").
 * Campos opcionais em branco chegam aqui como `null`.
 */
data class DadosPrestador(
    val apelido: String,
    val nomeRazaoSocial: String,
    val cpfCnpj: String?,
    val endereco: String?,
    val telefone: String?,
    val email: String?,
    val formaPagamentoPadrao: String?,
    val prazoPagamentoPadrao: String?,
    val observacoesPadrao: String?
)
