package com.clickorcamento.app.feature.orcamento.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clickorcamento.app.core.design.Avatar
import com.clickorcamento.app.core.design.CaixaMensagemAnimada
import com.clickorcamento.app.core.design.CampoTexto
import com.clickorcamento.app.core.format.iniciaisDe
import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.feature.cliente.FormularioCliente

/**
 * Escolher o cliente do orçamento: diálogo com busca e a opção "Novo cliente".
 * Diálogo (e não menu suspenso) porque a lista de clientes cresce, e assim a
 * busca funciona igual no desktop e no celular.
 */
@Composable
fun SeletorClienteDialogo(
    clientes: List<Cliente>,
    selecionadoId: String?,
    aoSelecionar: (String) -> Unit,
    aoNovoCliente: () -> Unit,
    aoFechar: () -> Unit
) {
    var busca by remember { mutableStateOf("") }
    val termo = busca.trim()
    val digitos = termo.filter { it.isDigit() }
    val filtrados = if (termo.isEmpty()) {
        clientes
    } else {
        clientes.filter {
            it.razaoSocial.contains(termo, ignoreCase = true) ||
                (digitos.isNotEmpty() && it.cnpj.filter { c -> c.isDigit() }.contains(digitos))
        }
    }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Selecionar cliente") },
        text = {
            Column {
                OutlinedTextField(
                    value = busca,
                    onValueChange = { busca = it },
                    placeholder = { Text("Buscar por nome ou CNPJ") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(top = 8.dp))

                if (filtrados.isEmpty()) {
                    Text(
                        text = if (clientes.isEmpty()) {
                            "Você ainda não tem clientes. Cadastre o primeiro."
                        } else {
                            "Nenhum cliente encontrado."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())
                    ) {
                        filtrados.forEach { cliente ->
                            LinhaClienteSeletor(
                                cliente = cliente,
                                selecionado = cliente.id == selecionadoId,
                                aoClicar = { aoSelecionar(cliente.id) }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = aoNovoCliente) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Novo cliente")
            }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Fechar") } }
    )
}

@Composable
private fun LinhaClienteSeletor(cliente: Cliente, selecionado: Boolean, aoClicar: () -> Unit) {
    val cores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = aoClicar)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Avatar(iniciais = iniciaisDe(cliente.razaoSocial), forte = selecionado)
        Column(Modifier.weight(1f)) {
            Text(
                text = cliente.razaoSocial,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (cliente.cnpj.isNotBlank()) {
                Text(
                    text = cliente.cnpj,
                    style = MaterialTheme.typography.labelSmall,
                    color = cores.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Cadastro rápido de cliente dentro do orçamento: só o essencial (razão
 * social, CNPJ e telefone). O resto o usuário completa depois na aba Clientes.
 */
@Composable
fun NovoClienteDialogo(
    form: FormularioCliente,
    aoMudar: (FormularioCliente) -> Unit,
    aoSalvar: () -> Unit,
    aoFechar: () -> Unit
) {
    val editavel = !form.salvando

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Novo cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoTexto(
                    valor = form.razaoSocial,
                    rotulo = "Razão social *",
                    habilitado = editavel,
                    aoMudar = { aoMudar(form.copy(razaoSocial = it)) }
                )
                CampoTexto(
                    valor = form.cnpj,
                    rotulo = "CNPJ ou CPF",
                    habilitado = editavel,
                    aoMudar = { aoMudar(form.copy(cnpj = it)) }
                )
                CampoTexto(
                    valor = form.telefone,
                    rotulo = "Telefone",
                    habilitado = editavel,
                    teclado = KeyboardType.Phone,
                    ultimo = true,
                    aoMudar = { aoMudar(form.copy(telefone = it)) }
                )
                CaixaMensagemAnimada(form.erro)
            }
        },
        confirmButton = {
            Button(
                onClick = aoSalvar,
                enabled = form.podeSalvar,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (form.salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar, enabled = editavel) { Text("Cancelar") }
        }
    )
}
