package com.clickorcamento.app.core.pdf

/**
 * As duas fontes do PDF: Helvetica e Helvetica-Bold.
 *
 * Por que Helvetica? Ela é uma das 14 "fontes padrão" do PDF: todo leitor
 * (Acrobat, Chrome, visualizador do Android) já tem as duas embutidas. Assim o
 * arquivo não precisa carregar a fonte dentro dele e fica com poucos KB.
 *
 * O preço disso: o PDF não sabe medir texto. Quem decide onde quebrar a linha
 * ou como alinhar um valor à direita somos nós, e para isso precisamos da
 * largura de cada letra. As tabelas abaixo saíram dos arquivos de métrica
 * oficiais da Adobe (Helvetica.afm / Helvetica-Bold.afm), já na ordem da
 * codificação WinAnsi (códigos 32 a 255). Unidade: milésimos do tamanho da
 * fonte (em corpo 10, uma letra de largura 556 ocupa 5,56 pt).
 */
enum class FontePdf(internal val nomePdf: String, internal val recurso: String, private val larguras: IntArray) {
    REGULAR(
        "Helvetica", "F1",
        intArrayOf(
            278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
            1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
            333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
            556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584, 0,
            556, 0, 222, 556, 333, 1000, 556, 556, 333, 1000, 667, 333, 1000, 0, 611, 0,
            0, 222, 222, 333, 333, 350, 556, 1000, 333, 1000, 500, 333, 944, 0, 500, 667,
            278, 333, 556, 556, 556, 556, 260, 556, 333, 737, 370, 556, 584, 333, 737, 333,
            400, 584, 333, 333, 333, 556, 537, 278, 333, 333, 365, 556, 834, 834, 834, 611,
            667, 667, 667, 667, 667, 667, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
            722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
            556, 556, 556, 556, 556, 556, 889, 500, 556, 556, 556, 556, 278, 278, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 584, 611, 556, 556, 556, 556, 500, 556, 500
        )
    ),
    NEGRITO(
        "Helvetica-Bold", "F2",
        intArrayOf(
            278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
            556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
            975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
            667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
            333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
            611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584, 0,
            556, 0, 278, 556, 500, 1000, 556, 556, 333, 1000, 667, 333, 1000, 0, 611, 0,
            0, 278, 278, 500, 500, 350, 556, 1000, 333, 1000, 556, 333, 944, 0, 500, 667,
            278, 333, 556, 556, 556, 556, 280, 556, 333, 737, 370, 556, 584, 333, 737, 333,
            400, 584, 333, 333, 333, 611, 556, 278, 333, 333, 365, 556, 834, 834, 834, 611,
            722, 722, 722, 722, 722, 722, 1000, 722, 667, 667, 667, 667, 278, 278, 278, 278,
            722, 722, 778, 778, 778, 778, 778, 584, 778, 722, 722, 722, 722, 667, 667, 611,
            556, 556, 556, 556, 556, 556, 889, 556, 556, 556, 556, 556, 278, 278, 278, 278,
            611, 611, 611, 611, 611, 611, 611, 584, 611, 611, 611, 611, 611, 556, 611, 556
        )
    );

    /** Largura do texto em pontos (1 pt = 1/72 de polegada), no [tamanho] de fonte dado. */
    fun largura(texto: String, tamanho: Double): Double {
        var soma = 0
        for (c in texto) soma += larguras[codigoWinAnsi(c) - 32]
        return soma * tamanho / 1000.0
    }
}

/**
 * Converte um caractere para o código que ele tem na WinAnsi (a codificação
 * do Windows "Latin-1"), que é a que o PDF usa com as fontes padrão.
 *
 * Todos os acentos do português (á, ã, â, ç, é, ê, í, ó, õ, ô, ú) e o "º" de
 * "Nº" estão nela. O que não estiver (emoji, letras de outros alfabetos) vira
 * "?", para nunca quebrar o arquivo.
 */
internal fun codigoWinAnsi(c: Char): Int {
    val u = c.code
    return when {
        u in 32..126 -> u
        u in 0xA0..0xFF -> u // o trecho "Latin-1" é igual ao Unicode
        else -> ExtrasWinAnsi[c] ?: when (c) {
            '\t' -> ' '.code
            else -> '?'.code
        }
    }
}

/** Os caracteres "tipográficos" que a WinAnsi guarda nos códigos 0x80 a 0x9F. */
private val ExtrasWinAnsi = mapOf(
    '€' to 0x80, '‚' to 0x82, 'ƒ' to 0x83, '„' to 0x84, '…' to 0x85, '†' to 0x86, '‡' to 0x87,
    'ˆ' to 0x88, '‰' to 0x89, 'Š' to 0x8A, '‹' to 0x8B, 'Œ' to 0x8C, 'Ž' to 0x8E,
    '‘' to 0x91, '’' to 0x92, '“' to 0x93, '”' to 0x94, '•' to 0x95, '–' to 0x96, '—' to 0x97,
    '˜' to 0x98, '™' to 0x99, 'š' to 0x9A, '›' to 0x9B, 'œ' to 0x9C, 'ž' to 0x9E, 'Ÿ' to 0x9F
)

/**
 * Quebra o texto em linhas que caibam em [larguraMaxima].
 *
 * Respeita as quebras que o usuário digitou (Enter) e quebra entre palavras.
 * Uma palavra sozinha maior que a linha (um link, por exemplo) é cortada letra
 * a letra, senão ela vazaria para fora da coluna.
 */
fun quebrarLinhas(texto: String, fonte: FontePdf, tamanho: Double, larguraMaxima: Double): List<String> {
    val linhas = mutableListOf<String>()
    for (paragrafo in texto.replace("\r", "").split('\n')) {
        var atual = ""
        for (palavra in paragrafo.split(' ').filter { it.isNotEmpty() }) {
            val tentativa = if (atual.isEmpty()) palavra else "$atual $palavra"
            when {
                fonte.largura(tentativa, tamanho) <= larguraMaxima -> atual = tentativa
                fonte.largura(palavra, tamanho) <= larguraMaxima -> {
                    linhas += atual
                    atual = palavra
                }
                else -> {
                    // Palavra gigante: fecha a linha atual e vai picando a palavra.
                    if (atual.isNotEmpty()) linhas += atual
                    atual = ""
                    for (c in palavra) {
                        if (fonte.largura(atual + c, tamanho) > larguraMaxima && atual.isNotEmpty()) {
                            linhas += atual
                            atual = ""
                        }
                        atual += c
                    }
                }
            }
        }
        linhas += atual // parágrafo vazio vira linha em branco, como no texto original
    }
    return linhas
}

/** Corta o texto com "…" se ele não couber em [larguraMaxima] (usado onde só cabe uma linha). */
fun cortarComReticencias(texto: String, fonte: FontePdf, tamanho: Double, larguraMaxima: Double): String {
    if (fonte.largura(texto, tamanho) <= larguraMaxima) return texto
    var corte = texto
    while (corte.isNotEmpty() && fonte.largura("$corte…", tamanho) > larguraMaxima) {
        corte = corte.dropLast(1)
    }
    return corte.trimEnd() + "…"
}
