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

/**
 * Os bytes do PDF e o nome sugerido para o arquivo. Não é `data class` de
 * propósito: comparar o conteúdo de um ByteArray a cada mudança de estado seria
 * desperdício; basta saber se é o mesmo objeto.
 */
class PdfPronto(val nomeArquivo: String, val bytes: ByteArray)

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
    /** O salvamento em andamento veio do "Gerar PDF" (o indicador aparece nesse botão). */
    val gerandoPdf: Boolean = false,
    /**
     * PDF pronto esperando a tela entregá-lo (salvar, compartilhar ou baixar).
     * O ViewModel não faz isso sozinho porque a entrega depende da plataforma
     * (no Android, do `Context`); ver `core/pdf/EntregadorPdf.kt`.
     */
    val pdfParaEntregar: PdfPronto? = null,
    val erro: String? = null,
    val aviso: String? = null
) {
    val titulo: String
        get() = if (id == null) "Novo orçamento" else "Orçamento"

    /** O editor precisa de ao menos uma empresa cadastrada. */
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

    /**
     * Salvar rascunho: precisa de empresa e cliente (as duas FKs do banco são obrigatórias).
     * Bloqueado também enquanto um PDF está sendo entregue, para não gerar dois.
     */
    val podeSalvar: Boolean
        get() = pronto && !salvando && pdfParaEntregar == null && prestadorId != null && clienteId != null

    /** Gerar PDF: além do cliente, precisa de ao menos um item com descrição. */
    val podeGerarPdf: Boolean
        get() = podeSalvar && itens.any { it.descricao.isNotBlank() }
}
