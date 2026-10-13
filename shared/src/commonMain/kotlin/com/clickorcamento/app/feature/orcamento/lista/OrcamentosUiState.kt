package com.clickorcamento.app.feature.orcamento.lista

import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.domain.model.StatusOrcamento

/** Estado da lista de orçamentos num objeto só. */
data class OrcamentosUiState(
    /** True na primeira carga e a cada recarga (só vira tela de carregando se a lista estiver vazia). */
    val carregando: Boolean = true,
    val orcamentos: List<OrcamentoResumo> = emptyList(),
    /** Quantas empresas o usuário tem: só com mais de uma o apelido da empresa aparece nas linhas. */
    val quantidadeEmpresas: Int = 1,
    /** Falha ao CARREGAR a lista. */
    val erro: String? = null,
    val busca: String = "",
    /** `null` = "Todos". */
    val filtro: StatusOrcamento? = null,
    /** Orçamento aguardando confirmação de exclusão (abre o diálogo). */
    val orcamentoParaExcluir: OrcamentoResumo? = null,
    /** True enquanto duplicar/mudar status/excluir estão em andamento. */
    val ocupado: Boolean = false,
    /** Falha numa AÇÃO: aparece sobre a lista, que continua visível. */
    val erroAcao: String? = null,
    val aviso: String? = null
) {
    val mostrarEmpresa: Boolean
        get() = quantidadeEmpresas > 1

    /** Filtra por status e busca por nome do cliente ou pelo número ("9" ou "0009"). */
    val filtrados: List<OrcamentoResumo>
        get() {
            val termo = busca.trim()
            return orcamentos.filter { o ->
                (filtro == null || o.status == filtro) &&
                    (
                        termo.isEmpty() ||
                            o.clienteRazaoSocial.contains(termo, ignoreCase = true) ||
                            o.numero.toString().padStart(4, '0').contains(termo)
                        )
            }
        }

    val mostrarCarregando: Boolean
        get() = carregando && orcamentos.isEmpty()

    val mostrarErro: Boolean
        get() = !carregando && erro != null

    /** Primeiro acesso: nenhum orçamento ainda. */
    val mostrarVazio: Boolean
        get() = !carregando && erro == null && orcamentos.isEmpty()

    /** Há orçamentos, mas o filtro/busca não achou nenhum. */
    val mostrarSemResultado: Boolean
        get() = !carregando && erro == null && orcamentos.isNotEmpty() && filtrados.isEmpty()
}
