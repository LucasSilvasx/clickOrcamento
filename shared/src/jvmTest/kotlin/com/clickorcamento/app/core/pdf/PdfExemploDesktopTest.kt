package com.clickorcamento.app.core.pdf

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Grava dois PDFs de exemplo em `shared/build/pdf-exemplo/` para conferir o layout
 * sem rodar o app. Não verifica nada visual.
 */
class PdfExemploDesktopTest {

    @Test
    fun gravaPdfsDeExemplo() {
        val pasta = File("build/pdf-exemplo").apply { mkdirs() }

        val curto = File(pasta, "orcamento-1-pagina.pdf")
        curto.writeBytes(gerarPdfOrcamento(12, dadosExemplo(quantidadeItens = 6), prestadorExemplo, clienteExemplo))

        val longo = File(pasta, "orcamento-varias-paginas.pdf")
        longo.writeBytes(gerarPdfOrcamento(13, dadosExemplo(quantidadeItens = 40), prestadorExemplo, clienteExemplo))

        println("PDFs de exemplo em: ${pasta.absolutePath}")
        assertTrue(curto.length() > 0 && longo.length() > 0)
    }
}
