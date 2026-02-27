package com.example.chama.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rifas",
    foreignKeys = [
        ForeignKey(
            entity = Vendedor::class,
            parentColumns = ["vendedorId"],
            childColumns = ["vendedorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("vendedorId")]
)
data class Rifa (
    @PrimaryKey
    val numero: Int,
    val bloco: Int,
    val vendedorId: Long? = null,
    val estaPaga: Boolean = false,
    val nomeComprador: String? = null
)