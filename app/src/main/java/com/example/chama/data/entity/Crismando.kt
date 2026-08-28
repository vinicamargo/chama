package com.example.chama.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period
import kotlin.random.Random

enum class Genero {
    MASCULINO, FEMININO
}

@Entity(tableName = "crismandos")
data class Crismando (
    @PrimaryKey(autoGenerate = true)
    val crismandoId: Long = Random.nextLong(1, Long.MAX_VALUE),
    val nome: String,
    val fotoUrl: String? = null,
    val dataNascimento: String? = null,
    val telefone: String? = null,
    val nomeResponsavel: String? = null,
    val telefoneResponsavel: String? = null,
    val genero: Genero? = null
) {
    val idade: Int?
        get() {
            return if (!dataNascimento.isNullOrBlank()) {
                runCatching {
                    val nascimento = LocalDate.parse(dataNascimento)
                    Period.between(nascimento, LocalDate.now()).years
                }.getOrNull()
            } else {
                null
            }
        }
}