package com.clickorcamento.app.core.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Azul herdado do cabeçalho da planilha original — o app fica
// visualmente coerente com o que seu cliente já conhece.
private val Azul900 = Color(0xFF14375A)
private val Azul700 = Color(0xFF1F4E79)
private val Azul300 = Color(0xFF7FB3E3)
private val Azul100 = Color(0xFFDCE9F5)

val EsquemaClaro = lightColorScheme(
    primary            = Azul700,
    onPrimary          = Color.White,
    // "container" = versão suave da cor, para fundos de destaque
    primaryContainer   = Azul100,
    onPrimaryContainer = Azul900,

    background         = Color(0xFFFAFBFC),
    onBackground       = Color(0xFF1A1C1E),

    // surface é o fundo de cartões, campos e barras
    surface            = Color.White,
    onSurface          = Color(0xFF1A1C1E),
    surfaceVariant     = Color(0xFFEFF1F4),
    // onSurfaceVariant = texto secundário, rótulos, legendas
    onSurfaceVariant   = Color(0xFF5A6068),

    outline            = Color(0xFFC9CFD6),
    // outlineVariant = bordas sutis, divisores
    outlineVariant     = Color(0xFFE4E7EB),

    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002)
)

val EsquemaEscuro = darkColorScheme(
    primary            = Azul300,
    // Azul claro sobre azul claro não lê. No escuro, onPrimary inverte.
    onPrimary          = Color(0xFF07243D),
    primaryContainer   = Color(0xFF1B3A56),
    onPrimaryContainer = Azul100,

    background         = Color(0xFF0F1318),
    onBackground       = Color(0xFFE2E5E9),

    surface            = Color(0xFF171C23),
    onSurface          = Color(0xFFE2E5E9),
    surfaceVariant     = Color(0xFF232932),
    onSurfaceVariant   = Color(0xFF9AA3AE),

    outline            = Color(0xFF3B434D),
    outlineVariant     = Color(0xFF262C34),

    error              = Color(0xFFFFB4AB),
    onError            = Color(0xFF690005),
    errorContainer     = Color(0xFF93000A),
    onErrorContainer   = Color(0xFFFFDAD6)
)