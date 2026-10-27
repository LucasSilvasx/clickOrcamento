package com.clickorcamento.app.core.format

/*
 * CPF, CNPJ e telefone.
 *
 * Regra geral: o que vai para o banco é só o "miolo" (sem pontuação), ex.:
 * "12345678000190". A pontuação é colocada só na hora de mostrar:
 * - no campo de digitação, pelo VisualTransformation (core/design/MascaraVisual.kt);
 * - em listas e no PDF, por formatarCpfCnpj() / formatarTelefone().
 * As funções de exibição aceitam também dados antigos, digitados com pontuação.
 */

const val PadraoCpf = "###.###.###-##"
const val PadraoCnpj = "##.###.###/####-##"
const val PadraoTelefoneFixo = "(##) ####-####"
const val PadraoCelular = "(##) #####-####"

/**
 * Texto encaixado numa máscara + onde cada caractere digitado foi parar
 * (`posicoes[i]` = índice no [texto] do i-ésimo caractere digitado).
 * As posições servem para o cursor andar certo no campo.
 */
class TextoMascarado(val texto: String, val posicoes: IntArray)

/**
 * Encaixa [bruto] no [padrao] ('#' = um caractere digitado; o resto é separador).
 * O separador só aparece quando já existe caractere depois dele:
 * "123" -> "123", "1234" -> "123.4". Assim o backspace apaga normalmente.
 */
fun mascarar(bruto: String, padrao: String): TextoMascarado {
    val texto = StringBuilder()
    val posicoes = IntArray(bruto.length)
    var i = 0
    for (c in padrao) {
        if (i == bruto.length) break
        if (c == '#') {
            posicoes[i] = texto.length
            texto.append(bruto[i])
            i++
        } else {
            texto.append(c)
        }
    }
    // Se sobrou caractere além do padrão (não deveria, os "limpar" cortam antes), cola no fim.
    while (i < bruto.length) {
        posicoes[i] = texto.length
        texto.append(bruto[i])
        i++
    }
    return TextoMascarado(texto.toString(), posicoes)
}

// ───────────── CPF / CNPJ ─────────────

/**
 * Só dígitos e letras (A-Z), em maiúsculas. Letras porque o CNPJ passou a ser
 * alfanumérico (Receita Federal, a partir de julho/2026: "12.ABC.345/01DE-35").
 */
private fun miolo(texto: String): String =
    texto.filter { it in '0'..'9' || it in 'A'..'Z' || it in 'a'..'z' }.uppercase()

/** O que o campo guarda: o miolo, no máximo 14 caracteres (tamanho do CNPJ). */
fun limparCpfCnpj(texto: String): String = miolo(texto).take(14)

/** Até 11 caracteres, todos dígitos = ainda é CPF. Passou disso, ou tem letra = CNPJ. */
private fun pareceCpf(bruto: String): Boolean = bruto.length <= 11 && bruto.all { it.isDigit() }

/** Máscara usada enquanto digita (decide CPF ou CNPJ pelo que já foi digitado). */
fun padraoCpfCnpj(bruto: String): String = if (pareceCpf(bruto)) PadraoCpf else PadraoCnpj

/**
 * Para exibir: "12345678000190" -> "12.345.678/0001-90". Se não tiver o tamanho
 * de um CPF (11) ou CNPJ (14), devolve o texto como está (não inventa pontuação).
 */
fun formatarCpfCnpj(texto: String): String {
    val bruto = miolo(texto)
    return when {
        bruto.length == 11 && pareceCpf(bruto) -> mascarar(bruto, PadraoCpf).texto
        bruto.length == 14 -> mascarar(bruto, PadraoCnpj).texto
        else -> texto.trim()
    }
}

/** Rótulo certo para o documento: "CPF", "CNPJ" ou, se não der para saber, "CPF/CNPJ". */
fun rotuloCpfCnpj(texto: String): String {
    val bruto = miolo(texto)
    return when {
        bruto.length == 11 && pareceCpf(bruto) -> "CPF"
        bruto.length == 14 -> "CNPJ"
        else -> "CPF/CNPJ"
    }
}

// ───────────── Telefone ─────────────

/** O que o campo guarda: só dígitos, no máximo 11 (DDD + celular com 9). */
fun limparTelefone(texto: String): String = texto.filter { it.isDigit() }.take(11)

/** Até 10 dígitos = fixo "(11) 3456-7890"; 11 = celular "(11) 98765-4321". */
fun padraoTelefone(bruto: String): String = if (bruto.length <= 10) PadraoTelefoneFixo else PadraoCelular

/** Para exibir. Fora de 10/11 dígitos (ex.: "+55...", ramal) devolve como está. */
fun formatarTelefone(texto: String): String {
    val digitos = texto.filter { it.isDigit() }
    return when (digitos.length) {
        10, 11 -> mascarar(digitos, padraoTelefone(digitos)).texto
        else -> texto.trim()
    }
}
