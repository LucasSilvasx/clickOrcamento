package com.clickorcamento.app.navigation

import kotlinx.serialization.Serializable

/**
 * Rotas tipadas do app logado: cada rota é um `object` (ou `data class`,
 * quando carrega argumento) marcado com @Serializable, e o compilador confere
 * os tipos no lugar de strings como "orcamentos/{id}".
 */
/**
 * Área Orçamentos. É um grafo aninhado que agrupa a lista e o editor.
 * Por isso a aba Orçamentos continua marcada enquanto o editor está aberto
 * (a casca pergunta "a tela atual está dentro de RotaOrcamentos?").
 */
@Serializable
object RotaOrcamentos

/** Início da área Orçamentos: a lista. */
@Serializable
object RotaListaOrcamentos

/** Editor de orçamento. `orcamentoId = null` abre um orçamento novo. */
@Serializable
data class RotaEditorOrcamento(val orcamentoId: String? = null)

@Serializable
object RotaClientes

@Serializable
object RotaConfiguracoes
