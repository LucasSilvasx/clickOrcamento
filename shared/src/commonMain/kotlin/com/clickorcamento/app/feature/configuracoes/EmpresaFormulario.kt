package com.clickorcamento.app.feature.configuracoes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Formulário de empresa (criar e editar). Stateless: recebe o
 * [FormularioEmpresa] e devolve o formulário inteiro já alterado em
 * `aoMudar` (`form.copy(apelido = it)`), que o ViewModel guarda.
 *
 * Largura limitada (docs/UI-UX.md, seção 6): formulário simples não estica.
 */
@Composable
fun EmpresaFormulario(
    form: FormularioEmpresa,
    aoMudar: (FormularioEmpresa) -> Unit,
    aoSalvar: () -> Unit,
    aoCancelar: () -> Unit
) {
    val editavel = !form.salvando

    Column(Modifier.fillMaxWidth()) {
        // Cabeçalho: seta de voltar + título.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = aoCancelar, enabled = editavel) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
            }
            Text(form.titulo, style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Campo(
                valor = form.apelido,
                rotulo = "Apelido *",
                dica = "Nome curto que aparece no seletor, ex.: Marcenaria",
                habilitado = editavel,
                aoMudar = { aoMudar(form.copy(apelido = it)) }
            )
            Campo(
                valor = form.nomeRazaoSocial,
                rotulo = "Nome ou razão social *",
                habilitado = editavel,
                aoMudar = { aoMudar(form.copy(nomeRazaoSocial = it)) }
            )
            Campo(
                valor = form.cpfCnpj,
                rotulo = "CPF ou CNPJ",
                habilitado = editavel,
                aoMudar = { aoMudar(form.copy(cpfCnpj = it)) }
            )
            Campo(
                valor = form.endereco,
                rotulo = "Endereço",
                habilitado = editavel,
                linhasMinimas = 2,
                aoMudar = { aoMudar(form.copy(endereco = it)) }
            )
            Campo(
                valor = form.telefone,
                rotulo = "Telefone",
                habilitado = editavel,
                teclado = KeyboardType.Phone,
                aoMudar = { aoMudar(form.copy(telefone = it)) }
            )
            Campo(
                valor = form.email,
                rotulo = "E-mail",
                habilitado = editavel,
                teclado = KeyboardType.Email,
                aoMudar = { aoMudar(form.copy(email = it)) }
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(20.dp))

        Text("Condições padrão", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Entram preenchidas em cada orçamento novo desta empresa e podem ser editadas lá.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Campo(
                valor = form.formaPagamento,
                rotulo = "Forma de pagamento",
                dica = "Ex.: Pix, boleto, transferência",
                habilitado = editavel,
                aoMudar = { aoMudar(form.copy(formaPagamento = it)) }
            )
            Campo(
                valor = form.prazoPagamento,
                rotulo = "Prazo de pagamento",
                dica = "Ex.: 50% na entrada e 50% na entrega",
                habilitado = editavel,
                aoMudar = { aoMudar(form.copy(prazoPagamento = it)) }
            )
            Campo(
                valor = form.observacoes,
                rotulo = "Observações",
                dica = "Dados bancários, chave Pix, garantia...",
                habilitado = editavel,
                linhasMinimas = 3,
                ultimo = true,
                aoMudar = { aoMudar(form.copy(observacoes = it)) }
            )
        }

        MensagemErro(form.erro)

        Spacer(Modifier.height(24.dp))

        // Um botão de destaque só (Salvar); Cancelar é secundário.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            OutlinedButton(
                onClick = aoCancelar,
                enabled = editavel,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = aoSalvar,
                enabled = form.podeSalvar,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                if (form.salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** Campo de texto padrão do formulário: cantos 12.dp, `singleLine` quando cabe numa linha. */
@Composable
private fun Campo(
    valor: String,
    rotulo: String,
    aoMudar: (String) -> Unit,
    habilitado: Boolean,
    dica: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    linhasMinimas: Int = 1,
    ultimo: Boolean = false
) {
    val umaLinha = linhasMinimas == 1
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        supportingText = dica?.let { { Text(it) } },
        singleLine = umaLinha,
        minLines = linhasMinimas,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado,
            // Campo de várias linhas usa o Enter para quebrar linha; os demais avançam.
            imeAction = if (ultimo) ImeAction.Default else ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Mesma caixa de erro do login (errorContainer, cantos 10.dp, entra com fade). */
@Composable
private fun MensagemErro(texto: String?) {
    AnimatedVisibility(visible = texto != null, enter = fadeIn(), exit = fadeOut()) {
        Column {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text(
                    text = texto.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
