package com.example.chama.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chama.data.entity.Vendedor
import kotlinx.coroutines.flow.Flow

@Dao
interface VendedorDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun inserirVendedor(vendedor: Vendedor)

    @Query("SELECT * FROM vendedores")
    fun listarTodos(): Flow<List<Vendedor>>

    @Query("DELETE FROM vendedores WHERE tipo = 'CRISMANDO'")
    fun deleteAllCrismandos()

}