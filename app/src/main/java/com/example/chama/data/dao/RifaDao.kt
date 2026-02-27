package com.example.chama.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chama.data.entity.Rifa

@Dao
interface RifaDao {
    @Query("SELECT COUNT(*) FROM rifas")
    fun contarRifas(): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun inserirRifas(rifas: List<Rifa>)
}