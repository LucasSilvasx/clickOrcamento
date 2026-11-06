package com.clickorcamento.app.core.erro

import kotlinx.coroutines.CancellationException

/**
 * `runCatching` para chamadas `suspend`: o cancelamento precisa continuar subindo, senão sair
 * da tela no meio de uma chamada seria tratado como falha.
 *
 * O texto da exceção vai só para o log. Ele traz URL e cabeçalhos da requisição, que não
 * servem para quem usa o app: a tela mostra uma mensagem própria.
 */
inline fun <T> tentar(bloco: () -> T): Result<T> =
    runCatching(bloco).onFailure { erro ->
        if (erro is CancellationException) throw erro
        println("Falha na chamada: ${erro.message}")
    }
