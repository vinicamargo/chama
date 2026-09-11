package com.example.chama.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.Period

enum class Genero {
    MASCULINO,
    FEMININO
}

@Entity(
    tableName = "crismandos",
    indices = [
        Index(value = ["cpf"], unique = true)
    ]
)
data class Crismando(
    @PrimaryKey(autoGenerate = true)
    val crismandoId: Long = 0L,

    val nome: String,
    val dataNascimento: String? = null,
    val cpf: String? = null,
    val celular: String? = null,
    val fotoUrl: String? = null,
    val genero: Genero? = null,

    // Naturalidade e Origem
    val cidadeNascimento: String? = null,
    val estadoNascimento: String? = null,
    val paisNascimento: String? = "Brasil",

    val endereco: String? = null,
    val cep: String? = null,
    val cidadeAtual: String? = "Santo André",

    val nomePai: String? = null,
    val nomeMae: String? = null,
    val relacionamentoResponsavel: String? = null,
    val celularResponsavel: String? = null,

    val isBatizado: Boolean = false,
    val batizadoNaDiocese: Boolean = true,
    val paroquiaBatismo: String? = null,
    val cidadeBatismo: String? = null,
    val certidaoBatismoEntregue: Boolean = false,
    val certidaoBatismoUrl: String? = null,
    val temPrimeiraComunhao: Boolean = false,
) {
    val idade: Int?
        get() = runCatching {
            dataNascimento?.let { Period.between(LocalDate.parse(it), LocalDate.now()).years }
        }.getOrNull()

    val batismoPendenteDocumentacao: Boolean
        get() {
            if (!isBatizado) return false
            return if (batizadoNaDiocese) {
                paroquiaBatismo.isNullOrBlank()
            } else {
                !certidaoBatismoEntregue && certidaoBatismoUrl.isNullOrBlank()
            }
        }
}