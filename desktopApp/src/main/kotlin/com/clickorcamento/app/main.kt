package com.clickorcamento.app

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.skia.Image

fun main() = application {
    // O ícone do instalador não vale para a barra da janela, que precisa do próprio.
    val icone = remember {
        val bytes = checkNotNull(object {}.javaClass.getResourceAsStream("/icone.png")).use { it.readBytes() }
        BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "clickOrçamento",
        icon = icone,
    ) {
        App()
    }
}