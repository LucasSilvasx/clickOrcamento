package com.clickorcamento.app.core.pdf

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Não verifica nada visual (isso é com os olhos): grava dois PDFs de exemplo em
 * `shared/build/pdf-exemplo/` para abrir e conferir o layout sem rodar o app.
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
