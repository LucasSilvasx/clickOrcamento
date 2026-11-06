package com.clickorcamento.app.data.repository

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Guarda em memória o resultado das listas, para que trocar de aba não repita as consultas.
 *
 * As listas dependem umas das outras (o resumo do orçamento traz o nome do cliente e da
 * empresa), por isso qualquer gravação limpa tudo em vez de tentar acertar o que mudou.
 * A validade cobre o que foi alterado em outro aparelho.
 *
 * Os dados ficam aqui depois do filtro do RLS: ao trocar de conta, quem limpa é o App.kt.
 */
class CacheLeituras(
    private val validade: Duration = 2.minutes,
    private val relogio: TimeSource = TimeSource.Monotonic
) {
    private class Entrada(val valor: Any?, val guardadoEm: TimeMark)

    private val trava = Mutex()
    private val entradas = mutableMapOf<String, Entrada>()

    /** Muda a cada limpeza: identifica consultas que começaram antes dela. */
    private var geracao = 0L

    suspend fun <T> ler(chave: String, buscar: suspend () -> T): T {
        val (existente, geracaoInicial) = trava.withLock { entradas[chave] to geracao }
        if (existente != null && existente.guardadoEm.elapsedNow() < validade) {
            @Suppress("UNCHECKED_CAST")
            return existente.valor as T
        }

        val valor = buscar()

        trava.withLock {
            // Se houve limpeza durante a busca, o resultado pode ser anterior à gravação:
            // serve para quem pediu, mas não pode responder às próximas leituras.
            if (geracao == geracaoInicial) {
                entradas[chave] = Entrada(valor, relogio.markNow())
            }
        }
        return valor
    }

    /** Limpa também quando a gravação falha, porque ela pode ter sido feita em parte. */
    suspend fun <T> gravando(gravar: suspend () -> T): T =
        try {
            gravar()
        } finally {
            // NonCancellable: sair da tela no meio da gravação não pode deixar a lista antiga.
            withContext(NonCancellable) { limpar() }
        }

    suspend fun limpar() {
        trava.withLock {
            entradas.clear()
            geracao++
        }
    }
}
