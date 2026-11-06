package com.clickorcamento.app.di

import com.clickorcamento.app.data.remote.criarSupabaseClient
import com.clickorcamento.app.data.repository.CacheLeituras
import com.clickorcamento.app.data.repository.ClienteRepositorySupabase
import com.clickorcamento.app.data.repository.OrcamentoRepositorySupabase
import com.clickorcamento.app.data.repository.PrestadorRepositorySupabase
import com.clickorcamento.app.domain.repository.ClienteRepository
import com.clickorcamento.app.domain.repository.OrcamentoRepository
import com.clickorcamento.app.domain.repository.PrestadorRepository
import com.clickorcamento.app.feature.auth.LoginViewModel
import com.clickorcamento.app.feature.cliente.ClientesViewModel
import com.clickorcamento.app.feature.configuracoes.ConfiguracoesViewModel
import com.clickorcamento.app.feature.orcamento.editor.EditorOrcamentoViewModel
import com.clickorcamento.app.feature.orcamento.lista.OrcamentosViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { criarSupabaseClient() }
    single { CacheLeituras() }

    singleOf(::PrestadorRepositorySupabase) bind PrestadorRepository::class
    singleOf(::ClienteRepositorySupabase) bind ClienteRepository::class
    singleOf(::OrcamentoRepositorySupabase) bind OrcamentoRepository::class

    viewModelOf(::LoginViewModel)
    viewModelOf(::ConfiguracoesViewModel)
    viewModelOf(::ClientesViewModel)
    viewModelOf(::OrcamentosViewModel)

    // O editor recebe o id do orçamento (ou nada, se for novo) pela tela, por isso não usa viewModelOf.
    viewModel { params -> EditorOrcamentoViewModel(params.getOrNull<String>(), get(), get(), get()) }
}
