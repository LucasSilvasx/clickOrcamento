package com.clickorcamento.app.core.pdf

import androidx.compose.runtime.Composable

/**
 * Entrega o PDF pronto ao usuário, do jeito de cada plataforma:
 *
 * - Desktop: janela "Salvar como" do Windows e, depois de salvo, abre o arquivo.
 * - Android: folha de compartilhamento (WhatsApp, Gmail, Drive...).
 * - Web: download pelo navegador.
 *
 * Gerar os bytes é igual em todo lugar ([gerarPdfOrcamento], em commonMain);
 * só esta parte precisa de `expect/actual`, porque cada sistema guarda e
 * compartilha arquivos de um jeito.
 */
interface EntregadorPdf {
    /** @return a mensagem para mostrar na tela, ou `null` se o usuário cancelou. */
    suspend fun entregar(nomeArquivo: String, bytes: ByteArray): String?
}

/**
 * É `@Composable` porque o Android precisa do `Context` da tela para abrir a
 * folha de compartilhamento, e quem tem o `Context` é a composição
 * (`LocalContext`). Mesma ideia do `rememberLauncherForActivityResult`.
 */
@Composable
expect fun rememberEntregadorPdf(): EntregadorPdf
