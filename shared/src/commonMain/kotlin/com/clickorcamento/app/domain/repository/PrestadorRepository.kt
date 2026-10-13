package com.clickorcamento.app.domain.repository

import com.clickorcamento.app.domain.model.Prestador

/**
 * Contrato de acesso às empresas. O ViewModel só conhece esta interface;
 * quem fala com o Supabase é a implementação em data/repository/.
 *
 * Funções `suspend` podem lançar exceção (rede fora, projeto pausado...).
 * Quem chama (o ViewModel) trata com `runCatching` e transforma em estado de tela.
 */
interface PrestadorRepository {
    /** Todas as empresas do usuário logado: a padrão primeiro, depois por apelido. */
    suspend fun listar(): List<Prestador>
}
