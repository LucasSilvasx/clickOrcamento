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
    // KoinApplication sobe o container do Koin junto com a UI. É a forma
    // multiplataforma: o mesmo código serve para Desktop, Android e Web,
    // sem precisar de startKoin() em cada MainActivity/main().
    KoinApplication(configuracaoKoin) {
        ClickTheme(temaManual = temaManual) {
            val supabase = koinInject<SupabaseClient>()

            // sessionStatus é um StateFlow que o supabase-kt mantém
            // atualizado. Fazendo a raiz do app reagir a ele, o login e o
            // logout trocam de tela sozinhos, e nenhuma tela precisa
            // "navegar" manualmente depois de autenticar.
            val sessao by supabase.auth.sessionStatus.collectAsState()

            // O cache guarda dados já filtrados pelo RLS: sem esta limpeza, outra conta
            // entrando no mesmo app veria as listas da anterior.
            val cache = koinInject<CacheLeituras>()
            val logado = sessao is SessionStatus.Authenticated
            LaunchedEffect(logado) {
                if (!logado) cache.limpar()
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (sessao) {
                    // Initializing: o cliente ainda está verificando se
                    // existe sessão salva. Mostrar login aqui faria a tela
                    // piscar pra quem já estava logado.
                    is SessionStatus.Initializing -> TelaCarregando()
                    is SessionStatus.Authenticated -> ClickNavHost()
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
