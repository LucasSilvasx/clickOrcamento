package com.clickorcamento.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.clickorcamento.app.core.design.LocalAlternarTema
import com.clickorcamento.app.core.design.LocalTemaEscuro

/** Abaixo disso o app usa o layout de celular (docs/UI-UX.md, seção 6). */
private val LarguraMinimaDesktop = 600.dp

/**
 * Moldura do app logado: escolhe o layout pela largura disponível e
 * desenha a navegação. O conteúdo da tela atual entra em `conteudo`.
 *
 * Medidas e cores seguem o mockup em docs/mockups/telas-clickorcamento.html.
 *
 * - Estreito (< 600.dp): barra de título com o nome da área, conteúdo e
 *   `NavigationBar` embaixo; padding 16.dp.
 * - Largo: barra do topo (marca, tema, avatar), abas e conteúdo com
 *   padding 24.dp. A largura máxima do conteúdo (~1000.dp no editor) fica
 *   a cargo de cada tela, porque Clientes e Configurações usam a largura toda.
 *
 * Analogia com o Android puro: é o `Scaffold` que você já conhece, só que
 * ele troca de "roupa" conforme o tamanho da janela, em vez de ter um
 * layout-sw600dp separado.
 */
@Composable
fun CascaApp(
    destinoAtual: NavDestination?,
    iniciais: String,
    aoSelecionar: (Destino) -> Unit,
    conteudo: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth < LarguraMinimaDesktop) {
            CascaCelular(destinoAtual, aoSelecionar, conteudo)
        } else {
            CascaDesktop(destinoAtual, iniciais, aoSelecionar, conteudo)
        }
    }
}

// ───────────────────────────── Celular ─────────────────────────────

@Composable
private fun CascaCelular(
    destinoAtual: NavDestination?,
    aoSelecionar: (Destino) -> Unit,
    conteudo: @Composable () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraTituloCelular(titulo = destinos.firstOrNull { destinoAtual.estaEm(it) }?.titulo.orEmpty())
        },
        bottomBar = { BarraInferior(destinoAtual, aoSelecionar) }
    ) { paddingInterno ->
        // paddingInterno já desconta as duas barras; o do design vem depois.
        Box(Modifier.fillMaxSize().padding(paddingInterno).padding(16.dp)) {
            conteudo()
        }
    }
}

/** Mockup "appbar": 56dp, fundo surface, título 18sp semibold, divisor embaixo. */
@Composable
private fun BarraTituloCelular(titulo: String) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                // O app desenha sob a barra de status (edge-to-edge no Android):
                // o fundo surface sobe até lá e o texto fica abaixo dela.
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

/** Mockup "bottomnav": fundo surface, divisor em cima, "pílula" primaryContainer no item ativo. */
@Composable
private fun BarraInferior(destinoAtual: NavDestination?, aoSelecionar: (Destino) -> Unit) {
    val cores = MaterialTheme.colorScheme
    Column {
        HorizontalDivider(color = cores.outlineVariant)
        NavigationBar(containerColor = cores.surface, tonalElevation = 0.dp) {
            destinos.forEach { destino ->
                val selecionado = destinoAtual.estaEm(destino)
                NavigationBarItem(
                    selected = selecionado,
                    onClick = { aoSelecionar(destino) },
                    icon = { Icon(destino.icone, contentDescription = null) },
                    label = {
                        Text(
                            text = destino.titulo,
                            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    alwaysShowLabel = true,
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = cores.primaryContainer,
                        selectedIconColor = cores.onPrimaryContainer,
                        selectedTextColor = cores.onSurface,
                        unselectedIconColor = cores.onSurfaceVariant,
                        unselectedTextColor = cores.onSurfaceVariant
                    )
                )
            }
        }
    }
}

// ───────────────────────────── Desktop ─────────────────────────────

@Composable
private fun CascaDesktop(
    destinoAtual: NavDestination?,
    iniciais: String,
    aoSelecionar: (Destino) -> Unit,
    conteudo: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Barra do topo + abas formam um bloco só, em fundo surface.
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                BarraTopoDesktop(iniciais)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AbasDesktop(destinoAtual, aoSelecionar)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Box(Modifier.fillMaxSize().padding(24.dp)) {
            conteudo()
        }
    }
}

/** Mockup "topbar": 56dp, marca à esquerda; à direita ícone de tema e avatar. */
@Composable
private fun BarraTopoDesktop(iniciais: String) {
    val cores = MaterialTheme.colorScheme
    val escuro = LocalTemaEscuro.current
    val alternarTema = LocalAlternarTema.current

    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder da marca (igual ao LoginScreen.Marca()): quadrado azul com "C".
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(cores.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "C",
                color = cores.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "clickOrçamento",
            modifier = Modifier.padding(start = 10.dp).weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        // Mostra o ícone da ação (ir para escuro / ir para claro), não do estado atual.
        IconButton(onClick = alternarTema) {
            Icon(
                imageVector = if (escuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = "Alternar tema",
                tint = cores.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(cores.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iniciais,
                color = cores.onPrimaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Mockup "tabs": só texto, 28dp entre abas, a ativa em primary com
 * sublinhado de 3dp. Feito à mão (em vez de `TabRow`) porque o TabRow do
 * Material divide a largura em partes iguais e o mockup quer abas do
 * tamanho do texto, coladas à esquerda.
 */
@Composable
private fun AbasDesktop(destinoAtual: NavDestination?, aoSelecionar: (Destino) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        destinos.forEach { destino ->
            AbaDesktop(
                titulo = destino.titulo,
                selecionada = destinoAtual.estaEm(destino),
                aoClicar = { aoSelecionar(destino) }
            )
        }
    }
}

@Composable
private fun AbaDesktop(titulo: String, selecionada: Boolean, aoClicar: () -> Unit) {
    val cores = MaterialTheme.colorScheme
    // width(IntrinsicSize.Max): a coluna tem a largura do texto. Sem isso, o
    // fillMaxWidth() do sublinhado puxa TODA a largura da linha para a
    // primeira aba e espreme as outras (bug visto ao alargar a janela).
    Column(
        Modifier
            .width(IntrinsicSize.Max)
            .clickable(role = Role.Tab, onClick = aoClicar)
    ) {
        Text(
            text = titulo,
            modifier = Modifier.padding(top = 13.dp, bottom = 11.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selecionada) cores.primary else cores.onSurfaceVariant
        )
        // Faixa do sublinhado: sempre ocupa os 3dp (a aba não "pula" ao
        // selecionar), só ganha cor quando ativa.
        Box(
            Modifier.fillMaxWidth().height(3.dp)
                .then(if (selecionada) Modifier.background(cores.primary) else Modifier)
        )
    }
}

/**
 * A aba fica selecionada também se a tela atual for "filha" dela
 * (ex.: o editor dentro de Orçamentos). `hierarchy` percorre a árvore de
 * destinos do pai ao filho.
 */
private fun NavDestination?.estaEm(destino: Destino): Boolean =
    this?.hierarchy?.any { it.hasRoute(destino.classe) } == true
