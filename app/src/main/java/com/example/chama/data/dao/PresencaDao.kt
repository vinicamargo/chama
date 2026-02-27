package com.example.chama.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chama.data.entity.Presenca
import kotlinx.coroutines.flow.Flow

@Dao
interface PresencaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun gerarListaPresenca(presencas: List<Presenca>)

    @Query("SELECT * FROM presencas WHERE data = :data")
    fun buscarPresencasPorData(data: String): Flow<List<Presenca>>

    @Query("SELECT estaPresente FROM presencas WHERE crismandoId = :crismandoId AND data = :data")
    fun buscarPresencaDoDiaPorCrismando(crismandoId: Long, data: String): Boolean

    @Query("SELECT * FROM presencas")
    fun buscarTodasAsPresencasStatic(): List<Presenca>

    @Query("SELECT DISTINCT data FROM presencas")
    fun buscarDiasComPresencas(): Flow<List<String>>

    @Query("UPDATE presencas SET estaPresente = :status WHERE crismandoId = :crismandoId AND data = :data")
    fun atualizarPresenca(crismandoId: Long, data: String, status: Boolean)

    @Query("DELETE FROM presencas")
    fun deleteAllPresencas()
}