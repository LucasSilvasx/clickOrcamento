package com.clickorcamento.app.core.format

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** Data de hoje no fuso do aparelho. `kotlinx-datetime` funciona nos 3 alvos (java.time não). */
fun hoje(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

fun LocalDate.maisDias(dias: Int): LocalDate = plus(dias, DateTimeUnit.DAY)

/** 2026-10-13 -> "13/10/2026". */
fun LocalDate.formatarBr(): String =
    "${day.toString().padStart(2, '0')}/${month.number.toString().padStart(2, '0')}/$year"

/** "13/10/2026" -> LocalDate, ou `null` se estiver incompleta ou for uma data que não existe (31/02). */
fun lerDataBr(texto: String): LocalDate? {
    val partes = texto.trim().split('/')
    if (partes.size != 3) return null
    val dia = partes[0].toIntOrNull() ?: return null
    val mes = partes[1].toIntOrNull() ?: return null
    val ano = partes[2].toIntOrNull() ?: return null
    if (partes[2].length != 4) return null
    return try {
        LocalDate(ano, mes, dia)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Máscara de digitação: o usuário digita só números ("13102026") e o campo
 * mostra "13/10/2026". A barra só aparece quando há dígito depois dela, assim
 * o backspace funciona normalmente.
 */
fun mascararData(entrada: String): String {
    val digitos = entrada.filter { it.isDigit() }.take(8)
    return buildString {
        digitos.forEachIndexed { i, c ->
            if (i == 2 || i == 4) append('/')
            append(c)
        }
    }
}

/** "Agropecuária Lago Azul Ltda" -> "AL". Usado nos avatares. */
fun iniciaisDe(nome: String): String =
    nome.trim().split(' ').filter { it.isNotEmpty() && it[0].isLetterOrDigit() }
        .take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
