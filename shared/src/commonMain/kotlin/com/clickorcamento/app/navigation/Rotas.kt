package com.clickorcamento.app.navigation

import kotlinx.serialization.Serializable

/**
 * Rotas do app logado. Pense nelas como os "endereços" das telas.
 *
 * No Android puro você provavelmente usava strings ("orcamentos/{id}").
 * Aqui cada rota é um `object` (ou `data class`, quando carrega argumento)
 * marcado com @Serializable: o compilador checa os tipos e some o risco de
 * errar uma string. Quando o editor precisar de argumento, vira, por ex.:
 * `data class Editor(val orcamentoId: String? = null)`.
 */
/**
 * Área Orçamentos. Agora é um "grafo": uma pasta que agrupa a lista e o editor.
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
