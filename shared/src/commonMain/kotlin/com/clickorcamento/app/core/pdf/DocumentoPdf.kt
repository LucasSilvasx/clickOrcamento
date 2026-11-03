package com.clickorcamento.app.core.pdf

import kotlin.math.abs
import kotlin.math.roundToLong

/** Cor RGB do PDF. `CorPdf(0x1F4E79)` = o azul da planilha. */
class CorPdf(rgb: Int) {
    internal val r = (rgb shr 16 and 0xFF) / 255.0
    internal val g = (rgb shr 8 and 0xFF) / 255.0
    internal val b = (rgb and 0xFF) / 255.0
}

/**
 * Um escritor de PDF mínimo, em Kotlin puro (roda igual no desktop, Android e web).
 *
 * Cada folha guarda uma lista de comandos de desenho ("pinte um retângulo azul aqui", "escreva este texto
 * ali"). Esta classe junta as folhas e escreve o arquivo no formato que os
 * leitores entendem: objetos numerados + um índice (xref) dizendo em que byte
 * cada objeto começa.
 *
 * Só sabe o que o orçamento precisa: retângulo, linha e texto em Helvetica.
 * Tamanho A4, medidas em pontos (1 pt = 1/72 polegada; A4 = 595 x 842 pt).
 */
class DocumentoPdf(private val titulo: String) {
    private val paginas = mutableListOf<PaginaPdf>()

    val totalPaginas: Int get() = paginas.size

    fun novaPagina(): PaginaPdf = PaginaPdf().also { paginas += it }

    fun pagina(indice: Int): PaginaPdf = paginas[indice]

    /**
     * Monta o arquivo. Tudo é escrito em ASCII puro (os acentos viram códigos
     * `\ooo` dentro do texto), então 1 caractere = 1 byte e a posição de cada
     * objeto no texto já é a posição em bytes que o índice exige.
     */
    fun gerar(): ByteArray {
        val saida = StringBuilder()
        val posicoes = mutableListOf<Int>() // posição em bytes de cada objeto (objeto 1 no índice 0)

        fun objeto(corpo: String) {
            posicoes += saida.length
            saida.append("${posicoes.size} 0 obj\n").append(corpo).append("\nendobj\n")
        }

        // Numeração fixa dos primeiros objetos; as páginas vêm depois, em pares (página + conteúdo).
        val primeiraPagina = 6
        val kids = paginas.indices.joinToString(" ") { "${primeiraPagina + it * 2} 0 R" }

        saida.append("%PDF-1.4\n")
        objeto("<< /Type /Catalog /Pages 2 0 R >>")                                               // 1
        objeto("<< /Type /Pages /Kids [$kids] /Count ${paginas.size} >>")                         // 2
        objeto(fonte(FontePdf.REGULAR))                                                           // 3
        objeto(fonte(FontePdf.NEGRITO))                                                           // 4
        objeto("<< /Title ${textoPdf(titulo)} /Creator ${textoPdf("ClickOrçamento")} >>")        // 5
        paginas.forEachIndexed { i, pagina ->
            val numeroConteudo = primeiraPagina + i * 2 + 1
            objeto(
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${num(LarguraA4)} ${num(AlturaA4)}] " +
                    "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents $numeroConteudo 0 R >>"
            )
            val conteudo = pagina.comandos.toString()
            objeto("<< /Length ${conteudo.length} >>\nstream\n$conteudo\nendstream")
        }

        // Índice: uma linha de exatamente 20 bytes por objeto, com a posição dele.
        val inicioIndice = saida.length
        saida.append("xref\n0 ${posicoes.size + 1}\n")
        saida.append("0000000000 65535 f \n")
        posicoes.forEach { saida.append(it.toString().padStart(10, '0')).append(" 00000 n \n") }
        saida.append("trailer\n<< /Size ${posicoes.size + 1} /Root 1 0 R /Info 5 0 R >>\n")
        saida.append("startxref\n$inicioIndice\n%%EOF\n")

        return saida.toString().encodeToByteArray()
    }

    private fun fonte(f: FontePdf) =
        "<< /Type /Font /Subtype /Type1 /BaseFont /${f.nomePdf} /Encoding /WinAnsiEncoding >>"

    companion object {
        const val LarguraA4 = 595.28
        const val AlturaA4 = 841.89
    }
}

/**
 * Uma folha. As coordenadas que recebe são "de tela": (0, 0) no canto de cima
 * à esquerda e `y` crescendo para baixo, como no Compose. O PDF conta `y` de
 * baixo para cima, então cada comando converte com `AlturaA4 - y`.
 */
class PaginaPdf internal constructor() {
    internal val comandos = StringBuilder()

    private fun yPdf(y: Double) = DocumentoPdf.AlturaA4 - y

    /** Retângulo preenchido; ([x], [y]) é o canto de cima à esquerda. */
    fun retangulo(x: Double, y: Double, largura: Double, altura: Double, cor: CorPdf) {
        comandos.append("${cor(cor)} rg ${num(x)} ${num(yPdf(y + altura))} ${num(largura)} ${num(altura)} re f\n")
    }

    fun linha(x1: Double, y1: Double, x2: Double, y2: Double, cor: CorPdf, espessura: Double = 0.5) {
        comandos.append(
            "${cor(cor)} RG ${num(espessura)} w ${num(x1)} ${num(yPdf(y1))} m ${num(x2)} ${num(yPdf(y2))} l S\n"
        )
    }

    /** Texto com a linha de base em [y] (a linha onde as letras "pisam"). */
    fun texto(x: Double, y: Double, texto: String, fonte: FontePdf, tamanho: Double, cor: CorPdf) {
        if (texto.isEmpty()) return
        comandos.append(
            "BT /${fonte.recurso} ${num(tamanho)} Tf ${cor(cor)} rg ${num(x)} ${num(yPdf(y))} Td ${textoPdf(texto)} Tj ET\n"
        )
    }

    /** Texto alinhado à direita: termina exatamente em [xDireita]. */
    fun textoDireita(xDireita: Double, y: Double, texto: String, fonte: FontePdf, tamanho: Double, cor: CorPdf) {
        texto(xDireita - fonte.largura(texto, tamanho), y, texto, fonte, tamanho, cor)
    }

    private fun cor(c: CorPdf) = "${num(c.r)} ${num(c.g)} ${num(c.b)}"
}

/**
 * Número no formato do PDF: ponto decimal, no máximo 3 casas, nunca notação
 * científica. Não dá para usar `Double.toString()`: ele muda de plataforma para
 * plataforma (no JS, 1.0 vira "1"; valores pequenos viram "1.0E-4").
 */
internal fun num(valor: Double): String {
    val milesimos = (valor * 1000).roundToLong()
    val sinal = if (milesimos < 0) "-" else ""
    val inteiro = abs(milesimos) / 1000
    val fracao = (abs(milesimos) % 1000).toString().padStart(3, '0').trimEnd('0')
    return if (fracao.isEmpty()) "$sinal$inteiro" else "$sinal$inteiro.$fracao"
}

/**
 * Texto entre parênteses, como o PDF pede: `(Orçamento)`. Parênteses e barra
 * invertida ganham escape; tudo fora do ASCII vira código octal (`ç` -> `\347`),
 * o que mantém o arquivo inteiro em ASCII.
 */
internal fun textoPdf(texto: String): String = buildString {
    append('(')
    for (c in texto) {
        val codigo = codigoWinAnsi(c)
        when {
            c == '(' || c == ')' || c == '\\' -> append('\\').append(c)
            codigo in 32..126 -> append(codigo.toChar())
            else -> append('\\').append(codigo.toString(8).padStart(3, '0'))
        }
    }
    append(')')
}
