package com.clickorcamento.app.data.remote.dto

import com.clickorcamento.app.domain.model.DadosOrcamento
import com.clickorcamento.app.domain.model.ItemOrcamento
import com.clickorcamento.app.domain.model.Orcamento
import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.domain.model.StatusOrcamento
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Linha da view `orcamento_resumo`. Datas chegam como texto ISO ("2026-10-13"). */
@Serializable
data class OrcamentoResumoDto(
    val id: String,
    val numero: Int,
    @SerialName("data_emissao") val dataEmissao: String,
    @SerialName("data_validade") val dataValidade: String,
    val status: String,
    @SerialName("prestador_id") val prestadorId: String,
    // Os nomes e somas da view podem vir nulos (orçamento sem itens, por ex.).
    @SerialName("prestador_nome") val prestadorNome: String? = null,
    @SerialName("cliente_id") val clienteId: String,
    @SerialName("cliente_razao_social") val clienteRazaoSocial: String? = null,
    @SerialName("desconto_centavos") val descontoCentavos: Long? = null,
    @SerialName("subtotal_centavos") val subtotalCentavos: Long? = null,
    @SerialName("total_geral_centavos") val totalGeralCentavos: Long? = null
) {
    fun paraDominio() = OrcamentoResumo(
        id = id,
        numero = numero,
        dataEmissao = LocalDate.parse(dataEmissao),
        dataValidade = LocalDate.parse(dataValidade),
        status = StatusOrcamento.deTexto(status),
        prestadorId = prestadorId,
        prestadorNome = prestadorNome.orEmpty(),
        clienteId = clienteId,
        clienteRazaoSocial = clienteRazaoSocial.orEmpty(),
        descontoCentavos = descontoCentavos ?: 0,
        subtotalCentavos = subtotalCentavos ?: 0,
        totalGeralCentavos = totalGeralCentavos ?: 0
    )
}

/** Linha da tabela `orcamento`. */
@Serializable
data class OrcamentoDto(
    val id: String,
    val numero: Int,
    @SerialName("data_emissao") val dataEmissao: String,
    @SerialName("data_validade") val dataValidade: String,
    @SerialName("prestador_id") val prestadorId: String,
    @SerialName("cliente_id") val clienteId: String,
    @SerialName("desconto_centavos") val descontoCentavos: Long = 0,
    @SerialName("forma_pagamento") val formaPagamento: String = "",
    @SerialName("prazo_pagamento") val prazoPagamento: String = "",
    val observacoes: String = "",
    val status: String = "RASCUNHO"
) {
    fun paraDominio(itens: List<ItemOrcamento>) = Orcamento(
        id = id,
        numero = numero,
        dataEmissao = LocalDate.parse(dataEmissao),
        dataValidade = LocalDate.parse(dataValidade),
        prestadorId = prestadorId,
        clienteId = clienteId,
        descontoCentavos = descontoCentavos,
        formaPagamento = formaPagamento,
        prazoPagamento = prazoPagamento,
        observacoes = observacoes,
        status = StatusOrcamento.deTexto(status),
        itens = itens
    )
}

/** Linha da tabela `item_orcamento` (`quantidade` é numeric no banco; chega como número JSON). */
@Serializable
data class ItemOrcamentoDto(
    val ordem: Int,
    val descricao: String = "",
    val quantidade: Double = 1.0,
    val unidade: String = "un",
    @SerialName("valor_unitario_centavos") val valorUnitarioCentavos: Long = 0
) {
    fun paraDominio() = ItemOrcamento(
        descricao = descricao,
        quantidade = quantidade,
        unidade = unidade,
        valorUnitarioCentavos = valorUnitarioCentavos
    )
}

/** Corpo do INSERT/UPDATE em `orcamento`. `numero` fica de fora: o trigger preenche. */
fun DadosOrcamento.paraJson(): JsonObject = buildJsonObject {
    put("data_emissao", dataEmissao.toString())
    put("data_validade", dataValidade.toString())
    put("prestador_id", prestadorId)
    put("cliente_id", clienteId)
    put("desconto_centavos", descontoCentavos)
    put("forma_pagamento", formaPagamento)
    put("prazo_pagamento", prazoPagamento)
    put("observacoes", observacoes)
}

/** Corpo de um item. `ordem` começa em 1. `usuario_id` o banco preenche (default auth.uid()). */
fun ItemOrcamento.paraJson(orcamentoId: String, ordem: Int): JsonObject = buildJsonObject {
    put("orcamento_id", orcamentoId)
    put("ordem", ordem)
    put("descricao", descricao)
    put("quantidade", quantidade)
    put("unidade", unidade)
    put("valor_unitario_centavos", valorUnitarioCentavos)
}
