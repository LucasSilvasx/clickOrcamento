package com.clickorcamento.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

/** Três estados, não dois: o usuário pode querer forçar um tema
 *  independentemente do que o sistema dele está usando. */
enum class PreferenciaTema { SISTEMA, CLARO, ESCURO }

/**
 * CompositionLocal é um "canal" que atravessa a árvore de Composables.
 * Sem ele, pra um botão lá no fundo da tela alternar o tema, você teria
 * que passar a função por parâmetro em cada Composable do caminho.
 *
 * Use com moderação: serve para coisas verdadeiramente globais
 * (tema, idioma). Não use pra passar dados de tela.
 */
val LocalTemaEscuro = staticCompositionLocalOf<Boolean> {
    error("ClickTheme não foi aplicado")
}
val LocalAlternarTema = staticCompositionLocalOf<() -> Unit> {
    error("ClickTheme não foi aplicado")
}

@Composable
fun ClickTheme(content: @Composable () -> Unit) {
    var preferencia by remember { mutableStateOf(PreferenciaTema.SISTEMA) }

    // Lê a configuração do sistema operacional / navegador
    val sistemaEscuro = isSystemInDarkTheme()

    val usarEscuro = when (preferencia) {
        PreferenciaTema.SISTEMA -> sistemaEscuro
        PreferenciaTema.CLARO   -> false
        PreferenciaTema.ESCURO  -> true
    }

    val alternar: () -> Unit = {
        // Alterna a partir do que está valendo AGORA, não da preferência.
        // Assim, se está em SISTEMA e o sistema é claro, o primeiro
        // clique leva para o escuro, que é o que o usuário espera.
        preferencia = if (usarEscuro) PreferenciaTema.CLARO else PreferenciaTema.ESCURO
    }

    CompositionLocalProvider(
        LocalTemaEscuro provides usarEscuro,
        LocalAlternarTema provides alternar
    ) {
        MaterialTheme(
            colorScheme = if (usarEscuro) EsquemaEscuro else EsquemaClaro,
            typography  = TipografiaClick,
            content     = content
        )
    }
}