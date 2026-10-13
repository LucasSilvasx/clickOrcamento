package com.clickorcamento.app.domain.model

import com.clickorcamento.app.core.format.totalDaLinha
import kotlinx.datetime.LocalDate

/** Os quatro estados do orçamento; o nome do enum é exatamente o texto salvo no banco. */
enum class StatusOrcamento(val rotulo: String) {
    RASCUNHO("Rascunho"),
    EMITIDO("Emitido"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado");

    companion object {
        fun deTexto(texto: String): StatusOrcamento =
            entries.firstOrNull { it.name == texto } ?: RASCUNHO
    }
}

data class ItemOrcamento(
    val descricao: String,
    val quantidade: Double,
    val unidade: String,
    val valorUnitarioCentavos: Long
) {
    /** Nada de total salvo: sempre calculado (CLAUDE.md, seção 6). */
    val totalCentavos: Long
        get() = totalDaLinha(quantidade, valorUnitarioCentavos)
}

/** Orçamento completo (com itens), usado pelo editor. */
data class Orcamento(
    val id: String,
    val numero: Int,
    val dataEmissao: LocalDate,
    val dataValidade: LocalDate,
    val prestadorId: String,
    val clienteId: String,
    val descontoCentavos: Long,
    val formaPagamento: String,
    val prazoPagamento: String,
    val observacoes: String,
    val status: StatusOrcamento,
    val itens: List<ItemOrcamento>
)

/** Linha da lista (vem da view `orcamento_resumo`, já com nomes e totais). */
data class OrcamentoResumo(
    val id: String,
    val numero: Int,
    val dataEmissao: LocalDate,
    val dataValidade: LocalDate,
    val status: StatusOrcamento,
    val prestadorId: String,
    val prestadorNome: String,
    val clienteId: String,
    val clienteRazaoSocial: String,
    val descontoCentavos: Long,
    val subtotalCentavos: Long,
    val totalGeralCentavos: Long
)

/** O que o editor manda para salvar. Número e status não entram: quem cuida deles é o banco/ações da lista. */
data class DadosOrcamento(
    val dataEmissao: LocalDate,
    val dataValidade: LocalDate,
    val prestadorId: String,
    val clienteId: String,
    val descontoCentavos: Long,
    val formaPagamento: String,
    val prazoPagamento: String,
    val observacoes: String,
    val itens: List<ItemOrcamento>
)

/** Resposta de criar/duplicar: o número é gerado pelo trigger do banco. */
data class OrcamentoCriado(val id: String, val numero: Int)
