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
 *
 * Quem decide quando criar (e garante que seja uma instância só) é o
 * Koin, em di/AppModule.kt.
 */
fun criarSupabaseClient(): SupabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseConfig.URL,
    supabaseKey = SupabaseConfig.PUBLISHABLE_KEY
) {
    install(Auth)
    install(Postgrest)
}