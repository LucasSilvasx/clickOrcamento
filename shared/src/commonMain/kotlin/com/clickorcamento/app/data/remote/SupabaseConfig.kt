package com.clickorcamento.app.data.remote

/**
 * Chave publicável do projeto. Pode ir pro Git sem problema.
 *
 * Ela não é segredo: vai dentro do app instalado e dentro do bundle
 * do navegador, onde qualquer um consegue ler. O que impede alguém
 * de ver seus dados com ela é o Row Level Security no banco.
 *
 * A chave que NUNCA pode aparecer aqui é a secreta (sb_secret_...),
 * que ignora RLS. Ela nunca sai do servidor.
 */
object SupabaseConfig {
    const val URL = "https://ksbkcqohqjqwmelvepli.supabase.co"
    const val PUBLISHABLE_KEY = "sb_publishable_EGJ0pvdKOY-AKRsvjIBaCQ_ehtuhniq"
}