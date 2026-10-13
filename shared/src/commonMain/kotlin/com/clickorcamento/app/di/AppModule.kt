package com.clickorcamento.app.di

import com.clickorcamento.app.data.remote.criarSupabaseClient
import com.clickorcamento.app.data.repository.PrestadorRepositorySupabase
import com.clickorcamento.app.domain.repository.PrestadorRepository
import com.clickorcamento.app.feature.auth.LoginViewModel
import com.clickorcamento.app.feature.configuracoes.ConfiguracoesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
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

    // `bind` registra a implementação sob o nome da interface: quem pede
    // PrestadorRepository recebe a versão Supabase (e um teste poderia
    // trocar por uma falsa sem mexer no ViewModel).
    singleOf(::PrestadorRepositorySupabase) bind PrestadorRepository::class

    viewModelOf(::LoginViewModel)
    viewModelOf(::ConfiguracoesViewModel)
}
