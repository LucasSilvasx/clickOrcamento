package com.clickorcamento.app.feature.cliente

import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.OrcamentoResumo

/** Estado da tela de Clientes num objeto só (mesmo padrão do login e das configurações). */
data class ClientesUiState(
    /** True na primeira carga e a cada recarga (só vira tela de carregando se a lista estiver vazia). */
    val carregando: Boolean = true,
    val clientes: List<Cliente> = emptyList(),
    /** Resumo dos orçamentos, só para contar e listar os últimos de cada cliente. */
    val orcamentos: List<OrcamentoResumo> = emptyList(),
    /** Falha ao CARREGAR a lista. */
    val erro: String? = null,
    val busca: String = "",
    /** Formulário aberto (criar/editar). `null` = nenhum. No desktop é o painel da direita. */
    val formulario: FormularioCliente? = null,
    /** Cliente aguardando confirmação de exclusão (abre o diálogo). */
    val clienteParaExcluir: Cliente? = null,
    /** Confirmação discreta da última ação ("Cliente salvo"). */
    val aviso: String? = null
) {
    /** Busca por nome (qualquer parte) ou por dígitos do CNPJ/CPF. */
    val filtrados: List<Cliente>
        get() {
            val termo = busca.trim()
            if (termo.isEmpty()) return clientes
            val digitos = termo.filter { it.isDigit() }
            return clientes.filter { cliente ->
                cliente.razaoSocial.contains(termo, ignoreCase = true) ||
                    (digitos.isNotEmpty() && cliente.cnpj.filter { it.isDigit() }.contains(digitos))
            }
        }

    val mostrarCarregando: Boolean
        get() = carregando && clientes.isEmpty()

    val mostrarErro: Boolean
        get() = !carregando && erro != null

    /** Primeiro acesso: nenhum cliente cadastrado. */
    val mostrarVazio: Boolean
        get() = !carregando && erro == null && clientes.isEmpty()

    /** Há clientes, mas a busca não achou nenhum. */
    val mostrarSemResultado: Boolean
        get() = !carregando && erro == null && clientes.isNotEmpty() && filtrados.isEmpty()

    fun orcamentosDe(clienteId: String): List<OrcamentoResumo> =
        orcamentos.filter { it.clienteId == clienteId }
}
