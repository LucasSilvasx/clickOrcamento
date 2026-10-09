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
 *
 * As três áreas vêm do docs/UI-UX.md (seção 4).
 */
@Serializable
object RotaOrcamentos

@Serializable
object RotaClientes

@Serializable
object RotaConfiguracoes
