package com.clickorcamento.app.feature.configuracoes

import com.clickorcamento.app.domain.model.Prestador

/**
 * Estado da tela de Configurações num objeto só (mesmo padrão do login).
 * Cobre a lista de Empresas, o formulário de empresa e a seção Conta;
 * Aparência entra num próximo passo.
 */
data class ConfiguracoesUiState(
    /** True na primeira carga e no "tentar de novo". */
    val carregando: Boolean = true,
    val empresas: List<Prestador> = emptyList(),
    /** Falha ao CARREGAR a lista (esconde a lista e oferece "tentar de novo"). */
    val erro: String? = null,
    /** E-mail da sessão atual, mostrado na seção Conta. */
    val emailUsuario: String = "",
    /** Formulário aberto (criar/editar). `null` = a tela mostra a lista. */
    val formulario: FormularioEmpresa? = null,
    /** Empresa aguardando confirmação de exclusão (abre o diálogo). */
    val empresaParaExcluir: Prestador? = null,
    /** True enquanto "definir como padrão" ou "excluir" estão em andamento. */
    val ocupado: Boolean = false,
    /** Falha numa AÇÃO (padrão/excluir): aparece sobre a lista, que continua visível. */
    val erroAcao: String? = null,
    /** Confirmação discreta da última ação ("Empresa salva"). */
    val aviso: String? = null
) {
    // Situações da tela, derivadas aqui para a tela só ler e escolher o que
    // desenhar.
    val mostrarCarregando: Boolean
        get() = carregando && empresas.isEmpty()

    val mostrarErro: Boolean
        get() = !carregando && erro != null

    /** Primeiro acesso: o editor de orçamento depende de ter ao menos uma empresa. */
    val mostrarVazio: Boolean
        get() = !carregando && erro == null && empresas.isEmpty()

    /** O botão "Nova empresa" do cabeçalho só aparece quando há lista; no vazio o convite tem o seu. */
    val mostrarBotaoNova: Boolean
        get() = !carregando && erro == null && empresas.isNotEmpty()
}
