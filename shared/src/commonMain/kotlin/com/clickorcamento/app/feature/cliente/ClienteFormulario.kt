package com.clickorcamento.app.feature.cliente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.clickorcamento.app.core.design.Avatar
import com.clickorcamento.app.core.design.Badge
import com.clickorcamento.app.core.design.CaixaMensagemAnimada
import com.clickorcamento.app.core.design.CampoCpfCnpj
import com.clickorcamento.app.core.design.CampoTelefone
import com.clickorcamento.app.core.design.CampoTexto
import com.clickorcamento.app.core.format.formatarBr
import com.clickorcamento.app.core.format.formatarMoeda
import com.clickorcamento.app.core.format.iniciaisDe
import com.clickorcamento.app.domain.model.OrcamentoResumo
import com.clickorcamento.app.feature.orcamento.BadgeStatus
import com.clickorcamento.app.feature.orcamento.textoNumero

/**
 * Formulário de cliente (criar e editar). Stateless: recebe o [FormularioCliente]
 * e devolve o formulário inteiro já alterado em `aoMudar`.
 *
 * [largo] = painel de detalhe do desktop (campos em pares, cabeçalho com avatar);
 * senão é a tela cheia do celular (campos empilhados, seta de voltar).
 */
@Composable
fun ClienteFormulario(
    form: FormularioCliente,
    orcamentosDoCliente: List<OrcamentoResumo>,
    largo: Boolean,
    aoMudar: (FormularioCliente) -> Unit,
    aoSalvar: () -> Unit,
    aoCancelar: () -> Unit,
    aoExcluir: () -> Unit,
    aoAbrirOrcamento: (String) -> Unit
) {
    val editavel = !form.salvando

    Column(Modifier.fillMaxWidth()) {
        Cabecalho(form, orcamentosDoCliente, largo, editavel, aoCancelar)
        Spacer(Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoTexto(
                valor = form.razaoSocial,
                rotulo = "Razão social *",
                habilitado = editavel,
                aoMudar = { aoMudar(form.copy(razaoSocial = it)) }
            )
            Par(largo) {
                CampoCpfCnpj(
                    valor = form.cnpj,
                    rotulo = "CNPJ ou CPF",
                    habilitado = editavel,
                    modifier = it,
                    aoMudar = { v -> aoMudar(form.copy(cnpj = v)) }
                )
                CampoTexto(
                    valor = form.inscricaoEstadual,
                    rotulo = "Inscrição estadual",
                    habilitado = editavel,
                    modifier = it,
                    aoMudar = { v -> aoMudar(form.copy(inscricaoEstadual = v)) }
                )
            }
            CampoTexto(
                valor = form.endereco,
                rotulo = "Endereço",
                habilitado = editavel,
                linhasMinimas = 2,
                aoMudar = { aoMudar(form.copy(endereco = it)) }
            )
            Par(largo) {
                CampoTelefone(
                    valor = form.telefone,
                    habilitado = editavel,
                    modifier = it,
                    aoMudar = { v -> aoMudar(form.copy(telefone = v)) }
                )
                CampoTexto(
                    valor = form.email,
                    rotulo = "E-mail",
                    habilitado = editavel,
                    teclado = KeyboardType.Email,
                    ultimo = true,
                    modifier = it,
                    aoMudar = { v -> aoMudar(form.copy(email = v)) }
                )
            }
        }

        if (form.editando && orcamentosDoCliente.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            UltimosOrcamentos(orcamentosDoCliente, aoAbrirOrcamento)
        }

        // Erro (inclusive "Este cliente tem orçamentos e não pode ser excluído").
        Spacer(Modifier.height(if (form.erro != null) 16.dp else 0.dp))
        CaixaMensagemAnimada(form.erro)

        Spacer(Modifier.height(24.dp))

        // Um botão de destaque só (Salvar); Cancelar é secundário; Excluir é discreto.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (form.editando) {
                TextButton(
                    onClick = aoExcluir,
                    enabled = editavel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Excluir")
                }
            }
            Spacer(Modifier.weight(1f))
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

@Composable
private fun Cabecalho(
    form: FormularioCliente,
    orcamentosDoCliente: List<OrcamentoResumo>,
    largo: Boolean,
    editavel: Boolean,
    aoVoltar: () -> Unit
) {
    if (largo) {
        // Desktop: avatar + nome + resumo (mockup "Clientes").
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(iniciais = iniciaisDe(form.razaoSocial), tamanho = 44.dp)
            Column {
                Text(
                    text = if (form.editando) form.razaoSocial.ifBlank { "Cliente" } else "Novo cliente",
                    style = MaterialTheme.typography.titleMedium
                )
                if (form.editando) {
                    Text(
                        text = resumoDeOrcamentos(orcamentosDoCliente),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        // Celular: seta de voltar + título.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = aoVoltar, enabled = editavel) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
            }
            Text(form.titulo, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

/** "8 orçamentos · último em 09/09/2026". */
private fun resumoDeOrcamentos(orcamentos: List<OrcamentoResumo>): String {
    if (orcamentos.isEmpty()) return "Nenhum orçamento ainda"
    val ultimo = orcamentos.maxOf { it.dataEmissao }
    val quantidade = if (orcamentos.size == 1) "1 orçamento" else "${orcamentos.size} orçamentos"
    return "$quantidade · último em ${ultimo.formatarBr()}"
}

@Composable
private fun UltimosOrcamentos(orcamentos: List<OrcamentoResumo>, aoAbrir: (String) -> Unit) {
    Text("Últimos orçamentos", style = MaterialTheme.typography.titleSmall)
    Spacer(Modifier.height(4.dp))
    // A lista já vem do mais novo para o mais antigo (ordem do repository).
    orcamentos.take(5).forEach { orcamento ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { aoAbrir(orcamento.id) }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Badge(textoNumero(orcamento.numero))
            Text(
                text = orcamento.dataEmissao.formatarBr(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            BadgeStatus(orcamento.status)
            Text(
                text = formatarMoeda(orcamento.totalGeralCentavos),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(110.dp)
            )
        }
    }
}

/** Dois campos lado a lado no desktop; empilhados no celular. */
@Composable
private fun Par(lado: Boolean, campos: @Composable (Modifier) -> Unit) {
    if (lado) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            campos(Modifier.weight(1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            campos(Modifier.fillMaxWidth())
        }
    }
}
