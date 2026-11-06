package com.clickorcamento.app.core.erro

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ErrosTest {

    @Test
    fun devolveOResultadoQuandoDaCerto() {
        assertEquals(7, tentar { 7 }.getOrNull())
    }

    @Test
    fun transformaFalhaComumEmResultado() {
        val resultado = tentar<Int> { error("falhou") }
        assertTrue(resultado.isFailure)
    }

    @Test
    fun deixaOCancelamentoSubir() {
        assertFailsWith<CancellationException> {
            tentar<Int> { throw CancellationException("tela fechada") }
        }
    }
}
