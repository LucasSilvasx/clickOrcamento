package com.clickorcamento.app.domain.repository

import com.clickorcamento.app.domain.model.Cliente
import com.clickorcamento.app.domain.model.DadosCliente

/** Contrato de acesso aos clientes. Funções `suspend` podem lançar exceção; o ViewModel trata. */
interface ClienteRepository {
    /** Todos os clientes do usuário, em ordem alfabética. */
    suspend fun listar(): List<Cliente>

    /** Cria e devolve o cliente já com o id (o editor de orçamento seleciona o recém-criado). */
    suspend fun criar(dados: DadosCliente): Cliente

    suspend fun atualizar(id: String, dados: DadosCliente)

    /** Falha se o cliente tiver orçamentos (a FK é RESTRICT no banco). */
    suspend fun excluir(id: String)
}
