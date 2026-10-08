package com.clickorcamento.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * O supabase-kt é modular: você instala só os pedaços que usa.
 * Instalamos:
 *
 *   Auth      — login, sessão, e (o mais importante) é ele quem
 *               anexa o token JWT em toda requisição. Sem esse
 *               token o Postgres não sabe quem é você e o RLS
 *               não devolve linha nenhuma.
 *
 *   Postgrest — o CRUD nas tabelas.
 *
 * Nada de Storage ou Realtime por enquanto — cada módulo instalado
 * é peso a mais no bundle do navegador.
 */
fun criarSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseConfig.URL,
    supabaseKey = SupabaseConfig.PUBLISHABLE_KEY
) {
    install(Auth)
    install(Postgrest)
}

/**
 * Instância única. "by lazy" cria só na primeira vez que alguém usa.
 *
 * Isso é temporário: quando entrar o Koin, essa variável global sai e
 * o cliente passa a ser injetado. Estou deixando assim agora pra não
 * misturar dois assuntos novos no mesmo passo.
 */
val supabase: SupabaseClient by lazy { criarSupabaseClient() }