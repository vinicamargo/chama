package com.example.chama.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chama.data.entity.Crismando
import kotlinx.coroutines.flow.Flow

@Dao
interface CrismandoDao {

    @Query("SELECT * FROM crismandos ORDER BY nome COLLATE LOCALIZED ASC ")
    fun getAllCrismandos(): Flow<List<Crismando>>

    @Query("SELECT * FROM crismandos ORDER BY nome COLLATE LOCALIZED ASC ")
    fun getAllCrismandosStatic(): List<Crismando>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun inserir(crismando: Crismando)

}