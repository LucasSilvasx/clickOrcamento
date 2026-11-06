package com.clickorcamento.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import com.clickorcamento.app.feature.cliente.ClientesScreen
import com.clickorcamento.app.feature.configuracoes.ConfiguracoesScreen
import com.clickorcamento.app.feature.orcamento.editor.EditorOrcamentoScreen
import com.clickorcamento.app.feature.orcamento.lista.OrcamentosScreen
import org.koin.compose.koinInject

/**
 * Grafo de navegação do app logado.
 *
 * Só existe depois do login: quem decide entre Login e app é o
 * `sessionStatus` no App.kt.
 *
 * A `CascaApp` desenha as abas/barra inferior em volta do NavHost.
 *
 * A área Orçamentos é um grafo aninhado (`navigation<RotaOrcamentos>`): a lista
 * é o início e o editor fica "dentro" dela. Assim a aba Orçamentos segue marcada
 * com o editor aberto e o "voltar" do editor cai na lista.
 */
@Composable
fun ClickNavHost() {
    val navController = rememberNavController()

    // Observa a rota atual: quando muda, a casca recompõe e marca a aba certa.
    val entradaAtual by navController.currentBackStackEntryAsState()

    // Lê o status para recalcular quando a sessão volta: durante RefreshFailure não há usuário ativo.
    val supabase = koinInject<SupabaseClient>()
    val sessao by supabase.auth.sessionStatus.collectAsState()
    val iniciais = remember(sessao) { iniciaisDoEmail(supabase.auth.currentUserOrNull()?.email) }

    // O editor é uma tela "cheia": no celular a casca esconde as barras e ele desenha as suas.
    val telaCheia = entradaAtual?.destination?.hasRoute(RotaEditorOrcamento::class) == true

    CascaApp(
        destinoAtual = entradaAtual?.destination,
        iniciais = iniciais,
        telaCheia = telaCheia,
        aoSelecionar = { navController.irParaArea(it) }
    ) {
        NavHost(
            navController = navController,
            startDestination = RotaOrcamentos, // o app abre na lista de orçamentos
            // O padrão é um fade de 700 ms, que em cada troca de aba parece a tela recarregando.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            navigation<RotaOrcamentos>(startDestination = RotaListaOrcamentos) {
                composable<RotaListaOrcamentos> {
                    OrcamentosScreen(
                        aoNovo = { navController.navigate(RotaEditorOrcamento()) },
                        aoAbrir = { id -> navController.navigate(RotaEditorOrcamento(id)) }
                    )
                }
                composable<RotaEditorOrcamento> { entrada ->
                    // toRoute devolve a rota com o argumento já tipado (orcamentoId).
                    val rota = entrada.toRoute<RotaEditorOrcamento>()
                    EditorOrcamentoScreen(
                        orcamentoId = rota.orcamentoId,
                        aoVoltar = { navController.popBackStack() },
                        aoIrParaConfiguracoes = {
                            navController.irParaArea(destinos.first { it.rota == RotaConfiguracoes })
                        }
                    )
                }
            }
            composable<RotaClientes> {
                ClientesScreen(
                    aoAbrirOrcamento = { id -> navController.navigate(RotaEditorOrcamento(id)) }
                )
            }
            composable<RotaConfiguracoes> { ConfiguracoesScreen() }
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
 * Troca de aba sem empilhar telas, com o estado de cada aba guardado: assim o "voltar" do
 * Android sai do app em vez de percorrer todas as abas.
 *
 * Tocar na aba da área atual não restaura nada (restoreState traria o editor de volta):
 * em Orçamentos, leva de volta à lista.
 */
private fun NavController.irParaArea(destino: Destino) {
    val jaNaArea = currentDestination?.hierarchy?.any { it.hasRoute(destino.classe) } == true
    if (jaNaArea) {
        if (destino.rota == RotaOrcamentos) {
            val voltou = popBackStack(RotaListaOrcamentos, inclusive = false)
            // Veio de outra aba direto para o editor: a lista não está na pilha, então abre ela no lugar.
            if (!voltou) {
                navigate(RotaListaOrcamentos) { popUpTo(RotaOrcamentos) { inclusive = true } }
            }
        }
        return
    }

    navigate(destino.rota) {
        popUpTo(RotaOrcamentos) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
