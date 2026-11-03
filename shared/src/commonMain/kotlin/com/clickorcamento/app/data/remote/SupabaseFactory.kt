package com.clickorcamento.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * O supabase-kt é modular; instalamos só o que o app usa:
 *
 *   Auth:      login e sessão. É ele quem anexa o token JWT em toda
 *              requisição; sem o token o RLS não devolve linha nenhuma.
 *
 *   Postgrest: o CRUD nas tabelas.
 *
 * Nada de Storage ou Realtime por enquanto: cada módulo instalado
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