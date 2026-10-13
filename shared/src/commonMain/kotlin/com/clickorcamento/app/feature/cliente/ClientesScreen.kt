package com.clickorcamento.app.feature.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clickorcamento.app.core.design.Avatar
import com.clickorcamento.app.core.design.CaixaMensagem
import com.clickorcamento.app.core.design.Carregando
import com.clickorcamento.app.core.design.ConviteVazio
import com.clickorcamento.app.core.design.ErroComRetentativa
import com.clickorcamento.app.core.design.LarguraMinimaDesktop
import com.clickorcamento.app.core.format.iniciaisDe
import com.clickorcamento.app.domain.model.Cliente
import org.koin.compose.viewmodel.koinViewModel

/** Só conhece o ViewModel e repassa; o desenho está em [ClientesConteudo]. */
@Composable
fun ClientesScreen(
    aoAbrirOrcamento: (String) -> Unit,
    viewModel: ClientesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Recarrega toda vez que a tela volta ao primeiro plano (inclusive na 1ª vez):
    // o editor de orçamento pode ter criado um cliente novo enquanto estávamos fora.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.carregar() }

    ClientesConteudo(
        state = state,
        aoTentarDeNovo = viewModel::carregar,
        aoMudarBusca = viewModel::aoMudarBusca,
        aoNovo = viewModel::novoCliente,
        aoEditar = viewModel::editarCliente,
        aoMudarFormulario = viewModel::aoMudarFormulario,
        aoSalvar = viewModel::salvarFormulario,
        aoFecharFormulario = viewModel::fecharFormulario,
        aoPedirExclusao = viewModel::pedirExclusao,
        aoConfirmarExclusao = viewModel::confirmarExclusao,
        aoCancelarExclusao = viewModel::cancelarExclusao,
        aoAbrirOrcamento = aoAbrirOrcamento
    )
}

@Composable
private fun ClientesConteudo(
    state: ClientesUiState,
    aoTentarDeNovo: () -> Unit,
    aoMudarBusca: (String) -> Unit,
    aoNovo: () -> Unit,
    aoEditar: (Cliente) -> Unit,
    aoMudarFormulario: (FormularioCliente) -> Unit,
    aoSalvar: () -> Unit,
    aoFecharFormulario: () -> Unit,
    aoPedirExclusao: () -> Unit,
    aoConfirmarExclusao: () -> Unit,
    aoCancelarExclusao: () -> Unit,
    aoAbrirOrcamento: (String) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val largo = maxWidth >= LarguraMinimaDesktop
        val form = state.formulario

        // O formulário é o mesmo nos dois layouts; muda onde ele aparece.
        val formulario: @Composable () -> Unit = {
            if (form != null) {
                ClienteFormulario(
                    form = form,
                    orcamentosDoCliente = form.id?.let { state.orcamentosDe(it) }.orEmpty(),
                    largo = largo,
                    aoMudar = aoMudarFormulario,
                    aoSalvar = aoSalvar,
                    aoCancelar = aoFecharFormulario,
                    aoExcluir = aoPedirExclusao,
                    aoAbrirOrcamento = aoAbrirOrcamento
                )
            }
        }

        if (largo) {
            // Master-detail: lista à esquerda, formulário à direita (mockup, seção 2).
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.width(400.dp).fillMaxHeight()) {
                    ListaDeClientes(
                        state = state,
                        largo = true,
                        selecionadoId = form?.id,
                        aoTentarDeNovo = aoTentarDeNovo,
                        aoMudarBusca = aoMudarBusca,
                        aoNovo = aoNovo,
                        aoEditar = aoEditar
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                    if (form != null) {
                        // Cartão do detalhe: surface com borda fina e cantos 12.dp;
                        // largura limitada, formulário simples não estica (UI-UX, seção 6).
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .widthIn(max = 560.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(24.dp)
                        ) {
                            formulario()
                        }
                    } else {
                        DetalheVazio()
                    }
                }
            }
        } else if (form != null) {
            // Celular: o formulário ocupa a tela toda no lugar da lista.
            Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { formulario() }
        } else {
            Box(Modifier.fillMaxSize()) {
                ListaDeClientes(
                    state = state,
                    largo = false,
                    selecionadoId = null,
                    aoTentarDeNovo = aoTentarDeNovo,
                    aoMudarBusca = aoMudarBusca,
                    aoNovo = aoNovo,
                    aoEditar = aoEditar
                )
                // "Novo cliente" no celular: botão flutuante acima da barra inferior.
                if (state.clientes.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = aoNovo,
                        icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                        text = { Text("Novo cliente") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }

    state.clienteParaExcluir?.let { cliente ->
        AlertDialog(
            onDismissRequest = aoCancelarExclusao,
            title = { Text("Excluir ${cliente.razaoSocial}?") },
            text = { Text("O cliente será removido. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = aoConfirmarExclusao) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = aoCancelarExclusao) { Text("Cancelar") } }
        )
    }
}

// ───────────────────────────── Lista ─────────────────────────────

@Composable
private fun ListaDeClientes(
    state: ClientesUiState,
    largo: Boolean,
    selecionadoId: String?,
    aoTentarDeNovo: () -> Unit,
    aoMudarBusca: (String) -> Unit,
    aoNovo: () -> Unit,
    aoEditar: (Cliente) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        if (largo) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Clientes", style = MaterialTheme.typography.headlineSmall)
                if (state.clientes.isNotEmpty()) {
                    Text(
                        text = state.clientes.size.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                if (state.clientes.isNotEmpty()) {
                    // Botão da tela (um só de destaque). No mockup é contornado porque o
                    // painel da direita já traz o "Salvar" cheio.
                    OutlinedButton(onClick = aoNovo, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Novo cliente", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        state.aviso?.let {
            CaixaMensagem(texto = it, erro = false)
            Spacer(Modifier.height(12.dp))
        }

        when {
            state.mostrarCarregando -> Carregando()

            state.mostrarErro -> ErroComRetentativa(state.erro.orEmpty(), aoTentarDeNovo)

            state.mostrarVazio -> ConviteVazio(
                titulo = "Cadastre seu primeiro cliente",
                descricao = "Quem recebe os orçamentos. Basta o nome para começar; o resto você completa depois.",
                textoBotao = "Novo cliente",
                aoClicar = aoNovo
            )

            else -> {
                CampoBusca(state.busca, aoMudarBusca)
                Spacer(Modifier.height(12.dp))

                if (state.mostrarSemResultado) {
                    Text(
                        text = "Nenhum cliente encontrado para \"${state.busca.trim()}\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        // Respiro no fim para o botão flutuante não cobrir o último cliente.
                        contentPadding = PaddingValues(bottom = if (largo) 0.dp else 88.dp)
                    ) {
                        items(state.filtrados, key = { it.id }) { cliente ->
                            LinhaCliente(
                                cliente = cliente,
                                quantidadeOrcamentos = state.orcamentosDe(cliente.id).size,
                                selecionado = cliente.id == selecionadoId,
                                largo = largo,
                                aoClicar = { aoEditar(cliente) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoBusca(busca: String, aoMudar: (String) -> Unit) {
    OutlinedTextField(
        value = busca,
        onValueChange = aoMudar,
        placeholder = { Text("Buscar por nome ou CNPJ") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LinhaCliente(
    cliente: Cliente,
    quantidadeOrcamentos: Int,
    selecionado: Boolean,
    largo: Boolean,
    aoClicar: () -> Unit
) {
    val cores = MaterialTheme.colorScheme
    // "CNPJ · telefone", ignorando o que estiver em branco.
    val detalhe = listOf(cliente.cnpj, cliente.telefone).filter { it.isNotBlank() }.joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selecionado) cores.primaryContainer else cores.background)
            .clickable(onClick = aoClicar)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(iniciais = iniciaisDe(cliente.razaoSocial), forte = selecionado)
        Column(Modifier.weight(1f)) {
            Text(
                text = cliente.razaoSocial,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (selecionado) cores.onPrimaryContainer else cores.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (detalhe.isNotEmpty()) {
                Text(
                    text = detalhe,
                    style = MaterialTheme.typography.labelSmall,
                    color = cores.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (largo) {
            Text(
                text = if (quantidadeOrcamentos == 0) "—" else "$quantidadeOrcamentos orç.",
                style = MaterialTheme.typography.labelSmall,
                color = cores.onSurfaceVariant
            )
        } else {
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = cores.onSurfaceVariant)
        }
    }
}

/** Painel da direita quando nenhum cliente está aberto. */
@Composable
private fun DetalheVazio() {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Selecione um cliente", style = MaterialTheme.typography.titleSmall)
        Text(
            text = "Toque num cliente da lista para ver e editar os dados, ou crie um novo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
