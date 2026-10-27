package com.clickorcamento.app.domain.repository

import com.clickorcamento.app.domain.model.DadosOrcamento
import com.clickorcamento.app.domain.model.Orcamento
import com.clickorcamento.app.domain.model.OrcamentoCriado
import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.domain.model.StatusOrcamento

/** Contrato de acesso aos orçamentos e seus itens. */
interface OrcamentoRepository {
    /** Lista para a tela de orçamentos (view `orcamento_resumo`), do número mais novo para o mais antigo. */
    suspend fun listarResumos(): List<OrcamentoResumo>

    /** Orçamento completo, com itens em ordem. */
    suspend fun buscar(id: String): Orcamento

    /** Cria o orçamento (status RASCUNHO) e seus itens. O número é gerado pelo banco. */
    suspend fun criar(dados: DadosOrcamento): OrcamentoCriado

    /**
     * Atualiza os dados e substitui os itens. Não mexe no status.
     * Devolve o número: a numeração é por empresa, então trocar a empresa
     * faz o banco dar um número novo (trigger definir_numero_orcamento).
     */
    suspend fun atualizar(id: String, dados: DadosOrcamento): Int

    /** Cria um rascunho novo com os mesmos dados/itens, emissão hoje e validade em 30 dias. */
    suspend fun duplicar(id: String): OrcamentoCriado

    suspend fun atualizarStatus(id: String, status: StatusOrcamento)

    /** Os itens vão junto (ON DELETE CASCADE). */
    suspend fun excluir(id: String)
}
