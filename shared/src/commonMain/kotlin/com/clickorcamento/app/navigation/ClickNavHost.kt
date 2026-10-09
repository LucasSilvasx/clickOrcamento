package com.clickorcamento.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Grafo de navegação do app logado.
 *
 * O NavHost é uma "moldura" que mostra uma tela por vez, conforme a rota
 * atual do `navController`. Quem decide entre Login e app continua sendo o
 * `sessionStatus` no App.kt — este NavHost só existe depois de autenticar.
 *
 * A `CascaApp` desenha as abas/barra inferior em volta do NavHost.
 */
@Composable
fun ClickNavHost() {
    // O NavController guarda a pilha de telas (o "voltar").
    val navController = rememberNavController()

    // Observa a rota atual: quando muda, a casca recompõe e marca a aba certa.
    val entradaAtual by navController.currentBackStackEntryAsState()

    val supabase = koinInject<SupabaseClient>()
    val iniciais = iniciaisDoEmail(supabase.auth.currentUserOrNull()?.email)

    CascaApp(
        destinoAtual = entradaAtual?.destination,
        iniciais = iniciais,
        aoSelecionar = { navController.irParaArea(it) }
    ) {
        NavHost(
            navController = navController,
            startDestination = RotaOrcamentos // área inicial, conforme UI-UX.md
        ) {
            composable<RotaOrcamentos> { TelaVazia("Orçamentos") }
            composable<RotaClientes> { TelaVazia("Clientes") }
            composable<RotaConfiguracoes> { ConfiguracoesProvisoria() }
        }
    }
}

/**
 * Iniciais para o avatar do topo. Ainda não guardamos o nome do usuário,
 * então usamos as duas primeiras letras do e-mail ("lucas@x.com" -> "LU").
 * Quando houver nome no perfil, troca aqui.
 */
private fun iniciaisDoEmail(email: String?): String =
    email?.substringBefore('@')
        ?.filter { it.isLetter() }
        ?.take(2)
        ?.uppercase()
        ?.ifEmpty { null }
        ?: "?"

/**
 * Troca de aba sem empilhar telas. Receita padrão do Navigation:
 * - popUpTo(início) + saveState: ao trocar de aba, guarda o estado da que saiu;
 * - launchSingleTop: tocar na aba atual não cria cópia dela;
 * - restoreState: voltar para uma aba restaura o que o usuário deixou nela.
 * Assim o "voltar" do Android sai do app em vez de percorrer todas as abas.
 */
private fun NavController.irParaArea(destino: Destino) {
    navigate(destino.rota) {
        popUpTo(RotaOrcamentos) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Tela vazia reaproveitada pelas áreas que ainda não existem. */
@Composable
private fun TelaVazia(titulo: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Em construção",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Placeholder de Configurações. Por enquanto só guarda a conta + "Sair"
 * (que antes ficava na home), porque o logout pertence a esta área segundo
 * o docs/UI-UX.md. Vira a tela real no passo 5.
 */
@Composable
private fun ConfiguracoesProvisoria() {
    val supabase = koinInject<SupabaseClient>()
    val escopo = rememberCoroutineScope()
    val usuario = supabase.auth.currentUserOrNull()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configurações", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = usuario?.email.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = { escopo.launch { supabase.auth.signOut() } }) {
            Text("Sair")
        }
    }
}
