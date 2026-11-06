package com.clickorcamento.app.feature.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.core.erro.tentar
import com.clickorcamento.app.domain.model.Prestador
import com.clickorcamento.app.domain.repository.PrestadorRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MensagemErroCarga =
    "Não foi possível carregar suas empresas. Verifique a conexão e tente de novo."
private const val MensagemErroSalvar =
    "Não foi possível salvar a empresa. Verifique a conexão e tente de novo."

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
        // Relê o e-mail porque a tela pode nascer em RefreshFailure, quando ainda não há sessão ativa.
        _uiState.update {
            it.copy(
                carregando = true,
                erro = null,
                emailUsuario = supabase.auth.currentUserOrNull()?.email ?: it.emailUsuario
            )
        }

        viewModelScope.launch {
            tentar { prestadores.listar() }
                .onSuccess { lista ->
                    _uiState.update { it.copy(carregando = false, empresas = lista) }
                }
                .onFailure {
                    _uiState.update { it.copy(carregando = false, erro = MensagemErroCarga) }
                }
        }
    }

    // --- Formulário ---

    fun novaEmpresa() {
        _uiState.update { it.copy(formulario = FormularioEmpresa(), aviso = null, erroAcao = null) }
    }

    fun editarEmpresa(empresa: Prestador) {
        _uiState.update {
            it.copy(formulario = FormularioEmpresa.de(empresa), aviso = null, erroAcao = null)
        }
    }

    /**
     * A tela devolve o formulário inteiro já com o campo trocado
     * (`form.copy(apelido = ...)`): evita uma função por campo. Ignora se o
     * formulário já fechou ou está salvando (campos desabilitados).
     */
    fun aoMudarFormulario(novo: FormularioEmpresa) {
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

        // A primeira empresa do usuário nasce como padrão (o editor depende dela).
        val primeira = _uiState.value.empresas.isEmpty()

        _uiState.update { it.copy(formulario = form.copy(salvando = true, erro = null)) }

        viewModelScope.launch {
            tentar {
                if (form.id == null) {
                    prestadores.criar(form.paraDados(), padrao = primeira)
                } else {
                    prestadores.atualizar(form.id, form.paraDados())
                }
            }.onSuccess {
                // Fecha o formulário e relê a lista: o banco é a fonte da verdade.
                _uiState.update { it.copy(formulario = null, aviso = "Empresa salva") }
                carregar()
            }.onFailure {
                // O formulário continua aberto e preenchido: o usuário não perde o que digitou.
                _uiState.update {
                    it.copy(formulario = form.copy(salvando = false, erro = MensagemErroSalvar))
                }
            }
        }
    }

    // --- Padrão e exclusão ---

    fun definirComoPadrao(empresa: Prestador) {
        if (empresa.padrao) return
        executarAcao("${empresa.apelido} agora é a empresa padrão") {
            prestadores.definirPadrao(empresa.id)
        }
    }

    fun pedirExclusao(empresa: Prestador) {
        // Regra de negócio: sem padrão o orçamento novo não sabe qual empresa usar.
        if (empresa.padrao && _uiState.value.empresas.size > 1) {
            _uiState.update {
                it.copy(
                    aviso = null,
                    erroAcao = "Defina outra empresa como padrão antes de excluir " +
                        "\"${empresa.apelido}\"."
                )
            }
            return
        }
        _uiState.update { it.copy(empresaParaExcluir = empresa, aviso = null, erroAcao = null) }
    }

    fun cancelarExclusao() {
        _uiState.update { it.copy(empresaParaExcluir = null) }
    }

    fun confirmarExclusao() {
        val alvo = _uiState.value.empresaParaExcluir ?: return
        _uiState.update { it.copy(empresaParaExcluir = null) }
        executarAcao("Empresa excluída") { prestadores.excluir(alvo.id) }
    }

    /**
     * Roda uma ação do repository com a tela "ocupada" e recarrega a lista no
     * fim, dando certo ou não (se `definirPadrao` falhar no meio, a lista
     * recarregada mostra o estado real do banco).
     */
    private fun executarAcao(avisoSucesso: String, acao: suspend () -> Unit) {
        _uiState.update { it.copy(ocupado = true, erroAcao = null, aviso = null) }

        viewModelScope.launch {
            tentar { acao() }
                .onSuccess {
                    _uiState.update { it.copy(ocupado = false, aviso = avisoSucesso) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(ocupado = false, erroAcao = traduzirErroAcao(e)) }
                }
            carregar()
        }
    }

    // --- Conta ---

    /** O `sessionStatus` do App.kt percebe o logout e troca para o Login. */
    fun sair() {
        viewModelScope.launch {
            // Sem rede o aviso ao servidor falha, mas a sessão deste aparelho precisa acabar:
            // quem pede para sair pode estar num computador compartilhado.
            tentar { supabase.auth.signOut() }
                .onFailure { tentar { supabase.auth.clearSession() } }
        }
    }

    fun abrirExclusaoConta() {
        _uiState.update {
            it.copy(exclusaoContaAberta = true, confirmacaoExclusaoConta = "", erroExclusaoConta = null)
        }
    }

    fun aoMudarConfirmacaoExclusaoConta(texto: String) {
        _uiState.update { it.copy(confirmacaoExclusaoConta = texto, erroExclusaoConta = null) }
    }

    fun fecharExclusaoConta() {
        _uiState.update { if (it.excluindoConta) it else it.copy(exclusaoContaAberta = false) }
    }

    /**
     * Apaga a conta e todos os dados no banco. O `sessionStatus` do App.kt percebe o fim
     * da sessão e troca para o Login, e o App.kt limpa o cache das listas.
     */
    fun confirmarExclusaoConta() {
        if (!_uiState.value.podeExcluirConta) return
        _uiState.update { it.copy(excluindoConta = true, erroExclusaoConta = null) }

        viewModelScope.launch {
            tentar {
                supabase.postgrest.rpc("excluir_minha_conta")
                // clearSession em vez de signOut: o usuário já não existe no servidor,
                // então só falta apagar a sessão guardada neste aparelho.
                supabase.auth.clearSession()
            }.onFailure {
                _uiState.update {
                    it.copy(
                        excluindoConta = false,
                        erroExclusaoConta = "Não foi possível excluir a conta. Verifique a " +
                            "conexão e tente de novo."
                    )
                }
            }
        }
    }

    // --- Erros ---

    private fun traduzirErroAcao(e: Throwable): String {
        val original = e.message.orEmpty()
        // 23503 = foreign_key_violation do Postgres: o orcamento aponta para
        // esta empresa com ON DELETE RESTRICT.
        return if (original.contains("23503") || original.contains("foreign key", ignoreCase = true)) {
            "Esta empresa tem orçamentos e não pode ser excluída."
        } else {
            "Não foi possível concluir a ação. Verifique a conexão e tente de novo."
        }
    }
}
