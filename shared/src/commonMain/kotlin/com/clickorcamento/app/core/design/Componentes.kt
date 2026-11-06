package com.clickorcamento.app.core.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clickorcamento.app.core.format.limparCpfCnpj
import com.clickorcamento.app.core.format.limparTelefone

/**
 * Componentes visuais reutilizados por Clientes, Orçamentos e Editor.
 * TODO: Configurações ainda usa cópias privadas destas peças.
 */

/** Abaixo desta largura o app usa o layout de celular. */
val LarguraMinimaDesktop = 600.dp

/** Pílula: `primaryContainer` por padrão, `surfaceVariant` quando [neutro]. */
@Composable
fun Badge(texto: String, modifier: Modifier = Modifier, neutro: Boolean = false) {
    val cores = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (neutro) cores.surfaceVariant else cores.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (neutro) cores.onSurfaceVariant else cores.onPrimaryContainer,
            maxLines = 1
        )
    }
}

/** Caixa de mensagem: erro (`errorContainer`) ou aviso/sucesso (`primaryContainer`). */
@Composable
fun CaixaMensagem(texto: String, modifier: Modifier = Modifier, erro: Boolean = true) {
    val cores = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (erro) cores.errorContainer else cores.primaryContainer)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = if (erro) cores.onErrorContainer else cores.onPrimaryContainer
        )
    }
}

/** Mesma caixa, mas que entra e sai com fade (some quando [texto] é `null`). */
@Composable
fun CaixaMensagemAnimada(texto: String?, erro: Boolean = true) {
    AnimatedVisibility(visible = texto != null, enter = fadeIn(), exit = fadeOut()) {
        CaixaMensagem(texto = texto.orEmpty(), erro = erro)
    }
}

/** Campo de texto padrão: cantos 12.dp, `singleLine` quando cabe numa linha. */
@Composable
fun CampoTexto(
    valor: String,
    rotulo: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    habilitado: Boolean = true,
    dica: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    linhasMinimas: Int = 1,
    ultimo: Boolean = false,
    alinharADireita: Boolean = false,
    prefixo: String? = null,
    erro: Boolean = false,
    transformacao: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        visualTransformation = transformacao,
        label = { Text(rotulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingText = dica?.let { { Text(it) } },
        prefix = prefixo?.let { { Text(it) } },
        singleLine = linhasMinimas == 1,
        minLines = linhasMinimas,
        enabled = habilitado,
        isError = erro,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = if (alinharADireita) TextAlign.End else TextAlign.Start
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado,
            // Campo de várias linhas usa o Enter para quebrar linha; os demais avançam.
            imeAction = if (ultimo) ImeAction.Default else ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}

/**
 * CPF ou CNPJ com máscara. O valor que entra e sai é só o miolo (sem pontuação,
 * ex.: "12345678000190"); a pontuação é desenhada pelo [MascaraCpfCnpj].
 * Aceita letras por causa do CNPJ alfanumérico.
 */
@Composable
fun CampoCpfCnpj(
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    rotulo: String = "CPF ou CNPJ",
    habilitado: Boolean = true,
    ultimo: Boolean = false
) {
    CampoTexto(
        // limpar aqui também protege contra valor antigo salvo com pontuação.
        valor = limparCpfCnpj(valor),
        rotulo = rotulo,
        aoMudar = { aoMudar(limparCpfCnpj(it)) },
        modifier = modifier,
        habilitado = habilitado,
        ultimo = ultimo,
        transformacao = MascaraCpfCnpj
    )
}

/** Telefone com máscara (fixo ou celular). Mesmo esquema: entra e sai só dígitos. */
@Composable
fun CampoTelefone(
    valor: String,
    aoMudar: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    rotulo: String = "Telefone",
    habilitado: Boolean = true,
    ultimo: Boolean = false
) {
    CampoTexto(
        valor = limparTelefone(valor),
        rotulo = rotulo,
        aoMudar = { aoMudar(limparTelefone(it)) },
        modifier = modifier,
        habilitado = habilitado,
        teclado = KeyboardType.Phone,
        ultimo = ultimo,
        transformacao = MascaraTelefone
    )
}

/**
 * Campo que não digita: mostra o valor escolhido e abre algo ao tocar
 * (um menu, um diálogo...). É um OutlinedTextField somente leitura com uma
 * camada transparente por cima que captura o clique. O [menu] fica dentro do
 * mesmo Box para que o DropdownMenu abra ancorado no campo.
 *
 * [destaque] pinta a borda de `primary` (usado no Cliente ainda vazio).
 */
@Composable
fun CampoSeletor(
    valor: String,
    rotulo: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    habilitado: Boolean = true,
    destaque: Boolean = false,
    menu: @Composable () -> Unit = {}
) {
    val cores = MaterialTheme.colorScheme
    Box(modifier) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = habilitado,
            label = { Text(rotulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = { Icon(Icons.Outlined.ExpandMore, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = if (destaque) {
                OutlinedTextFieldDefaults.colors(unfocusedBorderColor = cores.primary)
            } else {
                OutlinedTextFieldDefaults.colors()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = habilitado, onClick = aoClicar)
        )
        menu()
    }
}

/**
 * Alterna entre claro e escuro; não desenha nada onde o tema segue o sistema.
 * O ícone mostra a ação (sol leva ao claro, lua ao escuro), não o estado atual.
 */
@Composable
fun BotaoTema(modifier: Modifier = Modifier) {
    val alternar = LocalAlternarTema.current ?: return
    val escuro = LocalTemaEscuro.current
    IconButton(onClick = alternar, modifier = modifier) {
        Icon(
            imageVector = if (escuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            contentDescription = if (escuro) "Usar tema claro" else "Usar tema escuro",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Círculo com as iniciais (lista de clientes). [forte] = cor principal cheia (item selecionado). */
@Composable
fun Avatar(iniciais: String, tamanho: Dp = 36.dp, forte: Boolean = false) {
    val cores = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(if (forte) cores.primary else cores.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciais,
            color = if (forte) cores.onPrimary else cores.onPrimaryContainer,
            fontSize = (tamanho.value * 0.36f).sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Carregando a tela: indicador centralizado. */
@Composable
fun Carregando(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/** Erro de carga: mensagem + "Tentar de novo". */
@Composable
fun ErroComRetentativa(mensagem: String, aoTentarDeNovo: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CaixaMensagem(texto = mensagem)
        OutlinedButton(onClick = aoTentarDeNovo, shape = RoundedCornerShape(12.dp)) {
            Text("Tentar de novo")
        }
    }
}

/** Lista vazia: título que convida, uma linha explicando e o botão de ação (nunca só "Nada aqui"). */
@Composable
fun ConviteVazio(
    titulo: String,
    descricao: String,
    textoBotao: String? = null,
    aoClicar: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(
                text = descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (textoBotao != null) {
            Button(
                onClick = aoClicar,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Text(textoBotao, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
