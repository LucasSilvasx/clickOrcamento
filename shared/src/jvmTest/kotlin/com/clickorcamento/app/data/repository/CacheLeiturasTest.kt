package com.clickorcamento.app.data.repository

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class CacheLeiturasTest {

    private val relogio = TestTimeSource()
    private val cache = CacheLeituras(validade = 2.minutes, relogio = relogio)
    private var consultas = 0

    private suspend fun lerLista(): List<String> = cache.ler("lista") {
        consultas++
        listOf("consulta $consultas")
    }

    @Test
    fun `segunda leitura dentro da validade nao consulta de novo`() = runBlocking {
        val primeira = lerLista()
        relogio += 119.seconds
        val segunda = lerLista()

        assertEquals(1, consultas)
        assertEquals(primeira, segunda)
    }

    @Test
    fun `leitura depois da validade consulta de novo`() = runBlocking {
        lerLista()
        relogio += 2.minutes
        lerLista()

        assertEquals(2, consultas)
    }

    @Test
    fun `gravacao limpa todas as listas`() = runBlocking {
        lerLista()
        cache.gravando { "gravado" }
        lerLista()

        assertEquals(2, consultas)
    }

    @Test
    fun `gravacao que falha tambem limpa porque pode ter gravado em parte`() = runBlocking {
        lerLista()
        assertFailsWith<IllegalStateException> {
            cache.gravando { error("falhou no meio") }
        }
        lerLista()

        assertEquals(2, consultas)
    }

    @Test
    fun `erro na consulta nao fica guardado`() = runBlocking {
        assertFailsWith<IllegalStateException> {
            cache.ler<List<String>>("lista") { error("sem conexão") }
        }
        lerLista()

        assertEquals(1, consultas)
    }

    @Test
    fun `limpar apaga os dados e impede que outra conta os veja`() = runBlocking {
        lerLista()
        cache.limpar()
        lerLista()

        assertEquals(2, consultas)
    }

    @Test
    fun `resultado de consulta iniciada antes de limpar nao fica guardado`() = runBlocking {
        val consultaIniciou = CompletableDeferred<Unit>()
        val liberarConsulta = CompletableDeferred<Unit>()

        val leitura = async(Dispatchers.Default) {
            cache.ler("lista") {
                consultaIniciou.complete(Unit)
                liberarConsulta.await()
                listOf("dado antigo")
            }
        }
        consultaIniciou.await()
        cache.limpar()
        liberarConsulta.complete(Unit)

        // Quem pediu recebe o resultado, mas a próxima leitura precisa ir ao banco.
        assertEquals(listOf("dado antigo"), leitura.await())
        lerLista()
        assertEquals(1, consultas)
    }
}
