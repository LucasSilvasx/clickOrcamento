package com.clickorcamento.app.feature.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clickorcamento.app.core.design.LocalAlternarTema
import com.clickorcamento.app.core.design.LocalTemaEscuro

/**
 * Padrão de duas camadas:
 *   Screen  → conhece o ViewModel, só repassa estado e ações
 *   Conteudo → stateless puro, recebe tudo por parâmetro
 *
 * Assim o Conteudo é testável e previewável sem ViewModel, e fica
 * explícito quais ações a tela realmente dispara.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel { LoginViewModel() }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LoginConteudo(
        state = state,
        onEmail = viewModel::aoMudarEmail,
        onSenha = viewModel::aoMudarSenha,
        onConfirmacao = viewModel::aoMudarConfirmacao,
        onAlternarVisibilidade = viewModel::alternarVisibilidadeSenha,
        onAlternarModo = viewModel::alternarModo,
        onEnviar = viewModel::enviar
    )
}

@Composable
private fun LoginConteudo(
    state: LoginUiState,
    onEmail: (String) -> Unit,
    onSenha: (String) -> Unit,
    onConfirmacao: (String) -> Unit,
    onAlternarVisibilidade: () -> Unit,
    onAlternarModo: () -> Unit,
    onEnviar: () -> Unit
) {
    val temaEscuro = LocalTemaEscuro.current
    val alternarTema = LocalAlternarTema.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Botão de tema no canto. Sem ícone por enquanto — ícones
        // como DarkMode vivem em material-icons-extended, que é uma
        // dependência extra. Texto resolve e não pesa.
        TextButton(
            onClick = alternarTema,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
        ) {
            Text(if (temaEscuro) "Tema claro" else "Tema escuro")
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                // Largura máxima: sem isso, no desktop o formulário
                // esticaria pela tela inteira e ficaria horrível.
                .widthIn(max = 400.dp)
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
                // Teclado de e-mail no celular, e "próximo" em vez de "enter"
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
                    imeAction = ImeAction.Done
                ),
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

            // AnimatedVisibility: o campo de confirmação desliza pra
            // dentro quando o usuário troca pra cadastro, em vez de
            // pular na tela. Detalhe pequeno, diferença grande.
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
                        // Só marca erro depois que o usuário digitou algo
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

/** Marca do app. Quando você tiver o logo, troca o Box por uma Image. */
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
    // animateColorAsState faz a transição ser suave em vez de piscar
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