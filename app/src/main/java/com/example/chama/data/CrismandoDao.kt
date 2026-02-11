package com.example.chama.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrismandoDao {

    @Query("SELECT * FROM crismandos ORDER BY nome ASC ")
    fun getAllCrismandos(): Flow<List<Crismando>>

    @Query("SELECT * FROM crismandos ORDER BY nome ASC ")
    fun getAllCrismandosStatic(): List<Crismando>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserir(crismando: Crismando)

}