package com.clickorcamento.app.feature.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.domain.repository.PrestadorRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * O repository traz as empresas; o SupabaseClient entra só para a seção Conta
 * (e-mail logado e sair), que é do Auth e não de uma tabela.
 */
class ConfiguracoesViewModel(
    private val prestadores: PrestadorRepository,
    private val supabase: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConfiguracoesUiState(emailUsuario = supabase.auth.currentUserOrNull()?.email.orEmpty())
    )
    val uiState: StateFlow<ConfiguracoesUiState> = _uiState.asStateFlow()

    // Carrega assim que a tela nasce. O ViewModel vive enquanto a tela estiver
    // na pilha, então trocar de aba e voltar não refaz a busca.
    init {
        carregar()
    }

    fun carregar() {
        _uiState.update { it.copy(carregando = true, erro = null) }

        viewModelScope.launch {
            runCatching { prestadores.listar() }
                .onSuccess { lista ->
                    _uiState.update { it.copy(carregando = false, empresas = lista) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(carregando = false, erro = traduzirErro(e)) }
                }
        }
    }

    /**
     * Encerra a sessão. Não navegamos para lugar nenhum: o `sessionStatus`
     * do App.kt percebe o logout e troca para o Login sozinho.
     */
    fun sair() {
        viewModelScope.launch {
            // Se falhar, a sessão continua e a tela não muda: o usuário vê que
            // nada aconteceu e toca de novo. Mensagem própria fica para depois.
            runCatching { supabase.auth.signOut() }
        }
    }

    private fun traduzirErro(e: Throwable): String {
        val original = e.message.orEmpty()
        return "Não foi possível carregar suas empresas. Verifique a conexão " +
            "(se o projeto ficou parado, o Supabase pode estar pausado). $original"
    }
}
