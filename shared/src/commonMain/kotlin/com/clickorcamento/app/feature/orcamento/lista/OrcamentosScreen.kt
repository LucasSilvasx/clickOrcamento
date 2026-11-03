package com.clickorcamento.app.feature.orcamento.lista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clickorcamento.app.core.design.Badge
import com.clickorcamento.app.core.design.CaixaMensagem
import com.clickorcamento.app.core.design.Carregando
import com.clickorcamento.app.core.design.ConviteVazio
import com.clickorcamento.app.core.design.ErroComRetentativa
import com.clickorcamento.app.core.design.LarguraMinimaDesktop
import com.clickorcamento.app.core.format.formatarBr
import com.clickorcamento.app.core.format.formatarMoeda
import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.domain.model.StatusOrcamento
import com.clickorcamento.app.feature.orcamento.BadgeStatus
import com.clickorcamento.app.feature.orcamento.textoNumero
import org.koin.compose.viewmodel.koinViewModel

/** Largura máxima da tabela no desktop: em monitor grande ela não deve esticar até as bordas. */
private val LarguraMaximaLista = 1100.dp

/** Só conhece o ViewModel e repassa; o desenho está em [OrcamentosConteudo]. */
@Composable
fun OrcamentosScreen(
    aoNovo: () -> Unit,
    aoAbrir: (String) -> Unit,
    viewModel: OrcamentosViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarrega toda vez que a tela volta ao primeiro plano (inclusive na 1ª vez):
    // ao voltar do editor, o orçamento salvo já aparece na lista.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.carregar() }

    OrcamentosConteudo(
        state = state,
        aoTentarDeNovo = viewModel::carregar,
        aoMudarBusca = viewModel::aoMudarBusca,
        aoMudarFiltro = viewModel::aoMudarFiltro,
        aoNovo = aoNovo,
        aoAbrir = aoAbrir,
        aoDuplicar = viewModel::duplicar,
        aoAlterarStatus = viewModel::alterarStatus,
        aoExcluir = viewModel::pedirExclusao,
        aoConfirmarExclusao = viewModel::confirmarExclusao,
        aoCancelarExclusao = viewModel::cancelarExclusao
    )
}

@Composable
private fun OrcamentosConteudo(
    state: OrcamentosUiState,
    aoTentarDeNovo: () -> Unit,
    aoMudarBusca: (String) -> Unit,
    aoMudarFiltro: (StatusOrcamento?) -> Unit,
    aoNovo: () -> Unit,
    aoAbrir: (String) -> Unit,
    aoDuplicar: (OrcamentoResumo) -> Unit,
    aoAlterarStatus: (OrcamentoResumo, StatusOrcamento) -> Unit,
    aoExcluir: (OrcamentoResumo) -> Unit,
    aoConfirmarExclusao: () -> Unit,
    aoCancelarExclusao: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val largo = maxWidth >= LarguraMinimaDesktop

        Box(Modifier.fillMaxSize()) {
            Column(Modifier.widthIn(max = LarguraMaximaLista).fillMaxSize()) {
                if (largo) {
                    // Cabeçalho do desktop: título, contagem e o botão principal da tela.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Orçamentos", style = MaterialTheme.typography.headlineSmall)
                        if (state.orcamentos.isNotEmpty()) {
                            Text(
                                text = state.orcamentos.size.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = aoNovo,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Novo orçamento", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                state.aviso?.let {
                    CaixaMensagem(texto = it, erro = false)
                    Spacer(Modifier.height(12.dp))
                }
                state.erroAcao?.let {
                    CaixaMensagem(texto = it)
                    Spacer(Modifier.height(12.dp))
                }

                when {
                    state.mostrarCarregando -> Carregando()

                    state.mostrarErro -> ErroComRetentativa(state.erro.orEmpty(), aoTentarDeNovo)

                    state.mostrarVazio -> ConviteVazio(
                        titulo = "Crie seu primeiro orçamento",
                        descricao = "O número é gerado sozinho e as condições de pagamento já vêm da sua empresa.",
                        textoBotao = "Novo orçamento",
                        aoClicar = aoNovo
                    )

                    else -> {
                        Filtros(state, aoMudarBusca, aoMudarFiltro)
                        Spacer(Modifier.height(12.dp))

                        if (state.mostrarSemResultado) {
                            Text(
                                text = "Nenhum orçamento encontrado com esses filtros.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (largo) {
                            TabelaOrcamentos(state, aoAbrir, aoDuplicar, aoAlterarStatus, aoExcluir)
                        } else {
                            CartoesOrcamentos(state, aoAbrir, aoDuplicar, aoAlterarStatus, aoExcluir)
                        }
                    }
                }
            }

            // "Novo" no celular: botão flutuante acima da barra inferior.
            if (!largo && state.orcamentos.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = aoNovo,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Novo") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }

    state.orcamentoParaExcluir?.let { orcamento ->
        AlertDialog(
            onDismissRequest = aoCancelarExclusao,
            title = { Text("Excluir ${textoNumero(orcamento.numero)}?") },
            text = {
                Text(
                    "O orçamento de ${orcamento.clienteRazaoSocial} e todos os itens dele serão " +
                        "removidos. Essa ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(onClick = aoConfirmarExclusao) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = aoCancelarExclusao) { Text("Cancelar") } }
        )
    }
}

// --- Busca e filtro ---

@Composable
private fun Filtros(
    state: OrcamentosUiState,
    aoMudarBusca: (String) -> Unit,
    aoMudarFiltro: (StatusOrcamento?) -> Unit
) {
    OutlinedTextField(
        value = state.busca,
        onValueChange = aoMudarBusca,
        placeholder = { Text("Buscar por cliente ou número") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))

    // Chips de status; rolam na horizontal se a tela for estreita.
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipStatus("Todos", selecionado = state.filtro == null) { aoMudarFiltro(null) }
        StatusOrcamento.entries.forEach { status ->
            ChipStatus(status.rotulo, selecionado = state.filtro == status) { aoMudarFiltro(status) }
        }
    }
}

@Composable
private fun ChipStatus(texto: String, selecionado: Boolean, aoClicar: () -> Unit) {
    FilterChip(
        selected = selecionado,
        onClick = aoClicar,
        label = { Text(texto) },
        shape = RoundedCornerShape(8.dp)
    )
}

// --- Desktop: tabela ---

private val LarguraNumero = 96.dp
private val LarguraData = 110.dp
private val LarguraStatus = 110.dp
private val LarguraTotal = 130.dp
private val LarguraAcoes = 48.dp

@Composable
private fun TabelaOrcamentos(
    state: OrcamentosUiState,
    aoAbrir: (String) -> Unit,
    aoDuplicar: (OrcamentoResumo) -> Unit,
    aoAlterarStatus: (OrcamentoResumo, StatusOrcamento) -> Unit,
    aoExcluir: (OrcamentoResumo) -> Unit
) {
    val cores = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cores.surface,
        border = BorderStroke(1.dp, cores.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Cabeçalho das colunas.
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CabecalhoColuna("Nº", Modifier.width(LarguraNumero))
                CabecalhoColuna("Cliente", Modifier.weight(1f))
                CabecalhoColuna("Emissão", Modifier.width(LarguraData))
                CabecalhoColuna("Status", Modifier.width(LarguraStatus))
                CabecalhoColuna("Total", Modifier.width(LarguraTotal), alinharADireita = true)
                Spacer(Modifier.width(LarguraAcoes))
            }
            HorizontalDivider(color = cores.outlineVariant)

            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(state.filtrados, key = { it.id }) { orcamento ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aoAbrir(orcamento.id) }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.width(LarguraNumero)) { Badge(textoNumero(orcamento.numero)) }
                        Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                            Text(
                                text = orcamento.clienteRazaoSocial,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (state.mostrarEmpresa) {
                                Text(
                                    text = orcamento.prestadorNome,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cores.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = orcamento.dataEmissao.formatarBr(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = cores.onSurfaceVariant,
                            modifier = Modifier.width(LarguraData)
                        )
                        Box(Modifier.width(LarguraStatus)) { BadgeStatus(orcamento.status) }
                        Text(
                            text = formatarMoeda(orcamento.totalGeralCentavos),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(LarguraTotal)
                        )
                        Box(Modifier.width(LarguraAcoes), contentAlignment = Alignment.Center) {
                            MenuOrcamento(
                                orcamento = orcamento,
                                habilitado = !state.ocupado,
                                aoAbrir = { aoAbrir(orcamento.id) },
                                aoDuplicar = { aoDuplicar(orcamento) },
                                aoAlterarStatus = { aoAlterarStatus(orcamento, it) },
                                aoExcluir = { aoExcluir(orcamento) }
                            )
                        }
                    }
                    HorizontalDivider(color = cores.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun CabecalhoColuna(texto: String, modifier: Modifier, alinharADireita: Boolean = false) {
    Text(
        text = texto,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = if (alinharADireita) TextAlign.End else TextAlign.Start
    )
}

// --- Celular: cartões ---

@Composable
private fun CartoesOrcamentos(
    state: OrcamentosUiState,
    aoAbrir: (String) -> Unit,
    aoDuplicar: (OrcamentoResumo) -> Unit,
    aoAlterarStatus: (OrcamentoResumo, StatusOrcamento) -> Unit,
    aoExcluir: (OrcamentoResumo) -> Unit
) {
    val cores = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        // Respiro no fim para o botão flutuante não cobrir o último cartão.
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        items(state.filtrados, key = { it.id }) { orcamento ->
            Surface(
                onClick = { aoAbrir(orcamento.id) },
                shape = RoundedCornerShape(12.dp),
                color = cores.surface,
                border = BorderStroke(1.dp, cores.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(start = 14.dp, top = 4.dp, bottom = 12.dp, end = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(textoNumero(orcamento.numero))
                        Spacer(Modifier.width(8.dp))
                        BadgeStatus(orcamento.status)
                        Spacer(Modifier.weight(1f))
                        MenuOrcamento(
                            orcamento = orcamento,
                            habilitado = !state.ocupado,
                            aoAbrir = { aoAbrir(orcamento.id) },
                            aoDuplicar = { aoDuplicar(orcamento) },
                            aoAlterarStatus = { aoAlterarStatus(orcamento, it) },
                            aoExcluir = { aoExcluir(orcamento) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = orcamento.clienteRazaoSocial,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val legenda = buildString {
                                append(orcamento.dataEmissao.formatarBr())
                                if (state.mostrarEmpresa) append(" · ${orcamento.prestadorNome}")
                            }
                            Text(
                                text = legenda,
                                style = MaterialTheme.typography.labelSmall,
                                color = cores.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatarMoeda(orcamento.totalGeralCentavos),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// --- Menu da linha ---

/** Menu "três pontinhos": abrir, duplicar, mudar status e excluir. */
@Composable
private fun MenuOrcamento(
    orcamento: OrcamentoResumo,
    habilitado: Boolean,
    aoAbrir: () -> Unit,
    aoDuplicar: () -> Unit,
    aoAlterarStatus: (StatusOrcamento) -> Unit,
    aoExcluir: () -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    val erro = MaterialTheme.colorScheme.error

    Box {
        IconButton(onClick = { aberto = true }, enabled = habilitado) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Ações de ${textoNumero(orcamento.numero)}")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            DropdownMenuItem(
                text = { Text("Abrir") },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = { aberto = false; aoAbrir() }
            )
            DropdownMenuItem(
                text = { Text("Duplicar") },
                leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                onClick = { aberto = false; aoDuplicar() }
            )
            HorizontalDivider()
            // Só mostra os status diferentes do atual.
            StatusOrcamento.entries.filter { it != orcamento.status }.forEach { status ->
                DropdownMenuItem(
                    text = { Text("Marcar como ${status.rotulo.lowercase()}") },
                    onClick = { aberto = false; aoAlterarStatus(status) }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Excluir", color = erro) },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = erro) },
                onClick = { aberto = false; aoExcluir() }
            )
        }
    }
}
