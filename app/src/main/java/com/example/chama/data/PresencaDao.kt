package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chama.data.Presenca
import kotlinx.coroutines.flow.Flow

@Dao
interface PresencaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun gerarListaPresenca(presencas: List<Presenca>)

    @Query("SELECT * FROM presencas WHERE data = :data")
    fun buscarPresencasDoDia(data: String): Flow<List<Presenca>>

    @Query("SELECT estaPresente FROM presencas WHERE crismandoId = :crismandoId AND data = :data")
    fun buscarPresencaDoDiaPorCrismando(crismandoId: Long, data: String): Boolean

    @Query("UPDATE presencas SET estaPresente = :status WHERE crismandoId = :crismandoId AND data = :data")
    fun atualizarPresenca(crismandoId: Long, data: String, status: Boolean)

}