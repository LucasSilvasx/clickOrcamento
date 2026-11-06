package com.clickorcamento.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clickorcamento.app.core.design.ClickTheme
import com.clickorcamento.app.data.repository.CacheLeituras
import com.clickorcamento.app.di.appModule
import com.clickorcamento.app.feature.auth.LoginScreen
import com.clickorcamento.app.navigation.ClickNavHost
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

// Fora do @Composable de propósito: assim a configuração é criada uma vez
// e não a cada recomposição.
private val configuracaoKoin = koinConfiguration { modules(appModule) }

/** [temaManual]: o Android passa false para seguir o tema do aparelho, sem botão de alternar. */
@Composable
fun App(temaManual: Boolean = true) {
    // Dentro da composição para que os três alvos não precisem de um startKoin() próprio.
    KoinApplication(configuracaoKoin) {
        ClickTheme(temaManual = temaManual) {
            val supabase = koinInject<SupabaseClient>()

            // Login e logout trocam de tela por aqui: nenhuma tela navega depois de autenticar.
            val sessao by supabase.auth.sessionStatus.collectAsState()

            // O cache guarda dados já filtrados pelo RLS: sem esta limpeza, outra conta
            // entrando no mesmo app veria as listas da anterior. Limpa também ao voltar de
            // RefreshFailure, porque sem token válido o RLS devolve listas vazias.
            val cache = koinInject<CacheLeituras>()
            val logado = sessao is SessionStatus.Authenticated
            LaunchedEffect(logado) { cache.limpar() }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (sessao) {
                    // Mostrar o login aqui faria a tela piscar para quem já tem sessão salva.
                    is SessionStatus.Initializing -> TelaCarregando()
                    // RefreshFailure é falha de rede ou de servidor, não logout: a biblioteca segue
                    // tentando renovar. Ir para o login descartaria o orçamento em edição.
                    is SessionStatus.Authenticated, is SessionStatus.RefreshFailure -> ClickNavHost()
                    else -> LoginScreen()
                }
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
