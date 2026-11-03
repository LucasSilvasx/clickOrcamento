package com.clickorcamento.app.core.pdf

import androidx.compose.runtime.Composable
import kotlin.io.encoding.Base64

// Cópia do EntregadorPdf.wasmJs.kt para o alvo `js`, que vai ser removido.
// Quando ele sair, este arquivo sai junto.

@Composable
actual fun rememberEntregadorPdf(): EntregadorPdf = EntregadorPdfWeb

private object EntregadorPdfWeb : EntregadorPdf {
    override suspend fun entregar(nomeArquivo: String, bytes: ByteArray): String? {
        baixarPdf(nomeArquivo, Base64.encode(bytes))
        return "PDF baixado. Confira a pasta de downloads do navegador."
    }
}

private fun baixarPdf(nomeArquivo: String, base64: String) {
    js(
        """
        var binario = atob(base64);
        var bytes = new Uint8Array(binario.length);
        for (var i = 0; i < binario.length; i++) bytes[i] = binario.charCodeAt(i);
        var url = URL.createObjectURL(new Blob([bytes], { type: 'application/pdf' }));
        var link = document.createElement('a');
        link.href = url;
        link.download = nomeArquivo;
        document.body.appendChild(link);
        link.click();
        link.remove();
        setTimeout(function () { URL.revokeObjectURL(url); }, 10000);
        """
    )
}
