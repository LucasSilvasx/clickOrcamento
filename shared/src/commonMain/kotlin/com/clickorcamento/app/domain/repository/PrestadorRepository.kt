package com.clickorcamento.app.domain.repository

import com.clickorcamento.app.domain.model.DadosPrestador
import com.clickorcamento.app.domain.model.Prestador

/**
 * Contrato de acesso às empresas. Funções `suspend` podem lançar exceção; o ViewModel trata.
 */
interface PrestadorRepository {
    /** Todas as empresas do usuário logado: a padrão primeiro, depois por apelido. */
    suspend fun listar(): List<Prestador>

    /** Cria uma empresa. `padrao = true` só para a primeira (o banco aceita uma padrão por usuário). */
    suspend fun criar(dados: DadosPrestador, padrao: Boolean)

    /** Atualiza os dados de uma empresa. Não mexe em `padrao`. */
    suspend fun atualizar(id: String, dados: DadosPrestador)

    /** Torna esta empresa a padrão e tira a marca da anterior. */
    suspend fun definirPadrao(id: String)

    /** Falha se a empresa tiver orçamentos (a FK é RESTRICT no banco). */
    suspend fun excluir(id: String)
}
