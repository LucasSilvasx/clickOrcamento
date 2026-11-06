package com.clickorcamento.app.feature.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clickorcamento.app.core.design.BotaoTema
import com.clickorcamento.app.core.legal.DocumentosLegais
import org.koin.compose.viewmodel.koinViewModel

/** Só conhece o ViewModel e repassa; o desenho está em [LoginConteudo] e [ConfirmacaoConteudo]. */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.aguardandoConfirmacao) {
        ConfirmacaoConteudo(
            state = state,
            onEntrar = viewModel::entrarAposConfirmar,
            onReenviar = viewModel::reenviarConfirmacao,
            onVoltar = viewModel::voltarParaLogin
        )
    } else {
        LoginConteudo(
            state = state,
            onEmail = viewModel::aoMudarEmail,
            onSenha = viewModel::aoMudarSenha,
            onConfirmacao = viewModel::aoMudarConfirmacao,
            onAlternarVisibilidade = viewModel::alternarVisibilidadeSenha,
            onAceiteTermos = viewModel::aoMudarAceiteTermos,
            onAlternarModo = viewModel::alternarModo,
            onEnviar = viewModel::enviar
        )
    }
}

/** Etapa 2 do cadastro: clicar no link que chegou por e-mail e voltar para entrar. */
@Composable
private fun ConfirmacaoConteudo(
    state: LoginUiState,
    onEntrar: () -> Unit,
    onReenviar: () -> Unit,
    onVoltar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        BotaoTema(Modifier.align(Alignment.TopEnd).padding(12.dp))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 400.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Marca()

            Spacer(Modifier.height(28.dp))

            Text("Confirme seu e-mail", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Abra o e-mail que enviamos para ${state.email} e clique no link de " +
                    "confirmação. Pode aparecer uma página de erro no navegador (localhost): " +
                    "é normal, a conta já fica confirmada. Depois volte aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onEntrar,
                enabled = !state.carregando,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Já confirmei, entrar", style = MaterialTheme.typography.labelLarge)
                }
            }

            Mensagem(
                texto = state.erro,
                fundo = MaterialTheme.colorScheme.errorContainer,
                cor = MaterialTheme.colorScheme.onErrorContainer
            )
            Mensagem(
                texto = state.aviso,
                fundo = MaterialTheme.colorScheme.primaryContainer,
                cor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onReenviar, enabled = !state.carregando) {
                Text("Reenviar e-mail", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onVoltar, enabled = !state.carregando) {
                Text("Voltar para o login", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LoginConteudo(
    state: LoginUiState,
    onEmail: (String) -> Unit,
    onSenha: (String) -> Unit,
    onConfirmacao: (String) -> Unit,
    onAlternarVisibilidade: () -> Unit,
    onAceiteTermos: (Boolean) -> Unit,
    onAlternarModo: () -> Unit,
    onEnviar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        BotaoTema(Modifier.align(Alignment.TopEnd).padding(12.dp))

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 400.dp)
                // Em tela baixa (celular deitado, teclado aberto) o formulário não cabe inteiro.
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Marca()

            Spacer(Modifier.height(28.dp))

            SeletorModo(modo = state.modo, onAlternar = onAlternarModo)

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmail,
                label = { Text("E-mail") },
                singleLine = true,
                enabled = !state.carregando,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.senha,
                onValueChange = onSenha,
                label = { Text("Senha") },
                singleLine = true,
                enabled = !state.carregando,
                visualTransformation =
                    if (state.senhaVisivel) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    // No cadastro ainda falta a confirmação da senha.
                    imeAction = if (state.modo == ModoAuth.ENTRAR) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onDone = { onEnviar() }),
                trailingIcon = {
                    TextButton(onClick = onAlternarVisibilidade) {
                        Text(
                            text = if (state.senhaVisivel) "Ocultar" else "Mostrar",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = state.modo == ModoAuth.CADASTRAR,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.confirmacaoSenha,
                        onValueChange = onConfirmacao,
                        label = { Text("Confirmar senha") },
                        singleLine = true,
                        enabled = !state.carregando,
                        visualTransformation =
                            if (state.senhaVisivel) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { onEnviar() }),
                        // Só marca erro depois que o usuário digitou algo.
                        isError = state.confirmacaoSenha.isNotEmpty() &&
                                state.confirmacaoSenha != state.senha,
                        supportingText = {
                            if (state.confirmacaoSenha.isNotEmpty() &&
                                state.confirmacaoSenha != state.senha) {
                                Text("As senhas não coincidem")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    AceiteTermos(
                        aceito = state.aceitouTermos,
                        habilitado = !state.carregando,
                        aoMudar = onAceiteTermos
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onEnviar,
                enabled = state.podeEnviar,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (state.carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(state.textoBotao, style = MaterialTheme.typography.labelLarge)
                }
            }

            Mensagem(
                texto = state.erro,
                fundo = MaterialTheme.colorScheme.errorContainer,
                cor = MaterialTheme.colorScheme.onErrorContainer
            )
            Mensagem(
                texto = state.aviso,
                fundo = MaterialTheme.colorScheme.primaryContainer,
                cor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onAlternarModo, enabled = !state.carregando) {
                Text(
                    text = if (state.modo == ModoAuth.ENTRAR)
                        "Não tem conta? Criar agora"
                    else
                        "Já tenho conta. Entrar",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Aceite exigido no cadastro. Os links abrem os textos no navegador, nos três alvos. */
@Composable
private fun AceiteTermos(aceito: Boolean, habilitado: Boolean, aoMudar: (Boolean) -> Unit) {
    val estiloLink = TextLinkStyles(
        SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = aceito, onCheckedChange = aoMudar, enabled = habilitado)
        Text(
            text = buildAnnotatedString {
                append("Li e aceito os ")
                withLink(LinkAnnotation.Url(DocumentosLegais.URL_TERMOS, estiloLink)) {
                    append("Termos de Uso")
                }
                append(" e a ")
                withLink(LinkAnnotation.Url(DocumentosLegais.URL_PRIVACIDADE, estiloLink)) {
                    append("Política de Privacidade")
                }
                append(".")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Marca do app. TODO: trocar o Box pelo logo (Image) quando houver um. */
@Composable
private fun Marca() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "C",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(14.dp))
        Text("clickOrçamento", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Orçamentos de serviço, sem planilha",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Alternador em formato de pílula, com a opção ativa destacada. */
@Composable
private fun SeletorModo(modo: ModoAuth, onAlternar: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        AbaModo("Entrar", modo == ModoAuth.ENTRAR) {
            if (modo != ModoAuth.ENTRAR) onAlternar()
        }
        AbaModo("Criar conta", modo == ModoAuth.CADASTRAR) {
            if (modo != ModoAuth.CADASTRAR) onAlternar()
        }
    }
}

@Composable
private fun AbaModo(texto: String, ativo: Boolean, onClick: () -> Unit) {
    val fundo by animateColorAsState(
        if (ativo) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    )
    val corTexto by animateColorAsState(
        if (ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(fundo)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 9.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = corTexto
        )
    }
}

@Composable
private fun Mensagem(
    texto: String?,
    fundo: androidx.compose.ui.graphics.Color,
    cor: androidx.compose.ui.graphics.Color
) {
    AnimatedVisibility(visible = texto != null, enter = fadeIn(), exit = fadeOut()) {
        Column {
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(fundo)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Text(
                    text = texto.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = cor
                )
            }
        }
    }
}