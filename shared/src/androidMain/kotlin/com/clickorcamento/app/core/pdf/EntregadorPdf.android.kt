package com.clickorcamento.app.core.pdf

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun rememberEntregadorPdf(): EntregadorPdf {
    val contexto = LocalContext.current
    return remember(contexto) { EntregadorPdfAndroid(contexto) }
}

/**
 * Grava o PDF na pasta de cache do app e abre a folha de compartilhamento.
 *
 * Por que FileProvider? Outro app (o WhatsApp, por exemplo) não pode ler os
 * arquivos privados do nosso. O FileProvider cria um endereço `content://`
 * temporário e dá permissão de leitura SÓ para quem recebe o arquivo.
 * Ele está declarado no AndroidManifest.xml do androidApp (authority
 * "<pacote>.arquivos") e a pasta liberada fica em res/xml/caminhos_arquivos.xml.
 */
private class EntregadorPdfAndroid(private val contexto: Context) : EntregadorPdf {
    override suspend fun entregar(nomeArquivo: String, bytes: ByteArray): String? {
        val arquivo = withContext(Dispatchers.IO) {
            val pasta = File(contexto.cacheDir, "pdf").apply { mkdirs() }
            File(pasta, nomeArquivo).apply { writeBytes(bytes) }
        }
        val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.arquivos", arquivo)

        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, nomeArquivo)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val seletor = Intent.createChooser(envio, "Enviar orçamento")
        // Fora de uma Activity o Android exige abrir numa "tarefa" nova.
        if (contexto !is Activity) seletor.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(seletor)

        return "PDF pronto para enviar"
    }
}
