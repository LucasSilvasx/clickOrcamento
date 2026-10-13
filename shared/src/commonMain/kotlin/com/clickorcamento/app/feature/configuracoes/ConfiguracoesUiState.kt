package com.clickorcamento.app.feature.configuracoes

import com.clickorcamento.app.domain.model.Prestador

/**
 * Estado da tela de Configurações num objeto só (mesmo padrão do login).
 * Hoje cobre a lista de Empresas e a seção Conta; Aparência e o formulário
 * de empresa entram nos próximos passos.
 */
data class ConfiguracoesUiState(
    /** True na primeira carga e no "tentar de novo". */
    val carregando: Boolean = true,
    val empresas: List<Prestador> = emptyList(),
    val erro: String? = null,
    /** E-mail da sessão atual, mostrado na seção Conta. */
    val emailUsuario: String = ""
) {
    // As quatro situações da tela (docs/UI-UX.md, seção 7), derivadas aqui
    // para a tela só ler e escolher o que desenhar.
    val mostrarCarregando: Boolean
        get() = carregando && empresas.isEmpty()

    val mostrarErro: Boolean
        get() = !carregando && erro != null

    /** Primeiro acesso: o editor de orçamento depende de ter ao menos uma empresa. */
    val mostrarVazio: Boolean
        get() = !carregando && erro == null && empresas.isEmpty()
}
