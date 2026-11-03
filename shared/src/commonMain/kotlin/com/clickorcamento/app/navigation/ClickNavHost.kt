package com.clickorcamento.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * O NavHost é uma "moldura" que mostra uma tela por vez, conforme a rota
 * atual do `navController`. Quem decide entre Login e app continua sendo o
 * `sessionStatus` no App.kt — este NavHost só existe depois de autenticar.
 *
 * A `CascaApp` desenha as abas/barra inferior em volta do NavHost.
 *
 * A área Orçamentos é um grafo aninhado (`navigation<RotaOrcamentos>`): a lista
 * é o início e o editor fica "dentro" dela. Assim a aba Orçamentos segue marcada
 * com o editor aberto e o "voltar" do editor cai na lista.
 */
@Composable
fun ClickNavHost() {
    // O NavController guarda a pilha de telas (o "voltar").
    val navController = rememberNavController()

    // Observa a rota atual: quando muda, a casca recompõe e marca a aba certa.
    val entradaAtual by navController.currentBackStackEntryAsState()

    val supabase = koinInject<SupabaseClient>()
    val iniciais = iniciaisDoEmail(supabase.auth.currentUserOrNull()?.email)

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
            startDestination = RotaOrcamentos // o app abre na lista de orçamentos
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
 * Troca de aba sem empilhar telas. Receita padrão do Navigation:
 * - popUpTo(início) + saveState: ao trocar de aba, guarda o estado da que saiu;
 * - launchSingleTop: tocar na aba atual não cria cópia dela;
 * - restoreState: voltar para uma aba restaura o que o usuário deixou nela.
 * Assim o "voltar" do Android sai do app em vez de percorrer todas as abas.
 *
 * Tocar na aba da área onde o usuário JÁ está não restaura nada (restoreState
 * traria o editor de volta): em Orçamentos, leva de volta à lista.
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
