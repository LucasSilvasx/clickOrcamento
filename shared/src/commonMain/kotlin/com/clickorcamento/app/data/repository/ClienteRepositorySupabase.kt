package com.clickorcamento.app.data.repository

import com.clickorcamento.app.data.remote.dto.ClienteDto
import com.clickorcamento.app.data.remote.dto.paraJson
import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosCliente
import com.clickorcamento.app.domain.repository.ClienteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ClienteRepositorySupabase(
    private val supabase: SupabaseClient
) : ClienteRepository {

    // Sem filtro por usuário: o RLS já devolve só as linhas de quem está logado.
    override suspend fun listar(): List<Cliente> =
        supabase.from("cliente")
            .select { order("razao_social", Order.ASCENDING) }
            .decodeList<ClienteDto>()
            .map { it.paraDominio() }

    // `select()` no fim pede ao banco que devolva a linha criada (com o id novo).
    override suspend fun criar(dados: DadosCliente): Cliente =
        supabase.from("cliente")
            .insert(dados.paraJson()) { select() }
            .decodeSingle<ClienteDto>()
            .paraDominio()

    override suspend fun atualizar(id: String, dados: DadosCliente) {
        supabase.from("cliente").update(dados.paraJson()) {
            filter { eq("id", id) }
        }
    }

    override suspend fun excluir(id: String) {
        supabase.from("cliente").delete {
            filter { eq("id", id) }
        }
    }
}
