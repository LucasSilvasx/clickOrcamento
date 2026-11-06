package com.clickorcamento.app.data.repository

import com.clickorcamento.app.data.remote.dto.PrestadorDto
import com.clickorcamento.app.data.remote.dto.paraJson
import com.clickorcamento.app.domain.model.DadosPrestador
import com.clickorcamento.app.domain.model.Prestador
import com.clickorcamento.app.domain.repository.PrestadorRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class PrestadorRepositorySupabase(
    private val cliente: SupabaseClient,
    private val cache: CacheLeituras
) : PrestadorRepository {

    override suspend fun listar(): List<Prestador> = cache.ler("empresas") {
        cliente.from("prestador")
            .select {
                // Sem filtro por usuário: o RLS do banco já devolve só as
                // linhas do usuário logado. Padrão primeiro (true > false).
                order("padrao", Order.DESCENDING)
                order("apelido", Order.ASCENDING)
            }
            .decodeList<PrestadorDto>()
            .map { it.paraDominio() }
    }

    // usuario_id não vai no corpo: o banco preenche com auth.uid() (default da coluna).
    override suspend fun criar(dados: DadosPrestador, padrao: Boolean) = cache.gravando {
        cliente.from("prestador").insert(dados.paraJson(padrao))
        Unit
    }

    override suspend fun atualizar(id: String, dados: DadosPrestador) = cache.gravando {
        cliente.from("prestador").update(dados.paraJson()) {
            filter { eq("id", id) }
        }
        Unit
    }

    /**
     * Dois passos, nesta ordem: o índice único parcial do banco só aceita uma
     * linha com padrao = true, então primeiro tiramos a marca da atual e só
     * depois marcamos a nova. Se o segundo passo falhar, o usuário fica sem
     * padrão até tentar de novo (nada se perde); o ViewModel recarrega a lista.
     */
    override suspend fun definirPadrao(id: String) = cache.gravando {
        cliente.from("prestador").update(buildJsonObject { put("padrao", false) }) {
            filter { eq("padrao", true) }
        }
        cliente.from("prestador").update(buildJsonObject { put("padrao", true) }) {
            filter { eq("id", id) }
        }
        Unit
    }

    override suspend fun excluir(id: String) = cache.gravando {
        cliente.from("prestador").delete {
            filter { eq("id", id) }
        }
        Unit
    }
}
