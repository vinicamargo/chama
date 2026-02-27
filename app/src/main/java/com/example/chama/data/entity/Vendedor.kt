package com.example.chama.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chama.utils.TipoVendedor

@Entity(tableName = "vendedores")
data class Vendedor(
    @PrimaryKey
    val vendedorId: Long,
    val tipo: TipoVendedor,
    val nomeExterno: String? = null
)

