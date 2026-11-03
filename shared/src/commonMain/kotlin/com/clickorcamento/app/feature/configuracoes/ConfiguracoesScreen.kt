package com.clickorcamento.app.feature.configuracoes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clickorcamento.app.core.format.formatarCpfCnpj
import com.clickorcamento.app.domain.model.Prestador
import org.koin.compose.viewmodel.koinViewModel

/** Largura máxima do conteúdo: formulários e listas não devem esticar na tela inteira. */
private val LarguraMaxima = 780.dp

/** Formulário simples: mais estreito que a lista. */
private val LarguraFormulario = 560.dp

/** Só conhece o ViewModel e repassa; o desenho está em [ConfiguracoesConteudo]. */
@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracoesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ConfiguracoesConteudo(
        state = state,
        onTentarDeNovo = viewModel::carregar,
        onSair = viewModel::sair,
        onNovaEmpresa = viewModel::novaEmpresa,
        onEditar = viewModel::editarEmpresa,
        onDefinirPadrao = viewModel::definirComoPadrao,
        onExcluir = viewModel::pedirExclusao,
        onConfirmarExclusao = viewModel::confirmarExclusao,
        onCancelarExclusao = viewModel::cancelarExclusao,
        onMudarFormulario = viewModel::aoMudarFormulario,
        onSalvarFormulario = viewModel::salvarFormulario,
        onFecharFormulario = viewModel::fecharFormulario
    )
}

@Composable
private fun ConfiguracoesConteudo(
    state: ConfiguracoesUiState,
    onTentarDeNovo: () -> Unit,
    onSair: () -> Unit,
    onNovaEmpresa: () -> Unit,
    onEditar: (Prestador) -> Unit,
    onDefinirPadrao: (Prestador) -> Unit,
    onExcluir: (Prestador) -> Unit,
    onConfirmarExclusao: () -> Unit,
    onCancelarExclusao: () -> Unit,
    onMudarFormulario: (FormularioEmpresa) -> Unit,
    onSalvarFormulario: () -> Unit,
    onFecharFormulario: () -> Unit
) {
    val formulario = state.formulario

    // verticalScroll: no celular (ou janela baixa) o conteúdo passa da tela.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (formulario != null) {
            // Modo formulário: a lista some e o formulário ocupa o lugar dela
            // (mesma ideia da etapa "Confirme seu e-mail" do login: outro estado
            // da mesma tela, sem navegação).
            Column(Modifier.widthIn(max = LarguraFormulario).fillMaxWidth()) {
                EmpresaFormulario(
                    form = formulario,
                    aoMudar = onMudarFormulario,
                    aoSalvar = onSalvarFormulario,
                    aoCancelar = onFecharFormulario
                )
            }
        } else {
            Column(Modifier.widthIn(max = LarguraMaxima).fillMaxWidth()) {
                SecaoEmpresas(
                    state = state,
                    onTentarDeNovo = onTentarDeNovo,
                    onNovaEmpresa = onNovaEmpresa,
                    onEditar = onEditar,
                    onDefinirPadrao = onDefinirPadrao,
                    onExcluir = onExcluir
                )

                Spacer(Modifier.height(32.dp))

                // A seção Conta fica fora do `when` das empresas de propósito:
                // mesmo com erro de carga, o "Sair" continua acessível.
                SecaoConta(email = state.emailUsuario, onSair = onSair)
            }
        }
    }

    state.empresaParaExcluir?.let { empresa ->
        AlertDialog(
            onDismissRequest = onCancelarExclusao,
            title = { Text("Excluir ${empresa.apelido}?") },
            text = { Text("A empresa será removida. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = onConfirmarExclusao) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelarExclusao) { Text("Cancelar") }
            }
        )
    }
}

// ───────────────────────────── Empresas ─────────────────────────────

@Composable
private fun SecaoEmpresas(
    state: ConfiguracoesUiState,
    onTentarDeNovo: () -> Unit,
    onNovaEmpresa: () -> Unit,
    onEditar: (Prestador) -> Unit,
    onDefinirPadrao: (Prestador) -> Unit,
    onExcluir: (Prestador) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Empresas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        // Botão principal da tela. No estado vazio quem convida é o próprio
        // aviso (mais abaixo), então aqui só aparece quando já há lista.
        if (state.mostrarBotaoNova) {
            Button(
                onClick = onNovaEmpresa,
                enabled = !state.ocupado,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.padding(start = 6.dp))
                Text("Nova empresa", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = "Quem presta o serviço. A empresa padrão já vem escolhida em cada orçamento novo.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))

    // Resultado da última ação, sobre a lista (que continua visível).
    state.aviso?.let {
        CaixaMensagem(
            texto = it,
            fundo = MaterialTheme.colorScheme.primaryContainer,
            cor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.height(12.dp))
    }
    state.erroAcao?.let {
        CaixaMensagem(
            texto = it,
            fundo = MaterialTheme.colorScheme.errorContainer,
            cor = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.height(12.dp))
    }

    when {
        state.mostrarCarregando -> Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        state.mostrarErro -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CaixaMensagem(
                texto = state.erro.orEmpty(),
                fundo = MaterialTheme.colorScheme.errorContainer,
                cor = MaterialTheme.colorScheme.onErrorContainer
            )
            OutlinedButton(onClick = onTentarDeNovo, shape = RoundedCornerShape(12.dp)) {
                Text("Tentar de novo")
            }
        }

        // Convite do primeiro acesso: título, uma linha explicando e a ação.
        state.mostrarVazio -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Cadastre sua empresa para começar", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Os dados dela aparecem no cabeçalho de cada orçamento e do PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onNovaEmpresa,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text("Cadastrar empresa", style = MaterialTheme.typography.labelLarge)
            }
        }

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.empresas.forEach { empresa ->
                CartaoEmpresa(
                    empresa = empresa,
                    habilitado = !state.ocupado,
                    onEditar = { onEditar(empresa) },
                    onDefinirPadrao = { onDefinirPadrao(empresa) },
                    onExcluir = { onExcluir(empresa) }
                )
            }
        }
    }
}

@Composable
private fun CartaoEmpresa(
    empresa: Prestador,
    habilitado: Boolean,
    onEditar: () -> Unit,
    onDefinirPadrao: () -> Unit,
    onExcluir: () -> Unit
) {
    val cores = MaterialTheme.colorScheme

    // Surface com onClick: tocar no cartão abre a edição.
    Surface(
        onClick = onEditar,
        enabled = habilitado,
        shape = RoundedCornerShape(12.dp),
        color = cores.surface,
        // A empresa padrão ganha borda na cor principal para se destacar.
        border = BorderStroke(
            width = if (empresa.padrao) 1.5.dp else 1.dp,
            color = if (empresa.padrao) cores.primary else cores.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                tint = if (empresa.padrao) cores.primary else cores.onSurfaceVariant
            )
            // weight(1f) + ellipsis: nomes longos não empurram nada para fora.
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = empresa.apelido,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (empresa.padrao) Badge("Padrão")
                }
                Text(
                    text = empresa.nomeRazaoSocial,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cores.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!empresa.cpfCnpj.isNullOrBlank()) {
                    Text(
                        text = formatarCpfCnpj(empresa.cpfCnpj),
                        style = MaterialTheme.typography.labelSmall,
                        color = cores.onSurfaceVariant
                    )
                }
            }
            MenuEmpresa(
                empresa = empresa,
                habilitado = habilitado,
                onEditar = onEditar,
                onDefinirPadrao = onDefinirPadrao,
                onExcluir = onExcluir
            )
        }
    }
}

/** Menu "três pontinhos" do cartão: editar, definir como padrão e excluir. */
@Composable
private fun MenuEmpresa(
    empresa: Prestador,
    habilitado: Boolean,
    onEditar: () -> Unit,
    onDefinirPadrao: () -> Unit,
    onExcluir: () -> Unit
) {
    var aberto by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { aberto = true }, enabled = habilitado) {
            Icon(Icons.Outlined.MoreVert, contentDescription = "Ações de ${empresa.apelido}")
        }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            DropdownMenuItem(
                text = { Text("Editar") },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                onClick = { aberto = false; onEditar() }
            )
            // Já é a padrão? Então a opção não faz sentido e some.
            if (!empresa.padrao) {
                DropdownMenuItem(
                    text = { Text("Definir como padrão") },
                    leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                    onClick = { aberto = false; onDefinirPadrao() }
                )
            }
            DropdownMenuItem(
                text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = { aberto = false; onExcluir() }
            )
        }
    }
}

/** Pílula `primaryContainer`. */
@Composable
private fun Badge(texto: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

// ───────────────────────────── Conta ─────────────────────────────

@Composable
private fun SecaoConta(email: String, onSair: () -> Unit) {
    val cores = MaterialTheme.colorScheme

    Text("Conta", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(12.dp))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cores.surface,
        border = BorderStroke(1.dp, cores.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            LinhaConta(
                icone = Icons.Outlined.AccountCircle,
                tint = cores.onSurfaceVariant
            ) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Conectado",
                    style = MaterialTheme.typography.labelSmall,
                    color = cores.onSurfaceVariant
                )
            }
            HorizontalDivider(color = cores.outlineVariant)
            LinhaConta(
                icone = Icons.AutoMirrored.Outlined.Logout,
                tint = cores.error,
                aoClicar = onSair
            ) {
                Text(
                    text = "Sair",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = cores.error
                )
            }
        }
    }
}

@Composable
private fun LinhaConta(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    aoClicar: (() -> Unit)? = null,
    conteudo: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (aoClicar != null) Modifier.clickable(onClick = aoClicar) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icone, contentDescription = null, tint = tint)
        Column(Modifier.weight(1f)) { conteudo() }
    }
}

/** Mesma caixa de mensagem do login (errorContainer / primaryContainer, cantos 10.dp). */
@Composable
private fun CaixaMensagem(
    texto: String,
    fundo: androidx.compose.ui.graphics.Color,
    cor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(fundo)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyMedium, color = cor)
    }
}
