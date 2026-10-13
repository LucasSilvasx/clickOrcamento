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
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clickorcamento.app.domain.model.Prestador
import org.koin.compose.viewmodel.koinViewModel

/** Largura máxima do conteúdo: formulários e listas não devem esticar na tela inteira. */
private val LarguraMaxima = 780.dp

/** Só conhece o ViewModel e repassa; o desenho está em [ConfiguracoesConteudo]. */
@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracoesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ConfiguracoesConteudo(
        state = state,
        onTentarDeNovo = viewModel::carregar,
        onSair = viewModel::sair
    )
}

@Composable
private fun ConfiguracoesConteudo(
    state: ConfiguracoesUiState,
    onTentarDeNovo: () -> Unit,
    onSair: () -> Unit
) {
    // verticalScroll: no celular (ou janela baixa) o conteúdo passa da tela.
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.widthIn(max = LarguraMaxima).fillMaxWidth()) {
            SecaoEmpresas(state, onTentarDeNovo)

            Spacer(Modifier.height(32.dp))

            // A seção Conta fica fora do `when` das empresas de propósito:
            // mesmo com erro de carga, o "Sair" continua acessível.
            SecaoConta(email = state.emailUsuario, onSair = onSair)
        }
    }
}

// ───────────────────────────── Empresas ─────────────────────────────

@Composable
private fun SecaoEmpresas(state: ConfiguracoesUiState, onTentarDeNovo: () -> Unit) {
    Text("Empresas", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Text(
        text = "Quem presta o serviço. A empresa padrão já vem escolhida em cada orçamento novo.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(16.dp))

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

        // Por enquanto sem botão: o cadastro de empresa é o próximo passo.
        state.mostrarVazio -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Cadastre sua empresa para começar", style = MaterialTheme.typography.titleSmall)
            Text(
                text = "Os dados dela aparecem no cabeçalho de cada orçamento e do PDF.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.empresas.forEach { CartaoEmpresa(it) }
        }
    }
}

@Composable
private fun CartaoEmpresa(empresa: Prestador) {
    val cores = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cores.surface,
        // A empresa padrão ganha a borda da cor principal, como o cartão
        // selecionado do mockup.
        border = BorderStroke(
            width = if (empresa.padrao) 1.5.dp else 1.dp,
            color = if (empresa.padrao) cores.primary else cores.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                tint = if (empresa.padrao) cores.primary else cores.onSurfaceVariant
            )
            // weight(1f) + ellipsis: nomes longos não empurram nada para fora.
            Column(Modifier.weight(1f)) {
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
                        text = empresa.cpfCnpj,
                        style = MaterialTheme.typography.labelSmall,
                        color = cores.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Pílula `primaryContainer` (docs/UI-UX.md, seção 3). */
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
