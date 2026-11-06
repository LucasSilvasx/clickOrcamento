package com.clickorcamento.app.data.remote

/**
 * A chave publicável não é segredo: vai dentro do app instalado e do bundle do navegador.
 * Quem protege os dados é o Row Level Security. A chave secreta ignora o RLS e não pode
 * aparecer no cliente.
 */
object SupabaseConfig {
    const val URL = "https://ksbkcqohqjqwmelvepli.supabase.co"
    const val PUBLISHABLE_KEY = "sb_publishable_EGJ0pvdKOY-AKRsvjIBaCQ_ehtuhniq"
}