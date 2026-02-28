package com.example.chama.data.model

import com.example.chama.utils.TipoVendedor

data class PessoaVendedora (
    val id: Long,
    val nome: String,
    val tipo: TipoVendedor
)