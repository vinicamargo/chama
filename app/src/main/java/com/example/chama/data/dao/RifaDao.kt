package com.example.chama.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chama.data.entity.Rifa
import kotlinx.coroutines.flow.Flow

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

    // Exclui os N últimos blocos (e todas as suas rifas)
    @Query("""
    DELETE FROM rifas 
    WHERE bloco IN (
        SELECT DISTINCT bloco FROM rifas 
        ORDER BY bloco DESC 
        LIMIT :quantidadeBlocos
    )
""")
    fun excluirUltimosBlocos(quantidadeBlocos: Int)

    // Checa se nos N últimos blocos há alguma rifa já paga ou com vendedor
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
