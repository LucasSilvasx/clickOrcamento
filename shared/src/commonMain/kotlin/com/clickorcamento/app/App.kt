package com.clickorcamento.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clickorcamento.app.core.design.ClickTheme
import com.clickorcamento.app.data.remote.supabase
import com.clickorcamento.app.feature.auth.LoginScreen
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

@Composable
fun App() {
    ClickTheme {
        // sessionStatus é um StateFlow que o supabase-kt mantém
        // atualizado. Fazendo a raiz do app reagir a ele, o login e o
        // logout trocam de tela sozinhos — nenhuma tela precisa
        // "navegar" manualmente depois de autenticar.
        val sessao by supabase.auth.sessionStatus.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (sessao) {
                // Initializing: o cliente ainda está verificando se
                // existe sessão salva. Mostrar login aqui faria a tela
                // piscar pra quem já estava logado.
                is SessionStatus.Initializing -> TelaCarregando()
                is SessionStatus.Authenticated -> HomeProvisoria()
                else -> LoginScreen()
            }
        }
    }
}

@Composable
private fun TelaCarregando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Placeholder até termos navegação e as telas de verdade. */
@Composable
private fun HomeProvisoria() {
    val escopo = rememberCoroutineScope()
    val usuario = supabase.auth.currentUserOrNull()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Você está dentro", style = MaterialTheme.typography.headlineSmall)
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