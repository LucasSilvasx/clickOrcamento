package com.clickorcamento.app.feature.orcamento.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.core.erro.tentar
import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.domain.model.StatusOrcamento
import com.clickorcamento.app.domain.repository.OrcamentoRepository
import com.clickorcamento.app.domain.repository.PrestadorRepository
import com.clickorcamento.app.feature.orcamento.textoNumero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MensagemErroCarga =
    "Não foi possível carregar seus orçamentos. Verifique a conexão e tente de novo."
private const val MensagemErroAcao =
    "Não foi possível concluir a ação. Verifique a conexão e tente de novo."

class OrcamentosViewModel(
    private val orcamentos: OrcamentoRepository,
    private val prestadores: PrestadorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrcamentosUiState())
    val uiState: StateFlow<OrcamentosUiState> = _uiState.asStateFlow()

    // Sem `init { carregar() }`: a tela chama `carregar()` em ON_RESUME (inclusive
    // na 1ª vez), assim voltar do editor mostra o orçamento recém-salvo.

    fun carregar() {
        _uiState.update { it.copy(carregando = true, erro = null) }

        viewModelScope.launch {
            tentar { orcamentos.listarResumos() }
                .onSuccess { lista ->
                    // Só enfeita a lista (mostrar ou não o nome da empresa); se falhar, segue com 1.
                    val empresas = tentar { prestadores.listar().size }.getOrDefault(1)
                    _uiState.update {
                        it.copy(carregando = false, orcamentos = lista, quantidadeEmpresas = empresas)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(carregando = false, erro = MensagemErroCarga) }
                }
        }
    }

    fun aoMudarBusca(texto: String) {
        _uiState.update { it.copy(busca = texto) }
    }

    fun aoMudarFiltro(status: StatusOrcamento?) {
        _uiState.update { it.copy(filtro = status) }
    }

    // --- Ações por linha ---

    fun duplicar(orcamento: OrcamentoResumo) {
        executarAcao(sucesso = { "Orçamento duplicado: ${textoNumero(orcamento.numero)} virou ${it}" }) {
            val novo = orcamentos.duplicar(orcamento.id)
            textoNumero(novo.numero)
        }
    }

    fun alterarStatus(orcamento: OrcamentoResumo, status: StatusOrcamento) {
        if (orcamento.status == status) return
        executarAcao(sucesso = { "${textoNumero(orcamento.numero)} marcado como ${status.rotulo.lowercase()}" }) {
            orcamentos.atualizarStatus(orcamento.id, status)
            ""
        }
    }

    fun pedirExclusao(orcamento: OrcamentoResumo) {
        _uiState.update { it.copy(orcamentoParaExcluir = orcamento, aviso = null, erroAcao = null) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(orcamentoParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val alvo = _uiState.value.orcamentoParaExcluir ?: return
        _uiState.update { it.copy(orcamentoParaExcluir = null) }
        executarAcao(sucesso = { "${textoNumero(alvo.numero)} excluído" }) {
            orcamentos.excluir(alvo.id)
            ""
        }
    }

    /**
     * Roda uma ação do repository com a tela "ocupada" e recarrega a lista no
     * fim, dando certo ou não. [acao] devolve um texto que [sucesso] pode usar
     * na mensagem (ex.: o número do orçamento duplicado).
     */
    private fun executarAcao(sucesso: (String) -> String, acao: suspend () -> String) {
        _uiState.update { it.copy(ocupado = true, erroAcao = null, aviso = null) }

        viewModelScope.launch {
            tentar { acao() }
                .onSuccess { resultado ->
                    _uiState.update { it.copy(ocupado = false, aviso = sucesso(resultado)) }
                }
                .onFailure {
                    _uiState.update { it.copy(ocupado = false, erroAcao = MensagemErroAcao) }
                }
            carregar()
        }
    }
}
