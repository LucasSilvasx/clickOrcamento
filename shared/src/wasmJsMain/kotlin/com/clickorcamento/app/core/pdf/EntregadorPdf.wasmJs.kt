package com.clickorcamento.app.core.pdf

import androidx.compose.runtime.Composable
import kotlin.io.encoding.Base64

@Composable
actual fun rememberEntregadorPdf(): EntregadorPdf = EntregadorPdfWeb

private object EntregadorPdfWeb : EntregadorPdf {
    override suspend fun entregar(nomeArquivo: String, bytes: ByteArray): String? {
        // O Wasm e o JavaScript não compartilham memória: o jeito simples de
        // passar os bytes é como texto Base64, que o navegador converte de volta.
        baixarPdf(nomeArquivo, Base64.encode(bytes))
        return "PDF baixado. Confira a pasta de downloads do navegador."
    }
}

/**
 * No Kotlin/Wasm o `js(...)` precisa ser o corpo inteiro de uma função de topo.
 * Um Blob e um `<a download>` invisível fazem o navegador baixar com o nome dado.
 */
private fun baixarPdf(nomeArquivo: String, base64: String): Unit = js(
    """{
    const binario = atob(base64);
    const bytes = new Uint8Array(binario.length);
    for (let i = 0; i < binario.length; i++) bytes[i] = binario.charCodeAt(i);
    const url = URL.createObjectURL(new Blob([bytes], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo;
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 10000);
}"""
)
