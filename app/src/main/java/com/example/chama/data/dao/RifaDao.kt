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
    @Query("SELECT COUNT(*) FROM rifas")
    fun contarRifas(): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun inserirRifas(rifas: List<Rifa>)
    @Query("UPDATE rifas SET vendedorId = :vendedorId WHERE bloco = :numBloco")
    fun vincularVendedorAoBloco(vendedorId: Long, numBloco: Int)
    @Query("UPDATE rifas SET vendedorId = NULL WHERE bloco = :numBloco")
    fun desvincularVendedorDoBloco(numBloco: Int)
}