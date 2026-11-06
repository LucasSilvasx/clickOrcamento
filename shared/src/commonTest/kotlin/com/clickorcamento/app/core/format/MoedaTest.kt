package com.clickorcamento.app.core.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoedaTest {

    @Test
    fun quantidadeAceitaVirgulaPontoEAteTresCasas() {
        assertEquals(1.0, lerQuantidade("1"))
        assertEquals(1.5, lerQuantidade("1,5"))
        assertEquals(1.5, lerQuantidade("1.5"))
        assertEquals(0.285, lerQuantidade("0,285"))
        assertEquals(0.5, lerQuantidade(",5"))
        assertEquals(2.0, lerQuantidade("2,"))
        assertEquals(1.5, lerQuantidade(" 1,5 "))
    }

    // NaN e Infinity são texto válido para toDoubleOrNull, mas derrubavam o cálculo do total.
    @Test
    fun quantidadeRecusaOQueNaoENumeroComum() {
        listOf("NaN", "Infinity", "-Infinity", "1e999", "1e3", "abc", "", ",", "-1", "1.000,5").forEach {
            assertNull(lerQuantidade(it), "deveria recusar \"$it\"")
        }
    }

    // A coluna é numeric(12,3): passar disso faria a tela e o banco guardarem valores diferentes.
    @Test
    fun quantidadeRecusaMaisDoQueOBancoGuarda() {
        assertNull(lerQuantidade("1,2345"))
        assertNull(lerQuantidade("1234567890"))
    }

    // A view orcamento_resumo calcula em numeric. Com Double, 0,285 x 100 dava 28 na tela e 29 na lista.
    @Test
    fun totalDaLinhaArredondaIgualAoBanco() {
        assertEquals(29, totalDaLinha(0.285, 100))
        assertEquals(1, totalDaLinha(0.5, 1))
        assertEquals(833, totalDaLinha(2.5, 333))
        assertEquals(0, totalDaLinha(0.001, 1))
        assertEquals(130000, totalDaLinha(1.0, 130000))
        assertEquals(9999999990, totalDaLinha(999999.999, 10000))
    }

    @Test
    fun formataQuantidadeSemNotacaoCientifica() {
        assertEquals("2", formatarQuantidade(2.0))
        assertEquals("1,5", formatarQuantidade(1.5))
        assertEquals("0,125", formatarQuantidade(0.125))
        assertEquals("0,285", formatarQuantidade(0.285))
        assertEquals("12345678,5", formatarQuantidade(12345678.5))
    }

    @Test
    fun lerCentavosEntendeOsFormatosDigitados() {
        assertEquals(110000, lerCentavos("1100"))
        assertEquals(110000, lerCentavos("1.100"))
        assertEquals(110000, lerCentavos("1.100,00"))
        assertEquals(110050, lerCentavos("1100,5"))
        assertEquals(110050, lerCentavos("1100.50"))
        assertEquals(110000, lerCentavos("R$ 1.100,00"))
    }

    @Test
    fun lerCentavosRecusaInvalidos() {
        listOf("", "abc", "-5", ",", "1,2,3").forEach {
            assertNull(lerCentavos(it), "deveria recusar \"$it\"")
        }
    }
}
