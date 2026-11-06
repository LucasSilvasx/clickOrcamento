package com.clickorcamento.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

/** Claro e escuro forçam o tema mesmo quando o sistema usa outro. */
enum class PreferenciaTema { SISTEMA, CLARO, ESCURO }

val LocalTemaEscuro = staticCompositionLocalOf<Boolean> {
    error("ClickTheme não foi aplicado")
}
/** Nulo quando o app segue sempre o tema do sistema e não há o que alternar. */
val LocalAlternarTema = staticCompositionLocalOf<(() -> Unit)?> {
    error("ClickTheme não foi aplicado")
}

/** [temaManual] = false fixa a preferência em SISTEMA e tira a alternância da interface. */
@Composable
fun ClickTheme(temaManual: Boolean = true, content: @Composable () -> Unit) {
    var preferencia by remember { mutableStateOf(PreferenciaTema.SISTEMA) }

    val sistemaEscuro = isSystemInDarkTheme()

    val usarEscuro = when (preferencia) {
        PreferenciaTema.SISTEMA -> sistemaEscuro
        PreferenciaTema.CLARO   -> false
        PreferenciaTema.ESCURO  -> true
    }

    val alternar: () -> Unit = {
        // Parte do tema em uso, não da preferência: em SISTEMA com o sistema claro,
        // o primeiro clique precisa levar ao escuro.
        preferencia = if (usarEscuro) PreferenciaTema.CLARO else PreferenciaTema.ESCURO
    }

    CompositionLocalProvider(
        LocalTemaEscuro provides usarEscuro,
        LocalAlternarTema provides alternar.takeIf { temaManual }
    ) {
        MaterialTheme(
            colorScheme = if (usarEscuro) EsquemaEscuro else EsquemaClaro,
            typography  = TipografiaClick,
            content     = content
        )
    }
}