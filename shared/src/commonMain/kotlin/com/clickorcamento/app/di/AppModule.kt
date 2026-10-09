package com.clickorcamento.app.di

import com.clickorcamento.app.data.remote.criarSupabaseClient
import com.clickorcamento.app.feature.auth.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * A "receita" do Koin: diz COMO criar cada objeto do app.
 *
 * Analogia: é a lista de ingredientes de uma cozinha. Quem precisa de um
 * prato (ViewModel) não vai ao mercado; só pede, e o Koin entrega pronto,
 * já com os ingredientes (cliente do Supabase) dentro.
 *
 *   single      → uma instância só para o app inteiro (o cliente do Supabase)
 *   viewModelOf → uma instância por tela, descartada junto com ela
 *
 * viewModelOf(::LoginViewModel) olha o construtor e entrega sozinho os
 * parâmetros que ele pede (o SupabaseClient, no caso).
 *
 * Próximas telas (empresa, cliente...) entram aqui: um repository como
 * `single` e o ViewModel com `viewModelOf`.
 */
val appModule = module {
    single { criarSupabaseClient() }

    viewModelOf(::LoginViewModel)
}
