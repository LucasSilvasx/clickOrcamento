package com.clickorcamento.app.feature.orcamento

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.clickorcamento.app.core.design.Badge
import com.clickorcamento.app.domain.model.StatusOrcamento

/**
 * Badge do status do orçamento. Só "Aprovado" ganha a cor principal (é a
 * boa notícia); os demais ficam neutros.
 */
@Composable
fun BadgeStatus(status: StatusOrcamento, modifier: Modifier = Modifier) {
    Badge(
        texto = status.rotulo,
        modifier = modifier,
        neutro = status != StatusOrcamento.APROVADO
    )
}

/** "Nº 0009": o número com 4 dígitos, como na planilha. */
fun textoNumero(numero: Int?): String =
    if (numero == null) "Nº —" else "Nº ${numero.toString().padStart(4, '0')}"
