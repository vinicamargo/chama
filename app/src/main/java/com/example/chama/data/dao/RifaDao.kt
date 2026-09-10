package com.example.chama.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chama.data.entity.Rifa
import kotlinx.coroutines.flow.Flow

data class BlocoDonoInfo(
    val vendedorId: Long,
    val nomeVendedor: String
)

@Dao
interface RifaDao {

    @Query("SELECT * FROM rifas")
    fun getRifas(): Flow<List<Rifa>>

    @Query("SELECT * FROM rifas WHERE numero = :numeroRifa")
    fun getRifaPorNumero(numeroRifa: Int): Rifa

    @Query("SELECT COUNT(*) FROM rifas")
    fun contarRifas(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun inserirRifas(rifas: List<Rifa>)

    @Query("UPDATE rifas SET vendedorId = :vendedorId WHERE bloco = :numBloco")
    fun vincularVendedorAoBloco(vendedorId: Long, numBloco: Int)

    @Query("UPDATE rifas SET vendedorId = NULL WHERE bloco = :numBloco")
    fun desvincularVendedorDoBloco(numBloco: Int)

    @Query("UPDATE rifas SET estaPaga = :estaPaga WHERE bloco = :bloco")
    fun atualizarPagamentoBloco (bloco: Int, estaPaga: Boolean)

    @Query("UPDATE rifas SET vendedorId = NULL WHERE vendedorId = :vendedorId")
    fun desvincularRifasDoVendedor(vendedorId: Long)

    @Query("SELECT COALESCE(MAX(numero), 0) FROM rifas")
    fun getMaiorNumeroRifa(): Int

    // Retorna o maior número de bloco cadastrado
    @Query("SELECT COALESCE(MAX(bloco), 0) FROM rifas")
    fun getMaiorNumeroBloco(): Int

    // Busca se o bloco já possui dono e o nome correspondente (via crismando)
    @Query("""
        SELECT r.vendedorId AS vendedorId, c.nome AS nomeVendedor
        FROM rifas r
        INNER JOIN crismandos c ON r.vendedorId = c.crismandoId
        WHERE r.bloco = :bloco AND r.vendedorId IS NOT NULL
        LIMIT 1
    """)
    fun buscarDonoDoBloco(bloco: Int): BlocoDonoInfo?

    @Query("""
        DELETE FROM rifas 
        WHERE bloco IN (
            SELECT DISTINCT bloco FROM rifas 
            ORDER BY bloco DESC 
            LIMIT :quantidadeBlocos
        )
    """)
    fun excluirUltimosBlocos(quantidadeBlocos: Int)

    @Query("""
        SELECT COUNT(*) FROM rifas 
        WHERE (estaPaga = 1 OR vendedorId IS NOT NULL) 
        AND bloco IN (
            SELECT DISTINCT bloco FROM rifas 
            ORDER BY bloco DESC 
            LIMIT :quantidadeBlocos
        )
    """)
    fun contarRifasEmUsoNosUltimosBlocos(quantidadeBlocos: Int): Int
}