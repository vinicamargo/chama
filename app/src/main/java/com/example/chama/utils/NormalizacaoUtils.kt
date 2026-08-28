package com.example.chama.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object NormalizacaoUtils {

    fun normalizarNome(nomeBruto: String?): String {
        if (nomeBruto.isNullOrBlank()) return ""
        val preposicoes = setOf("de", "da", "do", "das", "dos", "e")

        return nomeBruto
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(" ")
            .joinToString(" ") { palavra ->
                val palavraMinuscula = palavra.lowercase(Locale.ROOT)
                if (preposicoes.contains(palavraMinuscula)) {
                    palavraMinuscula
                } else {
                    palavraMinuscula.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                }
            }
    }

    fun normalizarTelefone(telefoneBruto: String?): String? {
        if (telefoneBruto.isNullOrBlank()) return null

        var digitos = telefoneBruto.filter { it.isDigit() }

        // Remove prefixo 55 caso o formulário tenha capturado com código de país
        if (digitos.startsWith("55") && digitos.length in 12..13) {
            digitos = digitos.substring(2)
        }

        return if (digitos.length in 10..11) digitos else digitos.ifBlank { null }
    }

    fun normalizarDataNascimento(dataBruta: String?): String? {
        if (dataBruta.isNullOrBlank()) return null

        // Remove partes de horário se houver timestamp (ex: "15/03/2011 14:30:00")
        val apenasData = dataBruta.trim().split(" ")[0]

        val formatos = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
        )

        for (formatter in formatos) {
            val resultado = runCatching {
                LocalDate.parse(apenasData, formatter).toString()
            }.getOrNull()

            if (resultado != null) return resultado
        }

        return null
    }

    fun normalizarPresenca(valorBruto: String?): Boolean {
        if (valorBruto.isNullOrBlank()) return false
        val valor = valorBruto.trim().uppercase()
        return valor in setOf("O", "P", "1", "TRUE", "SIM", "X", "PRESENTE")
    }
}