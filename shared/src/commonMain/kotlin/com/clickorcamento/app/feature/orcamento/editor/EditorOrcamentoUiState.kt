package com.clickorcamento.app.feature.orcamento.editor

import com.clickorcamento.app.core.format.lerCentavos
import com.clickorcamento.app.core.format.lerQuantidade
import com.clickorcamento.app.core.format.totalDaLinha
import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.Prestador
import com.clickorcamento.app.domain.model.StatusOrcamento
import com.clickorcamento.app.feature.cliente.FormularioCliente

/**
 * Uma linha da tabela de itens, com os campos em texto puro (como o usuário
 * digita). `chave` identifica a linha na tela (não é o id do banco): é o que
 * deixa o Compose acompanhar a linha certa quando outra é removida.
 */
data class ItemForm(
    val chave: Long,
    val descricao: String = "",
    val quantidade: String = "1",
    val unidade: String = "un",
    val valor: String = ""
) {
    /** Total da linha, sempre calculado. Texto inválido conta como zero (o erro aparece ao salvar). */
    val totalCentavos: Long
        get() = totalDaLinha(lerQuantidade(quantidade) ?: 0.0, lerCentavos(valor) ?: 0L)

    /** Linha que o usuário nem tocou: não vai para o banco. */
    val vazio: Boolean
        get() = descricao.isBlank() && valor.isBlank()

    val quantidadeInvalida: Boolean
        get() = !vazio && (lerQuantidade(quantidade)?.let { it <= 0.0 } ?: true)

    val valorInvalido: Boolean
        get() = valor.isNotBlank() && lerCentavos(valor) == null
}

/** Estado do editor de orçamento num objeto só. */
data class EditorOrcamentoUiState(
    val carregando: Boolean = true,
    /** Falha ao CARREGAR (empresas, clientes ou o orçamento aberto). */
    val erroCarga: String? = null,

    /** `null` = orçamento novo, ainda não salvo. */
    val id: String? = null,
    /** Gerado pelo banco no primeiro salvamento. */
    val numero: Int? = null,
    val status: StatusOrcamento = StatusOrcamento.RASCUNHO,

    val empresas: List<Prestador> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val prestadorId: String? = null,
    val clienteId: String? = null,

    /** Datas como o usuário vê: "13/10/2026". */
    val emissao: String = "",
    val validade: String = "",
    val itens: List<ItemForm> = emptyList(),
    val desconto: String = "",
    val formaPagamento: String = "",
    val prazoPagamento: String = "",
    val observacoes: String = "",

    val seletorClienteAberto: Boolean = false,
    /** Diálogo de cadastro rápido de cliente. `null` = fechado. */
    val novoCliente: FormularioCliente? = null,

    val salvando: Boolean = false,
    val erro: String? = null,
    val aviso: String? = null
) {
    val titulo: String
        get() = if (id == null) "Novo orçamento" else "Orçamento"

    /** O editor depende de ter ao menos uma empresa (docs/UI-UX.md, seção 5.5). */
    val semEmpresa: Boolean
        get() = !carregando && erroCarga == null && empresas.isEmpty()

    /** Carregou e pode editar. */
    val pronto: Boolean
        get() = !carregando && erroCarga == null && empresas.isNotEmpty()

    val mostrarSeletorEmpresa: Boolean
        get() = empresas.size > 1

    val empresaSelecionada: Prestador?
        get() = empresas.firstOrNull { it.id == prestadorId }

    val clienteSelecionado: Cliente?
        get() = clientes.firstOrNull { it.id == clienteId }

    val subtotalCentavos: Long
        get() = itens.sumOf { it.totalCentavos }

    val descontoCentavos: Long
        get() = lerCentavos(desconto) ?: 0L

    val totalGeralCentavos: Long
        get() = subtotalCentavos - descontoCentavos

    val descontoInvalido: Boolean
        get() = (desconto.isNotBlank() && lerCentavos(desconto) == null) ||
            descontoCentavos > subtotalCentavos

    /** Salvar rascunho: precisa de empresa e cliente (as duas FKs do banco são obrigatórias). */
    val podeSalvar: Boolean
        get() = pronto && !salvando && prestadorId != null && clienteId != null

    /** Gerar PDF: além do cliente, ao menos um item com descrição (docs/UI-UX.md, seção 5.3). */
    val podeGerarPdf: Boolean
        get() = podeSalvar && itens.any { it.descricao.isNotBlank() }
}
