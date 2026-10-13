package com.clickorcamento.app.feature.cliente

import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosCliente

/**
 * Estado do formulário de cliente (criar e editar). Campos em texto puro, como
 * o usuário digita. Vive dentro do [ClientesUiState]: `null` lá = formulário fechado.
 * O editor de orçamento reaproveita esta classe no "Novo cliente" rápido.
 */
data class FormularioCliente(
    /** `null` = cliente novo; preenchido = editando esse cliente. */
    val id: String? = null,
    val razaoSocial: String = "",
    val cnpj: String = "",
    val contato: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val email: String = "",
    val salvando: Boolean = false,
    val erro: String? = null
) {
    val editando: Boolean
        get() = id != null

    val titulo: String
        get() = if (editando) "Editar cliente" else "Novo cliente"

    /** Só a razão social é obrigatória (docs/UI-UX.md, seção 5.4). */
    val podeSalvar: Boolean
        get() = !salvando && razaoSocial.isNotBlank()

    /** Tira espaços das pontas. Campo em branco segue como "" (as colunas são NOT NULL DEFAULT ''). */
    fun paraDados() = DadosCliente(
        razaoSocial = razaoSocial.trim(),
        cnpj = cnpj.trim(),
        endereco = endereco.trim(),
        contato = contato.trim(),
        telefone = telefone.trim(),
        email = email.trim()
    )

    companion object {
        fun de(cliente: Cliente) = FormularioCliente(
            id = cliente.id,
            razaoSocial = cliente.razaoSocial,
            cnpj = cliente.cnpj,
            contato = cliente.contato,
            endereco = cliente.endereco,
            telefone = cliente.telefone,
            email = cliente.email
        )
    }
}
