package com.clickorcamento.app.core.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun rememberEntregadorPdf(): EntregadorPdf = remember { EntregadorPdfDesktop }

/** Pasta do último "Salvar como": o próximo já abre nela (vale enquanto o app estiver aberto). */
private var ultimaPasta: String? = null

private object EntregadorPdfDesktop : EntregadorPdf {
    override suspend fun entregar(nomeArquivo: String, bytes: ByteArray): String? {
        val dialogo = FileDialog(null as Frame?, "Salvar orçamento em PDF", FileDialog.SAVE).apply {
            directory = ultimaPasta ?: pastaDocumentos()
            file = nomeArquivo
        }
        dialogo.isVisible = true // modal: o código espera aqui até o usuário salvar ou cancelar

        val escolhido = dialogo.file ?: return null // cancelou
        ultimaPasta = dialogo.directory
        val nome = if (escolhido.endsWith(".pdf", ignoreCase = true)) escolhido else "$escolhido.pdf"
        val arquivo = File(dialogo.directory, nome)

        withContext(Dispatchers.IO) {
            arquivo.writeBytes(bytes)
            // Abre no leitor de PDF padrão. Se não houver nenhum, tudo bem: o arquivo já está salvo.
            runCatching { Desktop.getDesktop().open(arquivo) }
        }
        return "PDF salvo em ${arquivo.absolutePath}"
    }
}

private fun pastaDocumentos(): String {
    val home = System.getProperty("user.home")
    return File(home, "Documents").takeIf { it.isDirectory }?.path ?: home
}
