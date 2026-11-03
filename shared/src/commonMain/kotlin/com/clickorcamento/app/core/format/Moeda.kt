package com.clickorcamento.app.core.format

import kotlin.math.roundToLong

/**
 * Dinheiro vive em centavos (Long) no app inteiro; só vira texto aqui.
 * 130000 -> "1.300,00". Sem `java.text`/`String.format`: não existem no Wasm.
 */
fun formatarValor(centavos: Long): String {
    val negativo = centavos < 0
    val absoluto = if (negativo) -centavos else centavos
    val reais = (absoluto / 100).toString()
    val cents = (absoluto % 100).toString().padStart(2, '0')
    // Agrupa de 3 em 3 a partir da direita: "1300" -> "1.300".
    val milhares = reais.reversed().chunked(3).joinToString(".").reversed()
    return (if (negativo) "-" else "") + "$milhares,$cents"
}

/** 130000 -> "R$ 1.300,00". */
fun formatarMoeda(centavos: Long): String = "R$ " + formatarValor(centavos)

/**
 * Lê o que o usuário digitou e devolve centavos, ou `null` se estiver em
 * branco/inválido. Aceita `1100`, `1.100`, `1.100,00`, `1100,5` e `R$ 1.100,00`.
 *
 * Regra do ponto: "1.100" (grupos de 3 dígitos) é milhar; "1100.50" é decimal.
 * Casas decimais além da segunda são descartadas (não arredonda).
 */
fun lerCentavos(texto: String): Long? {
    val t = texto.trim().removePrefix("R$").trim().replace(" ", "")
    if (t.isEmpty()) return null

    val inteiro: String
    val fracao: String
    when {
        ',' in t -> {
            inteiro = t.substringBefore(',').replace(".", "")
            fracao = t.substringAfter(',')
        }
        '.' in t && !t.matches(Regex("""\d{1,3}(\.\d{3})+""")) -> {
            inteiro = t.substringBefore('.')
            fracao = t.substringAfter('.')
        }
        else -> {
            inteiro = t.replace(".", "")
            fracao = ""
        }
    }

    if (inteiro.isEmpty() && fracao.isEmpty()) return null
    if (!inteiro.all { it.isDigit() } || !fracao.all { it.isDigit() }) return null
    if (inteiro.length > 12) return null

    val centavos = (fracao + "00").take(2).toLong()
    return inteiro.ifEmpty { "0" }.toLong() * 100 + centavos
}

/** Quantidade aceita vírgula ou ponto: "1,5" e "1.5" valem 1,5. */
fun lerQuantidade(texto: String): Double? =
    texto.trim().replace(',', '.').toDoubleOrNull()

/** 2.0 -> "2"; 1.5 -> "1,5"; 0.125 -> "0,125". */
fun formatarQuantidade(quantidade: Double): String {
    if (quantidade == quantidade.toLong().toDouble()) return quantidade.toLong().toString()
    val arredondada = (quantidade * 1000).roundToLong() / 1000.0
    return arredondada.toString().replace('.', ',')
}

/** Total de uma linha: quantidade x valor unitário, arredondado ao centavo. */
fun totalDaLinha(quantidade: Double, valorUnitarioCentavos: Long): Long =
    (quantidade * valorUnitarioCentavos).roundToLong()
