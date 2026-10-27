package com.clickorcamento.app.core.format

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DocumentosTest {

    @Test
    fun cpfVaiSendoFormatadoEnquantoDigita() {
        fun m(bruto: String) = mascarar(bruto, padraoCpfCnpj(bruto)).texto
        assertEquals("", m(""))
        assertEquals("123", m("123"))
        assertEquals("123.4", m("1234"))
        assertEquals("123.456.789-0", m("1234567890"))
        assertEquals("123.456.789-01", m("12345678901"))
    }

    @Test
    fun noDecimoSegundoCaractereViraCnpj() {
        fun m(bruto: String) = mascarar(bruto, padraoCpfCnpj(bruto)).texto
        assertEquals("12.345.678/9012", m("123456789012"))
        assertEquals("12.345.678/0001-90", m("12345678000190"))
    }

    @Test
    fun cnpjAlfanumericoAceitaLetrasEmMaiusculas() {
        assertEquals("12ABC34501DE35", limparCpfCnpj("12.abc.345/01de-35"))
        assertEquals("12.ABC.345/01DE-35", formatarCpfCnpj("12abc34501de35"))
        assertEquals("CNPJ", rotuloCpfCnpj("12ABC34501DE35"))
        // Com letra já é CNPJ, mesmo curto.
        assertEquals("12.A", mascarar("12A", padraoCpfCnpj("12A")).texto)
    }

    @Test
    fun limparTiraPontuacaoECortaNoTamanho() {
        assertEquals("12345678000190", limparCpfCnpj("12.345.678/0001-90"))
        assertEquals("12345678000190", limparCpfCnpj("12.345.678/0001-90999"))
        assertEquals("41999990000", limparTelefone("(41) 99999-0000"))
    }

    @Test
    fun formatarParaExibirAceitaDadoAntigoEDeixaEstranhoComoEsta() {
        assertEquals("123.456.789-01", formatarCpfCnpj("12345678901"))
        assertEquals("123.456.789-01", formatarCpfCnpj("123.456.789-01"))
        assertEquals("98.765.432/0001-10", formatarCpfCnpj("98.765.432/0001-10"))
        assertEquals("123", formatarCpfCnpj(" 123 "))
        assertEquals("", formatarCpfCnpj(""))
        assertEquals("CPF", rotuloCpfCnpj("12345678901"))
        assertEquals("CPF/CNPJ", rotuloCpfCnpj("123"))
    }

    @Test
    fun telefoneFixoECelular() {
        fun m(bruto: String) = mascarar(bruto, padraoTelefone(bruto)).texto
        assertEquals("(4", m("4"))
        assertEquals("(41) 3", m("413"))
        assertEquals("(41) 3333-4444", m("4133334444"))
        assertEquals("(41) 99999-0000", m("41999990000"))
        assertEquals("(41) 99999-0000", formatarTelefone("41999990000"))
        assertEquals("(41) 3333-4444", formatarTelefone("41 3333 4444"))
        assertEquals("+55 41 99999-0000", formatarTelefone("+55 41 99999-0000"))
    }

    @Test
    fun posicoesDizemOndeCadaCaractereFoiParar() {
        // "1234" -> "123.4": o 4º dígito fica no índice 4 (depois do ponto).
        assertContentEquals(intArrayOf(0, 1, 2, 4), mascarar("1234", PadraoCpf).posicoes)
        // "(41) 3": '4'->1, '1'->2, '3'->5.
        assertContentEquals(intArrayOf(1, 2, 5), mascarar("413", PadraoTelefoneFixo).posicoes)
    }
}
