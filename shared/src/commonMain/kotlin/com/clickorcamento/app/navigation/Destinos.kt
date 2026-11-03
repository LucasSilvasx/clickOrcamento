package com.clickorcamento.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.reflect.KClass

/**
 * Uma "área" do app que aparece na navegação (abas no desktop, barra
 * inferior no celular). Liga a rota ao texto e ao ícone mostrados.
 *
 * `classe` serve para descobrir qual aba está selecionada: o NavController
 * sabe a rota atual, e `hasRoute(classe)` compara com ela.
 */
data class Destino(
    val rota: Any,
    val classe: KClass<*>,
    val titulo: String,
    val icone: ImageVector // só o celular usa; no desktop as abas são só texto
)

/** A ordem da lista é a ordem das abas na tela. */
val destinos = listOf(
    Destino(RotaOrcamentos, RotaOrcamentos::class, "Orçamentos", Icons.Outlined.ReceiptLong),
    Destino(RotaClientes, RotaClientes::class, "Clientes", Icons.Outlined.Group),
    Destino(RotaConfiguracoes, RotaConfiguracoes::class, "Configurações", Icons.Outlined.Settings)
)
