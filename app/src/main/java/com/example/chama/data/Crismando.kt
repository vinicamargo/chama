package com.example.chama.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.random.Random

@Entity(tableName = "crismandos")
data class Crismando (
    @PrimaryKey(autoGenerate = true)
    val id: Long = Random.nextLong(1, Long.MAX_VALUE),
    val nome: String
)