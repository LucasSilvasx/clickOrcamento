package com.clickorcamento.app.core.pdf

import com.clickorcamento.app.core.format.formatarBr
import com.clickorcamento.app.core.format.formatarCpfCnpj
import com.clickorcamento.app.core.format.formatarMoeda
import com.clickorcamento.app.core.format.formatarQuantidade
import com.clickorcamento.app.core.format.formatarTelefone
import com.clickorcamento.app.core.format.rotuloCpfCnpj
import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosOrcamento
import com.clickorcamento.app.domain.model.Prestador

/**
 * Gera o PDF do orçamento e devolve os bytes do arquivo.
 *
 * É Kotlin puro (commonMain): o mesmo código monta o PDF no desktop, no
 * Android e no navegador. O que muda por plataforma é só o que fazer com os
 * bytes (salvar, compartilhar, baixar), e isso fica em [EntregadorPdf].
 */
fun gerarPdfOrcamento(numero: Int, dados: DadosOrcamento, prestador: Prestador, cliente: Cliente): ByteArray =
    LayoutOrcamento(numero, dados, prestador, cliente).desenhar()

/** "Orçamento 0012 - Agropecuária Lago Azul Ltda.pdf", sem os caracteres que o Windows proíbe em nome de arquivo. */
fun nomeArquivoPdf(numero: Int, cliente: Cliente): String {
    val nomeCliente = cliente.razaoSocial.filterNot { it in "\\/:*?\"<>|" }.trim().take(60)
    val base = "Orçamento ${numero.toString().padStart(4, '0')}"
    return if (nomeCliente.isEmpty()) "$base.pdf" else "$base - $nomeCliente.pdf"
}

// --- Aparência ---

// Cores fixas de propósito: o PDF é "papel", não acompanha o tema escuro do app.
// São as mesmas do tema claro (core/design/Cores.kt).
private val Azul = CorPdf(0x1F4E79)
private val AzulClaro = CorPdf(0xDCE9F5)
private val Zebra = CorPdf(0xF3F6FA)
private val Branco = CorPdf(0xFFFFFF)
private val TextoForte = CorPdf(0x1A1C1E)
private val TextoSuave = CorPdf(0x5A6068)
private val Divisoria = CorPdf(0xE4E7EB)

private const val Margem = 40.0
private const val Direita = DocumentoPdf.LarguraA4 - Margem
private const val LarguraUtil = Direita - Margem

/** O conteúdo para aqui; abaixo fica o rodapé ("Página 1 de 2"). */
private const val LimiteInferior = DocumentoPdf.AlturaA4 - 56

// Colunas da tabela de itens. A descrição fica com o que sobrar.
private const val ColNumero = 24.0
private const val ColQuantidade = 46.0
private const val ColUnidade = 38.0
private const val ColValor = 82.0
private const val ColTotal = 86.0
private const val ColDescricao = LarguraUtil - ColNumero - ColQuantidade - ColUnidade - ColValor - ColTotal
private const val RespiroCelula = 6.0

private const val CorpoTexto = 9.0
private const val AlturaLinhaTexto = 12.0

/**
 * Desenha o orçamento de cima para baixo: [y] é a altura onde o próximo bloco começa.
 * Quando um bloco não cabe mais, [garantirEspaco] começa outra folha.
 */
private class LayoutOrcamento(
    private val numero: Int,
    private val dados: DadosOrcamento,
    private val prestador: Prestador,
    private val cliente: Cliente
) {
    private val numeroTexto = "Nº " + numero.toString().padStart(4, '0')
    private val documento = DocumentoPdf("Orçamento $numeroTexto - ${cliente.razaoSocial}")
    private var pagina = documento.novaPagina()
    private var y = 0.0

    fun desenhar(): ByteArray {
        cabecalho()
        blocoCliente()
        tabelaItens()
        totais()
        condicoes()
        observacoes()
        rodapes()
        return documento.gerar()
    }

    // --- Cabeçalho (página 1): a "folha timbrada" da empresa ---

    private fun cabecalho() {
        val alturaFaixa = 100.0
        pagina.retangulo(0.0, 0.0, DocumentoPdf.LarguraA4, alturaFaixa, Azul)

        // Esquerda: quem presta o serviço.
        val larguraEmpresa = LarguraUtil * 0.62
        pagina.texto(
            Margem, 44.0,
            cortarComReticencias(prestador.nomeRazaoSocial, FontePdf.NEGRITO, 16.0, larguraEmpresa),
            FontePdf.NEGRITO, 16.0, Branco
        )
        // Até 3 linhas: documento + telefone, e-mail, endereço (cada uma só se estiver preenchida).
        val documentoETelefone = listOfNotNull(
            prestador.cpfCnpj.preenchido()?.let { "${rotuloCpfCnpj(it)} ${formatarCpfCnpj(it)}" },
            prestador.telefone.preenchido()?.let { formatarTelefone(it) }
        ).joinToString("   •   ")
        val linhasEmpresa = listOfNotNull(
            documentoETelefone.ifEmpty { null },
            prestador.email.preenchido(),
            prestador.endereco.preenchido()
        )
        linhasEmpresa.forEachIndexed { i, linha ->
            pagina.texto(
                Margem, 62.0 + i * 13,
                cortarComReticencias(linha, FontePdf.REGULAR, CorpoTexto, larguraEmpresa),
                FontePdf.REGULAR, CorpoTexto, AzulClaro
            )
        }

        // Direita: o que é o documento.
        pagina.textoDireita(Direita, 42.0, "ORÇAMENTO", FontePdf.NEGRITO, 18.0, Branco)
        pagina.textoDireita(Direita, 60.0, numeroTexto, FontePdf.NEGRITO, 12.0, Branco)
        pagina.textoDireita(Direita, 76.0, "Emissão ${dados.dataEmissao.formatarBr()}", FontePdf.REGULAR, CorpoTexto, AzulClaro)
        pagina.textoDireita(Direita, 88.0, "Válido até ${dados.dataValidade.formatarBr()}", FontePdf.REGULAR, CorpoTexto, AzulClaro)

        y = alturaFaixa + 26
    }

    /** Cabeçalho discreto das páginas 2 em diante. */
    private fun cabecalhoContinuacao() {
        pagina.retangulo(0.0, 0.0, DocumentoPdf.LarguraA4, 6.0, Azul)
        pagina.texto(
            Margem, 32.0,
            cortarComReticencias(prestador.nomeRazaoSocial, FontePdf.NEGRITO, 10.0, LarguraUtil * 0.6),
            FontePdf.NEGRITO, 10.0, Azul
        )
        pagina.textoDireita(Direita, 32.0, "Orçamento $numeroTexto (continuação)", FontePdf.REGULAR, CorpoTexto, TextoSuave)
        pagina.linha(Margem, 42.0, Direita, 42.0, Divisoria)
        y = 60.0
    }

    // --- Cliente ---

    private fun blocoCliente() {
        titulo("CLIENTE")
        pagina.texto(Margem, y + 11, cliente.razaoSocial, FontePdf.NEGRITO, 11.0, TextoForte)
        y += 22

        // Campos curtos em duas colunas; o endereço (que costuma ser longo) ocupa a linha toda.
        val larguraColuna = LarguraUtil / 2
        val curtos = listOf(
            rotuloCpfCnpj(cliente.cnpj) to formatarCpfCnpj(cliente.cnpj),
            "Inscrição estadual" to cliente.inscricaoEstadual,
            "Telefone" to formatarTelefone(cliente.telefone),
            "E-mail" to cliente.email
        ).filter { it.second.isNotBlank() }
        curtos.chunked(2).forEach { par ->
            par.forEachIndexed { coluna, (rotulo, valor) ->
                campo(Margem + coluna * larguraColuna, rotulo, valor, larguraColuna - 12)
            }
            y += AlturaLinhaTexto + 2
        }
        if (cliente.endereco.isNotBlank()) {
            campoLongo("Endereço", cliente.endereco)
        }
        y += 14
    }

    // --- Itens ---

    private fun tabelaItens() {
        garantirEspaco(60.0)
        cabecalhoTabela()

        dados.itens.forEachIndexed { indice, item ->
            val linhas = quebrarLinhas(item.descricao, FontePdf.REGULAR, CorpoTexto, ColDescricao - 2 * RespiroCelula)
            val altura = 2 * RespiroCelula + linhas.size * AlturaLinhaTexto

            if (y + altura > LimiteInferior && !paginaVazia()) {
                virarPagina()
                cabecalhoTabela()
            }

            if (indice % 2 == 1) pagina.retangulo(Margem, y, LarguraUtil, altura, Zebra)
            val base = y + RespiroCelula + 8.5 // linha de base da primeira linha de texto

            var x = Margem
            pagina.texto(x + RespiroCelula, base, "${indice + 1}", FontePdf.REGULAR, CorpoTexto, TextoSuave)
            x += ColNumero
            linhas.forEachIndexed { i, linha ->
                pagina.texto(x + RespiroCelula, base + i * AlturaLinhaTexto, linha, FontePdf.REGULAR, CorpoTexto, TextoForte)
            }
            x += ColDescricao + ColQuantidade
            pagina.textoDireita(x - RespiroCelula, base, formatarQuantidade(item.quantidade), FontePdf.REGULAR, CorpoTexto, TextoForte)
            pagina.texto(
                x + RespiroCelula, base,
                cortarComReticencias(item.unidade, FontePdf.REGULAR, CorpoTexto, ColUnidade - 2 * RespiroCelula),
                FontePdf.REGULAR, CorpoTexto, TextoSuave
            )
            x += ColUnidade + ColValor
            pagina.textoDireita(x - RespiroCelula, base, formatarMoeda(item.valorUnitarioCentavos), FontePdf.REGULAR, CorpoTexto, TextoForte)
            x += ColTotal
            pagina.textoDireita(x - RespiroCelula, base, formatarMoeda(item.totalCentavos), FontePdf.NEGRITO, CorpoTexto, TextoForte)

            y += altura
            pagina.linha(Margem, y, Direita, y, Divisoria)
        }
        y += 14
    }

    private fun cabecalhoTabela() {
        val altura = 22.0
        pagina.retangulo(Margem, y, LarguraUtil, altura, Azul)
        val base = y + 14.5
        val tamanho = 8.0
        var x = Margem
        pagina.texto(x + RespiroCelula, base, "#", FontePdf.NEGRITO, tamanho, Branco)
        x += ColNumero
        pagina.texto(x + RespiroCelula, base, "DESCRIÇÃO", FontePdf.NEGRITO, tamanho, Branco)
        x += ColDescricao + ColQuantidade
        pagina.textoDireita(x - RespiroCelula, base, "QTD", FontePdf.NEGRITO, tamanho, Branco)
        pagina.texto(x + RespiroCelula, base, "UN", FontePdf.NEGRITO, tamanho, Branco)
        x += ColUnidade + ColValor
        pagina.textoDireita(x - RespiroCelula, base, "VALOR UNIT.", FontePdf.NEGRITO, tamanho, Branco)
        x += ColTotal
        pagina.textoDireita(x - RespiroCelula, base, "TOTAL", FontePdf.NEGRITO, tamanho, Branco)
        y += altura
    }

    // --- Totais ---

    private fun totais() {
        val subtotal = dados.itens.sumOf { it.totalCentavos }
        val total = subtotal - dados.descontoCentavos
        val temDesconto = dados.descontoCentavos > 0

        garantirEspaco(if (temDesconto) 76.0 else 60.0)

        val largura = 240.0
        val esquerda = Direita - largura
        val respiro = 10.0

        fun linhaValor(rotulo: String, valor: String) {
            pagina.texto(esquerda + respiro, y + 10, rotulo, FontePdf.REGULAR, CorpoTexto, TextoSuave)
            pagina.textoDireita(Direita - respiro, y + 10, valor, FontePdf.REGULAR, CorpoTexto, TextoForte)
            y += 16
        }

        linhaValor("Subtotal", formatarMoeda(subtotal))
        if (temDesconto) linhaValor("Desconto", "- " + formatarMoeda(dados.descontoCentavos))
        y += 4

        val alturaTotal = 30.0
        pagina.retangulo(esquerda, y, largura, alturaTotal, Azul)
        pagina.texto(esquerda + respiro, y + 19.5, "TOTAL", FontePdf.NEGRITO, 10.0, Branco)
        pagina.textoDireita(Direita - respiro, y + 20.5, formatarMoeda(total), FontePdf.NEGRITO, 14.0, Branco)
        y += alturaTotal + 24
    }

    // --- Condições e observações ---

    private fun condicoes() {
        val campos = listOf(
            "Forma de pagamento" to dados.formaPagamento,
            "Prazo de pagamento" to dados.prazoPagamento,
            "Validade da proposta" to "até ${dados.dataValidade.formatarBr()}"
        ).filter { it.second.isNotBlank() }

        garantirEspaco(30.0 + campos.size * (AlturaLinhaTexto + 2))
        titulo("CONDIÇÕES")
        campos.forEach { (rotulo, valor) -> campoLongo(rotulo, valor) }
        y += 12
    }

    private fun observacoes() {
        if (dados.observacoes.isBlank()) return
        garantirEspaco(30.0 + 2 * AlturaLinhaTexto)
        titulo("OBSERVAÇÕES")
        for (linha in quebrarLinhas(dados.observacoes, FontePdf.REGULAR, CorpoTexto, LarguraUtil)) {
            garantirEspaco(AlturaLinhaTexto)
            pagina.texto(Margem, y + 9, linha, FontePdf.REGULAR, CorpoTexto, TextoForte)
            y += AlturaLinhaTexto
        }
    }

    // --- Rodapé (desenhado no fim, quando já se sabe o total de páginas) ---

    private fun rodapes() {
        val total = documento.totalPaginas
        repeat(total) { i ->
            val folha = documento.pagina(i)
            val linhaY = DocumentoPdf.AlturaA4 - 40
            folha.linha(Margem, linhaY, Direita, linhaY, Divisoria)
            folha.texto(Margem, linhaY + 13, "Orçamento $numeroTexto  •  gerado pelo ClickOrçamento", FontePdf.REGULAR, 7.5, TextoSuave)
            folha.textoDireita(Direita, linhaY + 13, "Página ${i + 1} de $total", FontePdf.REGULAR, 7.5, TextoSuave)
        }
    }

    // --- Peças reutilizadas ---

    /** Título de seção: texto azul em caixa alta com um fio azul embaixo. */
    private fun titulo(texto: String) {
        pagina.texto(Margem, y + 9, texto, FontePdf.NEGRITO, 8.5, Azul)
        pagina.linha(Margem, y + 14, Direita, y + 14, Azul, espessura = 0.8)
        y += 22
    }

    /** "Rótulo: valor" numa linha só (corta com "…" se não couber). Não altera [y]. */
    private fun campo(x: Double, rotulo: String, valor: String, larguraMaxima: Double) {
        val textoRotulo = "$rotulo: "
        val larguraRotulo = FontePdf.NEGRITO.largura(textoRotulo, CorpoTexto)
        pagina.texto(x, y + 9, textoRotulo, FontePdf.NEGRITO, CorpoTexto, TextoSuave)
        pagina.texto(
            x + larguraRotulo, y + 9,
            cortarComReticencias(valor, FontePdf.REGULAR, CorpoTexto, larguraMaxima - larguraRotulo),
            FontePdf.REGULAR, CorpoTexto, TextoForte
        )
    }

    /** "Rótulo: valor" na largura toda; o valor quebra em quantas linhas precisar, alinhado depois do rótulo. */
    private fun campoLongo(rotulo: String, valor: String) {
        val textoRotulo = "$rotulo: "
        val larguraRotulo = FontePdf.NEGRITO.largura(textoRotulo, CorpoTexto)
        val linhas = quebrarLinhas(valor, FontePdf.REGULAR, CorpoTexto, LarguraUtil - larguraRotulo)
        pagina.texto(Margem, y + 9, textoRotulo, FontePdf.NEGRITO, CorpoTexto, TextoSuave)
        linhas.forEachIndexed { i, linha ->
            pagina.texto(Margem + larguraRotulo, y + 9 + i * AlturaLinhaTexto, linha, FontePdf.REGULAR, CorpoTexto, TextoForte)
        }
        y += linhas.size * AlturaLinhaTexto + 2
    }

    /** Se o próximo bloco (de [altura] pontos) não cabe nesta folha, começa outra. */
    private fun garantirEspaco(altura: Double) {
        if (y + altura > LimiteInferior && !paginaVazia()) virarPagina()
    }

    private fun virarPagina() {
        pagina = documento.novaPagina()
        cabecalhoContinuacao()
    }

    /**
     * [y] ainda está no topo de uma folha de continuação. Evita abrir folhas em loop
     * se um bloco for maior que a folha inteira.
     */
    private fun paginaVazia() = documento.totalPaginas > 1 && y <= 60.0 + 22.0

    private fun String?.preenchido(): String? = this?.trim()?.ifEmpty { null }
}
