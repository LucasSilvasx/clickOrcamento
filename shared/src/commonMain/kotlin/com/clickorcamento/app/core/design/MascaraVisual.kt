package com.clickorcamento.app.core.design

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.clickorcamento.app.core.format.mascarar
import com.clickorcamento.app.core.format.padraoCpfCnpj
import com.clickorcamento.app.core.format.padraoTelefone

/**
 * Máscara "só de desenho" para campos de texto.
 *
 * Por que não formatar direto no onValueChange? Porque aí o texto do estado
 * fica maior do que o usuário digitou (ganha "." "/" "-") e o Compose deixa o
 * cursor na posição antiga, um caractere antes do fim. Com VisualTransformation
 * o estado guarda só o miolo ("12345678000190") e a pontuação é desenhada por
 * cima. É como o PasswordVisualTransformation do login, que desenha "•••"
 * sem mudar a senha guardada.
 *
 * O OffsetMapping traduz a posição do cursor entre os dois mundos:
 * miolo (o que está no estado) <-> texto formatado (o que aparece na tela).
 *
 * [padraoPara] escolhe a máscara pelo que já foi digitado (CPF vira CNPJ
 * no 12º caractere; telefone fixo vira celular no 11º dígito).
 */
class MascaraVisual(private val padraoPara: (String) -> String) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val mascarado = mascarar(text.text, padraoPara(text.text))
        val posicoes = mascarado.posicoes

        val mapa = object : OffsetMapping {
            // Cursor antes do i-ésimo caractere digitado -> antes dele no texto formatado.
            override fun originalToTransformed(offset: Int): Int =
                if (offset < posicoes.size) posicoes[offset] else mascarado.texto.length

            // Posição na tela -> quantos caracteres digitados ficam antes dela.
            override fun transformedToOriginal(offset: Int): Int =
                posicoes.count { it < offset }
        }
        return TransformedText(AnnotatedString(mascarado.texto), mapa)
    }
}

/** CPF (###.###.###-##) ou CNPJ (##.###.###/####-##), decidido enquanto digita. */
val MascaraCpfCnpj = MascaraVisual(::padraoCpfCnpj)

/** (##) ####-#### ou (##) #####-####. */
val MascaraTelefone = MascaraVisual(::padraoTelefone)
