package com.example.chama.utils

import com.example.chama.data.entity.Genero
import java.text.Normalizer

object GeneroUtils {

    // Nomes femininos que não terminam com a letra "a"
    private val nomesFemininos = setOf(
        "alice", "beatriz", "clarice", "raquel", "rachel", "esther", "ester",
        "elis", "isabel", "liz", "ruth", "rute", "ingrid", "nicole", "yasmin",
        "iasmin", "carmen", "miriam", "myriam", "sarah", "deborah", "helen",
        "ellen", "elen", "karen", "carol", "kelly", "stephanie", "stefany",
        "ketlyn", "cibele", "monique", "solange", "cleo", "iris", "marise",
        "gisele", "arlete", "ivete", "lilian", "simone", "rose", "ines", "agnes"
    )

    // Nomes masculinos com terminações fora do padrão tradicional ("e", "i", "m", etc.)
    private val nomesMasculinos = setOf(
        "felipe", "philipe", "felippe", "henrique", "guilherme", "jorge",
        "alexandre", "andre", "davi", "david", "levi", "yuri", "iuri",
        "luigi", "kaua", "cauã", "caua", "breno", "ian", "yan", "jean",
        "joao", "luiz", "luis", "lucas", "marcos", "mateus", "matheus",
        "gabriel", "miguel", "samuel", "daniel", "rafael", "arthur", "artur",
        "heitor", "igor", "vitor", "victor", "valmir", "cesar", "caio",
        "bernardo", "enzo", "otavio", "marcelo", "murilo", "rodrigo", "pedro",
        "tiago", "thiago", "diego", "diogo", "bruno", "gustavo", "leonardo"
    )

    fun inferirGenero(nomeCompleto: String?): Genero {
        if (nomeCompleto.isNullOrBlank()) return Genero.MASCULINO

        // Normaliza removendo acentos e caracteres especiais para comparação segura
        val primeiroNome = nomeCompleto
            .trim()
            .split(" ")
            .firstOrNull()
            ?.let { removerAcentos(it.lowercase()) }
            ?: return Genero.MASCULINO

        return when {
            nomesFemininos.contains(primeiroNome) -> Genero.FEMININO
            nomesMasculinos.contains(primeiroNome) -> Genero.MASCULINO

            // Padrões morfológicos femininos
            primeiroNome.endsWith("a") ||
                    primeiroNome.endsWith("ah") ||
                    primeiroNome.endsWith("elly") ||
                    primeiroNome.endsWith("ely") ||
                    primeiroNome.endsWith("ine") ||
                    primeiroNome.endsWith("ete") -> Genero.FEMININO

            // Padrões morfológicos masculinos
            primeiroNome.endsWith("o") ||
                    primeiroNome.endsWith("os") ||
                    primeiroNome.endsWith("el") ||
                    primeiroNome.endsWith("or") ||
                    primeiroNome.endsWith("er") ||
                    primeiroNome.endsWith("on") ||
                    primeiroNome.endsWith("an") ||
                    primeiroNome.endsWith("ton") ||
                    primeiroNome.endsWith("son") ||
                    primeiroNome.endsWith("mar") ||
                    primeiroNome.endsWith("mir") ||
                    primeiroNome.endsWith("il") ||
                    primeiroNome.endsWith("ir") -> Genero.MASCULINO

            // Fallback para nomes não catalogados
            else -> Genero.MASCULINO
        }
    }

    private fun removerAcentos(texto: String): String {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}