package com.clickorcamento.app.feature.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.repository.ClienteRepository
import com.clickorcamento.app.domain.repository.OrcamentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientesViewModel(
    private val clientes: ClienteRepository,
    private val orcamentos: OrcamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    // Sem `init { carregar() }`: a tela chama `carregar()` ao entrar em primeiro
    // plano (ON_RESUME), inclusive na primeira vez. Assim, voltar do editor de
    // orçamento (onde pode ter nascido um cliente novo) atualiza a lista.

    fun carregar() {
        _uiState.update { it.copy(carregando = true, erro = null) }

        viewModelScope.launch {
            runCatching { clientes.listar() }
                .onSuccess { lista ->
                    // Os orçamentos só enfeitam a lista (contagem e "últimos orçamentos"):
                    // se falharem, a lista de clientes aparece do mesmo jeito.
                    val resumos = runCatching { orcamentos.listarResumos() }.getOrDefault(emptyList())
                    _uiState.update {
                        it.copy(carregando = false, clientes = lista, orcamentos = resumos)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(carregando = false, erro = traduzirErroCarga(e)) }
                }
        }
    }

    fun aoMudarBusca(texto: String) {
        _uiState.update { it.copy(busca = texto) }
    }

    // --- Formulário ---

    fun novoCliente() {
        _uiState.update { it.copy(formulario = FormularioCliente(), aviso = null) }
    }

    fun editarCliente(cliente: Cliente) {
        _uiState.update { it.copy(formulario = FormularioCliente.de(cliente), aviso = null) }
    }

    /** A tela devolve o formulário inteiro já com o campo trocado (`form.copy(...)`). */
    fun aoMudarFormulario(novo: FormularioCliente) {
        _uiState.update {
            if (it.formulario == null || it.formulario.salvando) it else it.copy(formulario = novo)
        }
    }

    fun fecharFormulario() {
        _uiState.update { if (it.formulario?.salvando == true) it else it.copy(formulario = null) }
    }

    fun salvarFormulario() {
        val form = _uiState.value.formulario ?: return
        if (!form.podeSalvar) return

        _uiState.update { it.copy(formulario = form.copy(salvando = true, erro = null)) }

        viewModelScope.launch {
            runCatching {
                if (form.id == null) {
                    clientes.criar(form.paraDados())
                } else {
                    clientes.atualizar(form.id, form.paraDados())
                }
            }.onSuccess {
                _uiState.update { it.copy(formulario = null, aviso = "Cliente salvo") }
                carregar()
            }.onFailure { e ->
                // O formulário continua aberto e preenchido: o usuário não perde o que digitou.
                _uiState.update {
                    it.copy(formulario = form.copy(salvando = false, erro = traduzirErroSalvar(e)))
                }
            }
        }
    }

    // --- Exclusão ---

    /** Chamado pelo botão "Excluir" do formulário aberto. */
    fun pedirExclusao() {
        val id = _uiState.value.formulario?.id ?: return
        val cliente = _uiState.value.clientes.firstOrNull { it.id == id } ?: return
        _uiState.update { it.copy(clienteParaExcluir = cliente) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(clienteParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val alvo = _uiState.value.clienteParaExcluir ?: return
        val form = _uiState.value.formulario
        _uiState.update {
            it.copy(clienteParaExcluir = null, formulario = form?.copy(salvando = true, erro = null))
        }

        viewModelScope.launch {
            runCatching { clientes.excluir(alvo.id) }
                .onSuccess {
                    _uiState.update { it.copy(formulario = null, aviso = "Cliente excluído") }
                    carregar()
                }
                .onFailure { e ->
                    // O motivo aparece dentro do formulário, que continua aberto.
                    _uiState.update {
                        it.copy(formulario = form?.copy(salvando = false, erro = traduzirErroExcluir(e)))
                    }
                }
        }
    }

    // --- Erros ---

    private fun traduzirErroCarga(e: Throwable): String =
        "Não foi possível carregar seus clientes. Verifique a conexão " +
            "(se o projeto ficou parado, o Supabase pode estar pausado). ${e.message.orEmpty()}"

    private fun traduzirErroSalvar(e: Throwable): String =
        "Não foi possível salvar o cliente. Verifique a conexão e tente de novo. ${e.message.orEmpty()}"

    private fun traduzirErroExcluir(e: Throwable): String {
        val original = e.message.orEmpty()
        // 23503 = foreign_key_violation: o orçamento aponta para o cliente com ON DELETE RESTRICT.
        return if (original.contains("23503") || original.contains("foreign key", ignoreCase = true)) {
            "Este cliente tem orçamentos e não pode ser excluído."
        } else {
            "Não foi possível excluir o cliente. Verifique a conexão e tente de novo. $original"
        }
    }
}
