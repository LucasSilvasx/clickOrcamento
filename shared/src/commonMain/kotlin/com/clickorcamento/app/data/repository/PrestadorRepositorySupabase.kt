package com.clickorcamento.app.data.repository

import com.clickorcamento.app.data.remote.dto.PrestadorDto
import com.clickorcamento.app.domain.model.Prestador
import com.clickorcamento.app.domain.repository.PrestadorRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class PrestadorRepositorySupabase(
    private val cliente: SupabaseClient
) : PrestadorRepository {

    override suspend fun listar(): List<Prestador> =
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
