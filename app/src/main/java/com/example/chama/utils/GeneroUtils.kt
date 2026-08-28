package com.example.chama.utils

import com.example.chama.data.entity.Genero

object GeneroUtils {
    private val nomesMasculinosExcecao = setOf(
        "lucas", "gabriel", "felipe", "andre", "alexandre", "davi", "guilherme",
        "henrique", "jorge", "mateus", "matheus", "arthur", "artur", "miguel",
        "samuel", "daniel", "rafael", "heitor", "cauã", "breno", "ian", "yuri"
    )

    private val nomesFemininosExcecao = setOf(
        "beatriz", "alice", "yasmin", "iasmin", "ester", "esther", "rachel",
        "raquel", "elis", "elizabeth", "isabel", "nicole", "ingrid", "carmen"
    )

    fun inferirGenero(nomeCompleto: String?): Genero? {
        if (nomeCompleto.isNullOrBlank()) return null

        val primeiroNome = nomeCompleto
            .trim()
            .split(" ")
            .firstOrNull()
            ?.lowercase() ?: return null

        return when {
            nomesMasculinosExcecao.contains(primeiroNome) -> Genero.MASCULINO
            nomesFemininosExcecao.contains(primeiroNome) -> Genero.FEMININO
            primeiroNome.endsWith("a") -> Genero.FEMININO
            primeiroNome.endsWith("o") || primeiroNome.endsWith("os") ||
                    primeiroNome.endsWith("el") || primeiroNome.endsWith("or") -> Genero.MASCULINO
            else -> null
        }
    }
}