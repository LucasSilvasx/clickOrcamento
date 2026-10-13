package com.clickorcamento.app.feature.orcamento.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickorcamento.app.core.format.formatarQuantidade
import com.clickorcamento.app.core.format.formatarValor
import com.clickorcamento.app.core.format.formatarBr
import com.clickorcamento.app.core.format.hoje
import com.clickorcamento.app.core.format.lerCentavos
import com.clickorcamento.app.core.format.lerDataBr
import com.clickorcamento.app.core.format.lerQuantidade
import com.clickorcamento.app.core.format.mascararData
import com.clickorcamento.app.core.format.maisDias
import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosOrcamento
import com.clickorcamento.app.domain.model.ItemOrcamento
import com.clickorcamento.app.domain.model.Orcamento
import com.clickorcamento.app.domain.model.Prestador
import com.clickorcamento.app.domain.repository.ClienteRepository
import com.clickorcamento.app.domain.repository.OrcamentoRepository
import com.clickorcamento.app.domain.repository.PrestadorRepository
import com.clickorcamento.app.feature.cliente.FormularioCliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @param orcamentoId `null` abre um orçamento novo; com id, abre esse orçamento para editar.
 *   Chega pelo Koin (`parametersOf`) a partir do argumento da rota.
 */
class EditorOrcamentoViewModel(
    orcamentoId: String?,
    private val orcamentos: OrcamentoRepository,
    private val prestadores: PrestadorRepository,
    private val clientes: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorOrcamentoUiState(id = orcamentoId))
    val uiState: StateFlow<EditorOrcamentoUiState> = _uiState.asStateFlow()

    private var proximaChave = 1L
    private fun novaChave() = proximaChave++

    init {
        carregar()
    }

    // ───────────────────────────── Carga ─────────────────────────────

    fun carregar() {
        _uiState.update { it.copy(carregando = true, erroCarga = null) }

        viewModelScope.launch {
            runCatching {
                val empresas = prestadores.listar()
                val lista = clientes.listar()
                val existente = _uiState.value.id?.let { orcamentos.buscar(it) }
                Triple(empresas, lista, existente)
            }.onSuccess { (empresas, lista, existente) ->
                _uiState.update { montarEstado(it, empresas, lista, existente) }
            }.onFailure { e ->
                _uiState.update { it.copy(carregando = false, erroCarga = traduzirErroCarga(e)) }
            }
        }
    }

    /**
     * Orçamento existente: preenche tudo com o que veio do banco.
     * Orçamento novo: o sistema faz, o usuário confere (UI-UX, princípio 4) —
     * emissão hoje, validade em 30 dias, empresa padrão e as condições dela.
     */
    private fun montarEstado(
        base: EditorOrcamentoUiState,
        empresas: List<Prestador>,
        lista: List<Cliente>,
        existente: Orcamento?
    ): EditorOrcamentoUiState {
        val comListas = base.copy(carregando = false, empresas = empresas, clientes = lista)

        if (existente != null) {
            return comListas.copy(
                id = existente.id,
                numero = existente.numero,
                status = existente.status,
                prestadorId = existente.prestadorId,
                clienteId = existente.clienteId,
                emissao = existente.dataEmissao.formatarBr(),
                validade = existente.dataValidade.formatarBr(),
                itens = existente.itens.map { it.paraForm() }.ifEmpty { listOf(ItemForm(novaChave())) },
                desconto = if (existente.descontoCentavos > 0) formatarValor(existente.descontoCentavos) else "",
                formaPagamento = existente.formaPagamento,
                prazoPagamento = existente.prazoPagamento,
                observacoes = existente.observacoes
            )
        }

        // Sem nenhuma empresa não há o que preencher: a tela mostra o convite para cadastrar.
        val empresa = empresas.firstOrNull { it.padrao } ?: empresas.firstOrNull()
            ?: return comListas

        val hoje = hoje()
        return comListas.copy(
            prestadorId = empresa.id,
            emissao = hoje.formatarBr(),
            validade = hoje.maisDias(30).formatarBr(),
            itens = listOf(ItemForm(novaChave())),
            formaPagamento = empresa.formaPagamentoPadrao.orEmpty(),
            prazoPagamento = empresa.prazoPagamentoPadrao.orEmpty(),
            observacoes = empresa.observacoesPadrao.orEmpty()
        )
    }

    private fun ItemOrcamento.paraForm() = ItemForm(
        chave = novaChave(),
        descricao = descricao,
        quantidade = formatarQuantidade(quantidade),
        unidade = unidade,
        valor = formatarValor(valorUnitarioCentavos)
    )

    // ───────────────────────────── Campos ─────────────────────────────

    /**
     * Toda edição passa por aqui: ignora mudanças enquanto salva (campos
     * desabilitados) e limpa erro/aviso antigos, que já não descrevem o que está na tela.
     */
    private fun editar(bloco: (EditorOrcamentoUiState) -> EditorOrcamentoUiState) {
        _uiState.update { if (it.salvando) it else bloco(it).copy(erro = null, aviso = null) }
    }

    fun aoMudarEmissao(texto: String) = editar { it.copy(emissao = mascararData(texto)) }

    fun aoMudarValidade(texto: String) = editar { it.copy(validade = mascararData(texto)) }

    /**
     * Trocar a empresa de um orçamento NOVO troca também as condições (elas são
     * o padrão da empresa). Num orçamento já salvo as condições não mudam: foram
     * copiadas na emissão e podem ter sido editadas.
     */
    fun selecionarEmpresa(id: String) = editar { estado ->
        val empresa = estado.empresas.firstOrNull { it.id == id } ?: return@editar estado
        if (estado.id == null) {
            estado.copy(
                prestadorId = id,
                formaPagamento = empresa.formaPagamentoPadrao.orEmpty(),
                prazoPagamento = empresa.prazoPagamentoPadrao.orEmpty(),
                observacoes = empresa.observacoesPadrao.orEmpty()
            )
        } else {
            estado.copy(prestadorId = id)
        }
    }

    fun aoMudarDesconto(texto: String) = editar { it.copy(desconto = texto) }
    fun aoMudarFormaPagamento(texto: String) = editar { it.copy(formaPagamento = texto) }
    fun aoMudarPrazoPagamento(texto: String) = editar { it.copy(prazoPagamento = texto) }
    fun aoMudarObservacoes(texto: String) = editar { it.copy(observacoes = texto) }

    // ───────────────────────────── Itens ─────────────────────────────

    fun adicionarItem() = editar { it.copy(itens = it.itens + ItemForm(novaChave())) }

    fun removerItem(chave: Long) = editar { estado ->
        // A tabela nunca fica sem linhas: apagar a última deixa uma linha em branco.
        val restantes = estado.itens.filterNot { it.chave == chave }
        estado.copy(itens = restantes.ifEmpty { listOf(ItemForm(novaChave())) })
    }

    /** A tela devolve a linha inteira já com o campo trocado (`item.copy(...)`). */
    fun aoMudarItem(novo: ItemForm) = editar { estado ->
        estado.copy(itens = estado.itens.map { if (it.chave == novo.chave) novo else it })
    }

    // ───────────────────────────── Cliente ─────────────────────────────

    fun abrirSeletorCliente() = editar { it.copy(seletorClienteAberto = true) }

    fun fecharSeletorCliente() {
        _uiState.update { it.copy(seletorClienteAberto = false) }
    }

    fun selecionarCliente(id: String) = editar { it.copy(clienteId = id, seletorClienteAberto = false) }

    fun abrirNovoCliente() {
        _uiState.update { it.copy(seletorClienteAberto = false, novoCliente = FormularioCliente()) }
    }

    fun aoMudarNovoCliente(novo: FormularioCliente) {
        _uiState.update {
            if (it.novoCliente == null || it.novoCliente.salvando) it else it.copy(novoCliente = novo)
        }
    }

    fun fecharNovoCliente() {
        _uiState.update { if (it.novoCliente?.salvando == true) it else it.copy(novoCliente = null) }
    }

    /** Cadastro rápido: cria o cliente e já o deixa selecionado no orçamento. */
    fun salvarNovoCliente() {
        val form = _uiState.value.novoCliente ?: return
        if (!form.podeSalvar) return

        _uiState.update { it.copy(novoCliente = form.copy(salvando = true, erro = null)) }

        viewModelScope.launch {
            runCatching { clientes.criar(form.paraDados()) }
                .onSuccess { criado ->
                    _uiState.update {
                        it.copy(
                            clientes = (it.clientes + criado).sortedBy { c -> c.razaoSocial.lowercase() },
                            clienteId = criado.id,
                            novoCliente = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            novoCliente = form.copy(
                                salvando = false,
                                erro = "Não foi possível salvar o cliente. Verifique a conexão e tente de novo. " +
                                    e.message.orEmpty()
                            )
                        )
                    }
                }
        }
    }

    // ───────────────────────────── Salvar ─────────────────────────────

    fun salvarRascunho() = salvar(avisoPdf = false)

    /**
     * O PDF ainda não existe (é a última etapa do projeto). Por enquanto este
     * botão salva o orçamento e avisa; quando o gerador existir, é aqui que ele entra.
     */
    fun gerarPdf() = salvar(avisoPdf = true)

    private fun salvar(avisoPdf: Boolean) {
        val estado = _uiState.value
        if (!estado.podeSalvar) return

        val dados = when (val resultado = validar(estado)) {
            is Validacao.Erro -> {
                _uiState.update { it.copy(erro = resultado.mensagem, aviso = null) }
                return
            }
            is Validacao.Ok -> resultado.dados
        }

        _uiState.update { it.copy(salvando = true, erro = null, aviso = null) }

        viewModelScope.launch {
            runCatching {
                val idAtual = estado.id
                if (idAtual == null) {
                    val criado = orcamentos.criar(dados)
                    criado.id to criado.numero
                } else {
                    orcamentos.atualizar(idAtual, dados)
                    idAtual to estado.numero
                }
            }.onSuccess { (id, numero) ->
                _uiState.update {
                    it.copy(
                        salvando = false,
                        id = id,
                        numero = numero,
                        aviso = if (avisoPdf) {
                            "Orçamento salvo. A geração de PDF ainda não está disponível."
                        } else {
                            "Orçamento salvo"
                        }
                    )
                }
            }.onFailure { e ->
                // Tudo que o usuário digitou continua na tela (UI-UX, princípio 5).
                _uiState.update { it.copy(salvando = false, erro = traduzirErroSalvar(e)) }
            }
        }
    }

    private sealed interface Validacao {
        data class Ok(val dados: DadosOrcamento) : Validacao
        data class Erro(val mensagem: String) : Validacao
    }

    /** Converte o que está na tela em [DadosOrcamento] ou diz, em português, o que corrigir. */
    private fun validar(estado: EditorOrcamentoUiState): Validacao {
        val prestadorId = estado.prestadorId ?: return Validacao.Erro("Selecione a empresa.")
        val clienteId = estado.clienteId ?: return Validacao.Erro("Selecione um cliente.")

        val emissao = lerDataBr(estado.emissao)
            ?: return Validacao.Erro("Data de emissão inválida. Use dia/mês/ano, por exemplo 13/10/2026.")
        val validade = lerDataBr(estado.validade)
            ?: return Validacao.Erro("Data de validade inválida. Use dia/mês/ano, por exemplo 12/11/2026.")
        if (validade < emissao) {
            return Validacao.Erro("A validade não pode ser anterior à data de emissão.")
        }

        val itens = mutableListOf<ItemOrcamento>()
        estado.itens.forEachIndexed { indice, item ->
            if (item.vazio) return@forEachIndexed // linha em branco: ignora
            val numero = indice + 1
            if (item.descricao.isBlank()) {
                return Validacao.Erro("Item $numero: descreva o serviço.")
            }
            val quantidade = lerQuantidade(item.quantidade)
            if (quantidade == null || quantidade <= 0.0) {
                return Validacao.Erro("Item $numero: informe uma quantidade maior que zero.")
            }
            val valor = if (item.valor.isBlank()) 0L else lerCentavos(item.valor)
                ?: return Validacao.Erro("Item $numero: valor inválido. Exemplo: 1.100,00")
            itens += ItemOrcamento(
                descricao = item.descricao.trim(),
                quantidade = quantidade,
                unidade = item.unidade.trim().ifEmpty { "un" },
                valorUnitarioCentavos = valor
            )
        }

        val desconto = if (estado.desconto.isBlank()) 0L else lerCentavos(estado.desconto)
            ?: return Validacao.Erro("Desconto inválido. Exemplo: 100,00")
        if (desconto > itens.sumOf { it.totalCentavos }) {
            return Validacao.Erro("O desconto não pode ser maior que o subtotal.")
        }

        return Validacao.Ok(
            DadosOrcamento(
                dataEmissao = emissao,
                dataValidade = validade,
                prestadorId = prestadorId,
                clienteId = clienteId,
                descontoCentavos = desconto,
                formaPagamento = estado.formaPagamento.trim(),
                prazoPagamento = estado.prazoPagamento.trim(),
                observacoes = estado.observacoes.trim(),
                itens = itens
            )
        )
    }

    // ───────────────────────────── Erros ─────────────────────────────

    private fun traduzirErroCarga(e: Throwable): String =
        "Não foi possível carregar o orçamento. Verifique a conexão " +
            "(se o projeto ficou parado, o Supabase pode estar pausado). ${e.message.orEmpty()}"

    private fun traduzirErroSalvar(e: Throwable): String =
        "Não foi possível salvar o orçamento. Verifique a conexão e tente de novo. ${e.message.orEmpty()}"
}
