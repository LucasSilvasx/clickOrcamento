package com.clickorcamento.app.feature.orcamento.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clickorcamento.app.core.design.Badge
import com.clickorcamento.app.core.design.CaixaMensagem
import com.clickorcamento.app.core.design.CampoSeletor
import com.clickorcamento.app.core.design.CampoTexto
import com.clickorcamento.app.core.design.Carregando
import com.clickorcamento.app.core.design.ConviteVazio
import com.clickorcamento.app.core.design.ErroComRetentativa
import com.clickorcamento.app.core.design.LarguraMinimaDesktop
import com.clickorcamento.app.core.format.formatarMoeda
import com.clickorcamento.app.core.format.lerDataBr
import com.clickorcamento.app.core.pdf.rememberEntregadorPdf
import com.clickorcamento.app.feature.cliente.FormularioCliente
import com.clickorcamento.app.feature.orcamento.BadgeStatus
import com.clickorcamento.app.feature.orcamento.textoNumero
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Largura máxima do editor no desktop (docs/UI-UX.md, seção 6). */
private val LarguraMaximaEditor = 1000.dp

/** Abaixo disso a tabela de itens fica apertada demais: os itens viram cartões, como no celular. */
private val LarguraMinimaTabela = 860.dp

/** Todas as ações que a tela dispara, juntas num lugar só (em vez de ~25 parâmetros soltos). */
private class AcoesEditor(
    val voltar: () -> Unit,
    val irParaConfiguracoes: () -> Unit,
    val tentarDeNovo: () -> Unit,
    val mudarEmissao: (String) -> Unit,
    val mudarValidade: (String) -> Unit,
    val selecionarEmpresa: (String) -> Unit,
    val abrirSeletorCliente: () -> Unit,
    val fecharSeletorCliente: () -> Unit,
    val selecionarCliente: (String) -> Unit,
    val abrirNovoCliente: () -> Unit,
    val mudarNovoCliente: (FormularioCliente) -> Unit,
    val fecharNovoCliente: () -> Unit,
    val salvarNovoCliente: () -> Unit,
    val adicionarItem: () -> Unit,
    val removerItem: (Long) -> Unit,
    val mudarItem: (ItemForm) -> Unit,
    val mudarDesconto: (String) -> Unit,
    val mudarFormaPagamento: (String) -> Unit,
    val mudarPrazoPagamento: (String) -> Unit,
    val mudarObservacoes: (String) -> Unit,
    val salvarRascunho: () -> Unit,
    val gerarPdf: () -> Unit
)

/**
 * Só conhece o ViewModel e repassa; o desenho está em [EditorConteudo].
 *
 * O `orcamentoId` vem da rota e entra no ViewModel via `parametersOf`: `null`
 * abre um orçamento novo. Esta tela é "cheia": no celular a casca esconde as
 * barras de cima e de baixo, e o editor desenha as próprias (seta de voltar e
 * barra fixa com Salvar / Gerar PDF).
 */
@Composable
fun EditorOrcamentoScreen(
    orcamentoId: String?,
    aoVoltar: () -> Unit,
    aoIrParaConfiguracoes: () -> Unit,
    viewModel: EditorOrcamentoViewModel = koinViewModel { parametersOf(orcamentoId) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val acoes = AcoesEditor(
        voltar = aoVoltar,
        irParaConfiguracoes = aoIrParaConfiguracoes,
        tentarDeNovo = viewModel::carregar,
        mudarEmissao = viewModel::aoMudarEmissao,
        mudarValidade = viewModel::aoMudarValidade,
        selecionarEmpresa = viewModel::selecionarEmpresa,
        abrirSeletorCliente = viewModel::abrirSeletorCliente,
        fecharSeletorCliente = viewModel::fecharSeletorCliente,
        selecionarCliente = viewModel::selecionarCliente,
        abrirNovoCliente = viewModel::abrirNovoCliente,
        mudarNovoCliente = viewModel::aoMudarNovoCliente,
        fecharNovoCliente = viewModel::fecharNovoCliente,
        salvarNovoCliente = viewModel::salvarNovoCliente,
        adicionarItem = viewModel::adicionarItem,
        removerItem = viewModel::removerItem,
        mudarItem = viewModel::aoMudarItem,
        mudarDesconto = viewModel::aoMudarDesconto,
        mudarFormaPagamento = viewModel::aoMudarFormaPagamento,
        mudarPrazoPagamento = viewModel::aoMudarPrazoPagamento,
        mudarObservacoes = viewModel::aoMudarObservacoes,
        salvarRascunho = viewModel::salvarRascunho,
        gerarPdf = viewModel::gerarPdf
    )

    // Quando o ViewModel termina um PDF, a tela entrega (salvar / compartilhar /
    // baixar, conforme a plataforma) e devolve o resultado para ele.
    val entregador = rememberEntregadorPdf()
    LaunchedEffect(state.pdfParaEntregar) {
        val pdf = state.pdfParaEntregar ?: return@LaunchedEffect
        runCatching { entregador.entregar(pdf.nomeArquivo, pdf.bytes) }
            .onSuccess(viewModel::aoEntregarPdf)
            .onFailure(viewModel::aoFalharEntregaPdf)
    }

    EditorConteudo(state, acoes)
}

@Composable
private fun EditorConteudo(state: EditorOrcamentoUiState, acoes: AcoesEditor) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val largo = maxWidth >= LarguraMinimaDesktop
        val tabela = maxWidth >= LarguraMinimaTabela

        if (largo) {
            EditorDesktop(state, acoes, tabela)
        } else {
            EditorCelular(state, acoes)
        }
    }

    if (state.seletorClienteAberto) {
        SeletorClienteDialogo(
            clientes = state.clientes,
            selecionadoId = state.clienteId,
            aoSelecionar = acoes.selecionarCliente,
            aoNovoCliente = acoes.abrirNovoCliente,
            aoFechar = acoes.fecharSeletorCliente
        )
    }
    state.novoCliente?.let { form ->
        NovoClienteDialogo(
            form = form,
            aoMudar = acoes.mudarNovoCliente,
            aoSalvar = acoes.salvarNovoCliente,
            aoFechar = acoes.fecharNovoCliente
        )
    }
}

// ───────────────────────────── Desktop ─────────────────────────────

@Composable
private fun EditorDesktop(state: EditorOrcamentoUiState, acoes: AcoesEditor, tabela: Boolean) {
    val cores = MaterialTheme.colorScheme

    // Rola a tela inteira; o conteúdo fica centralizado com largura máxima ~1000.dp.
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = LarguraMaximaEditor).fillMaxWidth()) {
            // Cabeçalho: voltar, título, badge do número e status à direita.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = acoes.voltar) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                }
                Text(state.titulo, style = MaterialTheme.typography.headlineSmall)
                Badge(textoNumero(state.numero))
                Spacer(Modifier.weight(1f))
                BadgeStatus(state.status)
            }
            Spacer(Modifier.height(16.dp))

            if (state.pronto) {
                // O cartão do mockup: surface, borda fina, cantos 12.dp.
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cores.surface,
                    border = BorderStroke(1.dp, cores.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Formulario(state, acoes, largo = true, tabela = tabela)
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            BotoesDeAcao(state, acoes, rotuloSalvar = "Salvar rascunho", peso = false)
                        }
                    }
                }
            } else {
                EstadoSemFormulario(state, acoes)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ───────────────────────────── Celular ─────────────────────────────

@Composable
private fun EditorCelular(state: EditorOrcamentoUiState, acoes: AcoesEditor) {
    val cores = MaterialTheme.colorScheme

    // imePadding: quando o teclado abre, a barra de ações sobe junto em vez de ficar escondida.
    Column(Modifier.fillMaxSize().background(cores.background).imePadding()) {
        // Barra de cima: seta de voltar, título e número (mockup 4b). Sem barra inferior de navegação.
        Surface(color = cores.surface) {
            Column {
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = acoes.voltar) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                    }
                    Text(
                        text = state.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Badge(textoNumero(state.numero))
                    if (state.id != null) {
                        Spacer(Modifier.width(6.dp))
                        BadgeStatus(state.status)
                    }
                }
                HorizontalDivider(color = cores.outlineVariant)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (state.pronto) {
                Formulario(state, acoes, largo = false, tabela = false)
            } else {
                EstadoSemFormulario(state, acoes)
            }
        }

        // Barra fixa de ações embaixo (só quando há formulário).
        if (state.pronto) {
            Surface(color = cores.surface) {
                Column {
                    HorizontalDivider(color = cores.outlineVariant)
                    Row(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BotoesDeAcao(state, acoes, rotuloSalvar = "Salvar", peso = true)
                    }
                }
            }
        }
    }
}

// ───────────────────────────── Estados sem formulário ─────────────────────────────

/** Carregando, erro de carga ou primeiro acesso sem empresa. */
@Composable
private fun EstadoSemFormulario(state: EditorOrcamentoUiState, acoes: AcoesEditor) {
    when {
        state.carregando -> Carregando()
        state.erroCarga != null -> ErroComRetentativa(state.erroCarga, acoes.tentarDeNovo)
        state.semEmpresa -> ConviteVazio(
            titulo = "Cadastre sua empresa para começar",
            descricao = "O orçamento leva os dados e as condições de pagamento da sua empresa.",
            textoBotao = "Ir para Configurações",
            aoClicar = acoes.irParaConfiguracoes
        )
    }
}

// ───────────────────────────── Formulário ─────────────────────────────

@Composable
private fun Formulario(
    state: EditorOrcamentoUiState,
    acoes: AcoesEditor,
    largo: Boolean,
    tabela: Boolean
) {
    val habilitado = !state.salvando

    // Linha de cabeçalho do orçamento: empresa (se houver mais de uma), datas e cliente.
    if (largo) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            if (state.mostrarSeletorEmpresa) {
                CampoEmpresa(state, acoes, Modifier.weight(1.2f))
            }
            CampoData(state.emissao, "Emissão", acoes.mudarEmissao, habilitado, Modifier.weight(1f))
            CampoData(state.validade, "Validade", acoes.mudarValidade, habilitado, Modifier.weight(1f))
            CampoCliente(state, acoes, Modifier.weight(1.6f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.mostrarSeletorEmpresa) CampoEmpresa(state, acoes, Modifier.fillMaxWidth())
            CampoCliente(state, acoes, Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoData(state.emissao, "Emissão", acoes.mudarEmissao, habilitado, Modifier.weight(1f))
                CampoData(state.validade, "Validade", acoes.mudarValidade, habilitado, Modifier.weight(1f))
            }
        }
    }

    Spacer(Modifier.height(22.dp))
    Text("Itens do serviço", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(8.dp))

    Column(verticalArrangement = Arrangement.spacedBy(if (tabela) 8.dp else 10.dp)) {
        state.itens.forEachIndexed { indice, item ->
            // `key` faz o Compose seguir a linha certa quando outra é removida.
            androidx.compose.runtime.key(item.chave) {
                if (tabela) {
                    LinhaItemTabela(indice + 1, item, habilitado, acoes)
                } else {
                    CartaoItem(indice + 1, item, habilitado, acoes)
                }
            }
        }
    }
    TextButton(onClick = acoes.adicionarItem, enabled = habilitado) {
        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Adicionar item")
    }

    Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        CaixaTotais(state, acoes, largo)
    }

    Spacer(Modifier.height(20.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Spacer(Modifier.height(16.dp))
    Condicoes(state, acoes, largo)

    state.erro?.let {
        Spacer(Modifier.height(16.dp))
        CaixaMensagem(texto = it)
    }
    state.aviso?.let {
        Spacer(Modifier.height(16.dp))
        CaixaMensagem(texto = it, erro = false)
    }
}

@Composable
private fun CampoEmpresa(state: EditorOrcamentoUiState, acoes: AcoesEditor, modifier: Modifier) {
    var aberto by remember { mutableStateOf(false) }
    CampoSeletor(
        valor = state.empresaSelecionada?.apelido.orEmpty(),
        rotulo = "Empresa",
        aoClicar = { aberto = true },
        modifier = modifier,
        habilitado = !state.salvando
    ) {
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            state.empresas.forEach { empresa ->
                DropdownMenuItem(
                    text = { Text(empresa.apelido) },
                    onClick = {
                        aberto = false
                        acoes.selecionarEmpresa(empresa.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun CampoCliente(state: EditorOrcamentoUiState, acoes: AcoesEditor, modifier: Modifier) {
    CampoSeletor(
        valor = state.clienteSelecionado?.razaoSocial.orEmpty(),
        rotulo = "Cliente",
        aoClicar = acoes.abrirSeletorCliente,
        modifier = modifier,
        habilitado = !state.salvando,
        // Cliente é o único campo que o usuário precisa preencher: a borda chama a atenção até escolher.
        destaque = state.clienteId == null
    )
}

@Composable
private fun CampoData(
    valor: String,
    rotulo: String,
    aoMudar: (String) -> Unit,
    habilitado: Boolean,
    modifier: Modifier
) {
    CampoTexto(
        valor = valor,
        rotulo = rotulo,
        aoMudar = aoMudar,
        modifier = modifier,
        habilitado = habilitado,
        teclado = KeyboardType.Number,
        // Só marca erro quando a data já tem os 10 caracteres e mesmo assim não existe (ex.: 31/02).
        erro = valor.length == 10 && lerDataBr(valor) == null
    )
}

// ───────────────────────────── Itens ─────────────────────────────

/** Linha da tabela (desktop): # | Descrição | Qtd | Un | Valor unit. | Total | lixeira. */
@Composable
private fun LinhaItemTabela(numero: Int, item: ItemForm, habilitado: Boolean, acoes: AcoesEditor) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = numero.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )
        CampoTexto(
            valor = item.descricao,
            rotulo = "Descrição",
            habilitado = habilitado,
            modifier = Modifier.weight(1f),
            aoMudar = { acoes.mudarItem(item.copy(descricao = it)) }
        )
        CampoTexto(
            valor = item.quantidade,
            rotulo = "Qtd",
            habilitado = habilitado,
            teclado = KeyboardType.Decimal,
            alinharADireita = true,
            erro = item.quantidadeInvalida,
            modifier = Modifier.width(84.dp),
            aoMudar = { acoes.mudarItem(item.copy(quantidade = it)) }
        )
        CampoTexto(
            valor = item.unidade,
            rotulo = "Un",
            habilitado = habilitado,
            modifier = Modifier.width(80.dp),
            aoMudar = { acoes.mudarItem(item.copy(unidade = it)) }
        )
        CampoTexto(
            valor = item.valor,
            rotulo = "Valor unit.",
            habilitado = habilitado,
            teclado = KeyboardType.Decimal,
            alinharADireita = true,
            erro = item.valorInvalido,
            modifier = Modifier.width(130.dp),
            aoMudar = { acoes.mudarItem(item.copy(valor = it)) }
        )
        // Total da linha: calculado, não editável, alinhado à direita.
        Text(
            text = formatarMoeda(item.totalCentavos),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(112.dp)
        )
        IconButton(onClick = { acoes.removerItem(item.chave) }, enabled = habilitado) {
            Icon(Icons.Outlined.Delete, contentDescription = "Remover item $numero")
        }
    }
}

/** Item como cartão (celular e janelas estreitas): descrição em cima; qtd, un e valor numa linha; total à direita. */
@Composable
private fun CartaoItem(numero: Int, item: ItemForm, habilitado: Boolean, acoes: AcoesEditor) {
    val cores = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cores.surface,
        border = BorderStroke(1.dp, cores.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = numero.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = cores.onSurfaceVariant,
                    modifier = Modifier.width(22.dp)
                )
                CampoTexto(
                    valor = item.descricao,
                    rotulo = "Descrição",
                    habilitado = habilitado,
                    modifier = Modifier.weight(1f),
                    aoMudar = { acoes.mudarItem(item.copy(descricao = it)) }
                )
                IconButton(onClick = { acoes.removerItem(item.chave) }, enabled = habilitado) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remover item $numero")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto(
                    valor = item.quantidade,
                    rotulo = "Qtd",
                    habilitado = habilitado,
                    teclado = KeyboardType.Decimal,
                    alinharADireita = true,
                    erro = item.quantidadeInvalida,
                    modifier = Modifier.weight(0.8f),
                    aoMudar = { acoes.mudarItem(item.copy(quantidade = it)) }
                )
                CampoTexto(
                    valor = item.unidade,
                    rotulo = "Un",
                    habilitado = habilitado,
                    modifier = Modifier.weight(0.7f),
                    aoMudar = { acoes.mudarItem(item.copy(unidade = it)) }
                )
                CampoTexto(
                    valor = item.valor,
                    rotulo = "Valor unit.",
                    habilitado = habilitado,
                    teclado = KeyboardType.Decimal,
                    alinharADireita = true,
                    erro = item.valorInvalido,
                    modifier = Modifier.weight(1.2f),
                    aoMudar = { acoes.mudarItem(item.copy(valor = it)) }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.bodyMedium, color = cores.onSurfaceVariant)
                Text(
                    text = formatarMoeda(item.totalCentavos),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ───────────────────────────── Totais ─────────────────────────────

/** Caixa `surfaceVariant`: subtotal, desconto (editável) e total geral em destaque. */
@Composable
private fun CaixaTotais(state: EditorOrcamentoUiState, acoes: AcoesEditor, largo: Boolean) {
    val cores = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .then(if (largo) Modifier.width(320.dp) else Modifier.fillMaxWidth())
            .clip(RoundedCornerShape(12.dp))
            .background(cores.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Subtotal", color = cores.onSurfaceVariant)
            Text(formatarMoeda(state.subtotalCentavos), color = cores.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Desconto", color = cores.onSurfaceVariant, modifier = Modifier.weight(1f))
            CampoTexto(
                valor = state.desconto,
                rotulo = "Valor",
                habilitado = !state.salvando,
                teclado = KeyboardType.Decimal,
                alinharADireita = true,
                prefixo = "R$ ",
                erro = state.descontoInvalido,
                modifier = Modifier.width(170.dp),
                aoMudar = acoes.mudarDesconto
            )
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = cores.outline)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total geral", fontWeight = FontWeight.SemiBold)
            Text(
                text = formatarMoeda(state.totalGeralCentavos),
                fontSize = if (largo) 24.sp else 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ───────────────────────────── Condições ─────────────────────────────

@Composable
private fun Condicoes(state: EditorOrcamentoUiState, acoes: AcoesEditor, largo: Boolean) {
    val habilitado = !state.salvando

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            Icons.Outlined.Tune,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Condições, preenchidas do padrão da empresa e editáveis",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(12.dp))

    if (largo) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CampoTexto(
                valor = state.formaPagamento,
                rotulo = "Forma de pagamento",
                habilitado = habilitado,
                modifier = Modifier.weight(1f),
                aoMudar = acoes.mudarFormaPagamento
            )
            CampoTexto(
                valor = state.prazoPagamento,
                rotulo = "Prazo de pagamento",
                habilitado = habilitado,
                modifier = Modifier.weight(1f),
                aoMudar = acoes.mudarPrazoPagamento
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CampoTexto(
                valor = state.formaPagamento,
                rotulo = "Forma de pagamento",
                habilitado = habilitado,
                aoMudar = acoes.mudarFormaPagamento
            )
            CampoTexto(
                valor = state.prazoPagamento,
                rotulo = "Prazo de pagamento",
                habilitado = habilitado,
                aoMudar = acoes.mudarPrazoPagamento
            )
        }
    }
    Spacer(Modifier.height(if (largo) 14.dp else 10.dp))
    CampoTexto(
        valor = state.observacoes,
        rotulo = "Observações (dados bancários, Pix)",
        habilitado = habilitado,
        linhasMinimas = 3,
        ultimo = true,
        aoMudar = acoes.mudarObservacoes
    )
}

// ───────────────────────────── Botões ─────────────────────────────

/**
 * "Salvar rascunho" (contornado) + "Gerar PDF" (o único botão de destaque da tela).
 * [peso] = cada botão divide a largura da barra fixa do celular.
 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.BotoesDeAcao(
    state: EditorOrcamentoUiState,
    acoes: AcoesEditor,
    rotuloSalvar: String,
    peso: Boolean
) {
    val larguraBotao = if (peso) Modifier.weight(1f) else Modifier
    val forma = RoundedCornerShape(12.dp)

    OutlinedButton(
        onClick = acoes.salvarRascunho,
        enabled = state.podeSalvar,
        shape = forma,
        modifier = larguraBotao.height(50.dp)
    ) {
        if (state.salvando && !state.gerandoPdf) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(rotuloSalvar, style = MaterialTheme.typography.labelLarge)
        }
    }
    Button(
        onClick = acoes.gerarPdf,
        enabled = state.podeGerarPdf,
        shape = forma,
        modifier = larguraBotao.height(50.dp)
    ) {
        if (state.gerandoPdf) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Gerar PDF", style = MaterialTheme.typography.labelLarge)
        }
    }
}
