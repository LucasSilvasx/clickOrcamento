package com.clickorcamento.app.core.design

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Usa a fonte padrão de cada plataforma por enquanto. Fonte própria
 * exige empacotar o arquivo .ttf nos recursos compartilhados; fica para
 * quando houver uma identidade visual definida.
 *
 * Aqui só ajustamos peso e espaçamento, que já muda bastante a
 * percepção de qualidade.
 */
val TipografiaClick = Typography(
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        // Negativo aperta as letras. Em títulos, deixa mais elegante.
        letterSpacing = (-0.4).sp
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        // Altura de linha maior que a fonte dá respiro na leitura
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        letterSpacing = 0.3.sp
    )
)