package com.clickorcamento.app.data.repository

import com.clickorcamento.app.core.format.hoje
import com.clickorcamento.app.core.format.maisDias
import com.clickorcamento.app.data.remote.dto.ItemOrcamentoDto
import com.clickorcamento.app.data.remote.dto.OrcamentoDto
import com.clickorcamento.app.data.remote.dto.OrcamentoResumoDto
import com.clickorcamento.app.data.remote.dto.paraJson
import com.clickorcamento.app.domain.model.DadosOrcamento
import com.clickorcamento.app.domain.model.ItemOrcamento
import com.clickorcamento.app.domain.model.Orcamento
import com.clickorcamento.app.domain.model.OrcamentoCriado
import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.domain.model.StatusOrcamento
import com.clickorcamento.app.domain.repository.OrcamentoRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OrcamentoRepositorySupabase(
    private val supabase: SupabaseClient
) : OrcamentoRepository {

    override suspend fun listarResumos(): List<OrcamentoResumo> =
        supabase.from("orcamento_resumo")
            .select { order("numero", Order.DESCENDING) }
            .decodeList<OrcamentoResumoDto>()
            .map { it.paraDominio() }

    override suspend fun buscar(id: String): Orcamento {
        val orcamento = supabase.from("orcamento")
            .select { filter { eq("id", id) } }
            .decodeSingle<OrcamentoDto>()

        val itens = supabase.from("item_orcamento")
            .select {
                filter { eq("orcamento_id", id) }
                order("ordem", Order.ASCENDING)
            }
            .decodeList<ItemOrcamentoDto>()
            .map { it.paraDominio() }

        return orcamento.paraDominio(itens)
    }

    override suspend fun criar(dados: DadosOrcamento): OrcamentoCriado {
        // `numero` não vai no corpo: o trigger definir_numero_orcamento preenche.
        val criado = supabase.from("orcamento")
            .insert(dados.paraJson()) { select() }
            .decodeSingle<OrcamentoDto>()

        try {
            gravarItens(criado.id, dados.itens)
        } catch (e: Throwable) {
            // Sem isto, uma falha nos itens deixaria um orçamento vazio no banco e
            // o "tentar de novo" do usuário criaria um segundo. Desfaz e repassa o erro.
            runCatching { excluir(criado.id) }
            throw e
        }
        return OrcamentoCriado(id = criado.id, numero = criado.numero)
    }

    override suspend fun atualizar(id: String, dados: DadosOrcamento): Int {
        // `select()` devolve a linha já gravada: se a empresa mudou, o trigger
        // trocou o número e é este novo que a tela (e o PDF) precisam usar.
        val atualizado = supabase.from("orcamento")
            .update(dados.paraJson()) {
                select()
                filter { eq("id", id) }
            }
            .decodeSingle<OrcamentoDto>()
        gravarItens(id, dados.itens)
        return atualizado.numero
    }

    override suspend fun duplicar(id: String): OrcamentoCriado {
        val original = buscar(id)
        val hoje = hoje()
        return criar(
            DadosOrcamento(
                dataEmissao = hoje,
                dataValidade = hoje.maisDias(30),
                prestadorId = original.prestadorId,
                clienteId = original.clienteId,
                descontoCentavos = original.descontoCentavos,
                formaPagamento = original.formaPagamento,
                prazoPagamento = original.prazoPagamento,
                observacoes = original.observacoes,
                itens = original.itens
            )
        )
    }

    override suspend fun atualizarStatus(id: String, status: StatusOrcamento) {
        supabase.from("orcamento").update(buildJsonObject { put("status", status.name) }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun excluir(id: String) {
        supabase.from("orcamento").delete {
            filter { eq("id", id) }
        }
    }

    /**
     * Grava os itens sem apagar tudo antes: faz upsert pela chave
     * (orcamento_id, ordem) e depois remove o que sobrou além do último item.
     * Se algo falhar no meio, os itens antigos continuam no banco (e o que o
     * usuário digitou continua na tela para salvar de novo).
     */
    private suspend fun gravarItens(orcamentoId: String, itens: List<ItemOrcamento>) {
        if (itens.isNotEmpty()) {
            supabase.from("item_orcamento").upsert(
                itens.mapIndexed { indice, item -> item.paraJson(orcamentoId, indice + 1) }
            ) {
                onConflict = "orcamento_id,ordem"
            }
        }
        supabase.from("item_orcamento").delete {
            filter {
                eq("orcamento_id", orcamentoId)
                gt("ordem", itens.size)
            }
        }
    }
}
