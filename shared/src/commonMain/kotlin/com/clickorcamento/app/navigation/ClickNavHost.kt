package com.clickorcamento.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
 */
@Composable
fun ClickNavHost() {
    // O NavController guarda a pilha de telas (o "voltar").
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RotaOrcamentos // área inicial, conforme UI-UX.md
    ) {
        composable<RotaOrcamentos> {
            HomeProvisoria(
                aoIrParaClientes = { navController.navigate(RotaClientes) },
                aoIrParaConfiguracoes = { navController.navigate(RotaConfiguracoes) }
            )
        }
        composable<RotaClientes> {
            TelaVazia(titulo = "Clientes", aoVoltar = { navController.popBackStack() })
        }
        composable<RotaConfiguracoes> {
            TelaVazia(titulo = "Configurações", aoVoltar = { navController.popBackStack() })
        }
    }
}

/**
 * Placeholder da área inicial (era a HomeProvisoria do App.kt). Os botões
 * existem só para testar a navegação; saem no passo 3, quando entrar a
 * barra de abas (desktop) / barra inferior (celular).
 */
@Composable
private fun HomeProvisoria(
    aoIrParaClientes: () -> Unit,
    aoIrParaConfiguracoes: () -> Unit
) {
    val supabase = koinInject<SupabaseClient>()
    val escopo = rememberCoroutineScope()
    val usuario = supabase.auth.currentUserOrNull()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Orçamentos", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = usuario?.email.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = aoIrParaClientes) { Text("Ir para Clientes") }
        OutlinedButton(onClick = aoIrParaConfiguracoes) { Text("Ir para Configurações") }
        OutlinedButton(onClick = { escopo.launch { supabase.auth.signOut() } }) {
            Text("Sair")
        }
    }
}

/** Tela vazia reaproveitada pelas áreas que ainda não existem. */
@Composable
private fun TelaVazia(titulo: String, aoVoltar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Em construção",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedButton(onClick = aoVoltar) { Text("Voltar") }
    }
}
