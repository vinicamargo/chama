package com.example.chama.data.entity

import androidx.room.Entity

@Entity(tableName = "presencas",
    primaryKeys = ["crismandoId", "data"]
)
data class Presenca (
    val crismandoId: Long,
    val data: String,
    val estaPresente: Boolean = false
)