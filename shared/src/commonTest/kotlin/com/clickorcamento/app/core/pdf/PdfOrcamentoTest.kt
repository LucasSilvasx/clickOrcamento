package com.clickorcamento.app.core.pdf

import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosOrcamento
import com.clickorcamento.app.domain.model.ItemOrcamento
import com.clickorcamento.app.domain.model.Prestador
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Roda em todos os alvos (`jvmTest`, `wasmJsTest`, `testAndroidHostTest`): o gerador é o mesmo código. */
class PdfOrcamentoTest {

    @Test
    fun numerosSaemNoFormatoDoPdf() {
        assertEquals("1", num(1.0))
        assertEquals("841.89", num(841.89))
        assertEquals("-2.5", num(-2.5))
        assertEquals("0", num(0.0001)) // nada de "1.0E-4"
    }

    @Test
    fun acentosViramCodigoOctalEParentesesGanhamEscape() {
        assertEquals("(Or\\347amento \\(1\\))", textoPdf("Orçamento (1)"))
        assertEquals("(N\\272 0012)", textoPdf("Nº 0012"))
        assertEquals("(Ol\\341 ?)", textoPdf("Olá ☃")) // fora da WinAnsi vira "?"
    }

    @Test
    fun quebraRespeitaLarguraEEnterDoUsuario() {
        val linhas = quebrarLinhas("um dois três\nquatro", FontePdf.REGULAR, 10.0, 1000.0)
        assertEquals(listOf("um dois três", "quatro"), linhas)

        val estreitas = quebrarLinhas("palavra palavra palavra", FontePdf.REGULAR, 10.0, 50.0)
        assertEquals(3, estreitas.size)
        assertTrue(estreitas.all { FontePdf.REGULAR.largura(it, 10.0) <= 50.0 })
    }

    @Test
    fun palavraGiganteEPicada() {
        val linhas = quebrarLinhas("https://exemplo.com.br/um/link/bem/comprido", FontePdf.REGULAR, 10.0, 60.0)
        assertTrue(linhas.size > 1)
        assertTrue(linhas.all { FontePdf.REGULAR.largura(it, 10.0) <= 60.0 })
    }

    @Test
    fun indiceDoArquivoApontaParaOsObjetosCertos() {
        val texto = gerarPdfOrcamento(12, dadosExemplo(quantidadeItens = 40), prestadorExemplo, clienteExemplo)
            .decodeToString()

        assertTrue(texto.startsWith("%PDF-1.4"))
        assertTrue(texto.endsWith("%%EOF\n"))

        // 40 itens não cabem numa folha: precisa ter virado a página.
        val paginas = Regex("""/Count (\d+)""").find(texto)!!.groupValues[1].toInt()
        assertTrue(paginas >= 2, "esperava 2+ páginas, veio $paginas")

        // "startxref" diz onde está o índice; cada linha do índice diz onde está cada objeto.
        val inicioIndice = texto.substringAfterLast("startxref\n").substringBefore('\n').toInt()
        assertTrue(texto.startsWith("xref", inicioIndice))
        val entradas = texto.substring(inicioIndice).lines().drop(3).takeWhile { it.endsWith(" n ") }
        assertTrue(entradas.isNotEmpty())
        entradas.forEachIndexed { i, entrada ->
            val posicao = entrada.take(10).toInt()
            assertTrue(texto.startsWith("${i + 1} 0 obj", posicao), "objeto ${i + 1} fora do lugar")
        }
    }

    @Test
    fun nomeDoArquivoSemCaracteresProibidos() {
        val cliente = clienteExemplo.copy(razaoSocial = "A/B: Teste?")
        assertEquals("Orçamento 0007 - AB Teste.pdf", nomeArquivoPdf(7, cliente))
    }
}

// ───────────── Dados de exemplo (usados também pelo teste do desktop) ─────────────

internal val prestadorExemplo = Prestador(
    id = "p1",
    apelido = "Marcenaria",
    nomeRazaoSocial = "Marcenaria Exemplo Ltda",
    cpfCnpj = "12.345.678/0001-90",
    endereco = "Rua das Araucárias, 123 - Centro - Curitiba/PR",
    telefone = "(41) 99999-0000",
    email = "contato@exemplo.com.br",
    formaPagamentoPadrao = null,
    prazoPagamentoPadrao = null,
    observacoesPadrao = null,
    padrao = true
)

internal val clienteExemplo = Cliente(
    id = "c1",
    razaoSocial = "Agropecuária Lago Azul Ltda",
    cnpj = "98.765.432/0001-10",
    endereco = "Estrada Rural, km 5 - Zona Rural - Cidade Exemplo/PR",
    inscricaoEstadual = "123.45678-90",
    telefone = "(42) 98888-1111",
    email = "compras@exemplo.com.br"
)

internal fun dadosExemplo(quantidadeItens: Int = 6) = DadosOrcamento(
    dataEmissao = LocalDate(2026, 10, 26),
    dataValidade = LocalDate(2026, 11, 25),
    prestadorId = "p1",
    clienteId = "c1",
    descontoCentavos = 15000,
    formaPagamento = "Pix ou transferência",
    prazoPagamento = "50% na aprovação e 50% na entrega",
    observacoes = "Dados bancários: Banco 000, ag. 0001, conta 12345-6.\nPix: contato@exemplo.com.br\n\n" +
        "Material incluso. Instalação em até 15 dias úteis após a aprovação.",
    itens = List(quantidadeItens) { i ->
        ItemOrcamento(
            descricao = if (i % 3 == 0) {
                "Armário de cozinha em MDF branco com portas de correr, puxadores em alumínio e " +
                    "prateleiras reguláveis (item ${i + 1})"
            } else {
                "Instalação e ajuste (item ${i + 1})"
            },
            quantidade = if (i % 2 == 0) 1.0 else 2.5,
            unidade = if (i % 2 == 0) "un" else "h",
            valorUnitarioCentavos = 130000L + i * 1000
        )
    }
)
