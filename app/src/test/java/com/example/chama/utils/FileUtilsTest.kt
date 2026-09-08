package com.example.chama.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FileUtilsTest {

    @Test
    fun `gerarChaveCrismando deve gerar MD5 correto e consistente`() {
        val chave1 = FileUtils.gerarChaveCrismando("Maria Silva", "2010-05-18")
        val chave2 = FileUtils.gerarChaveCrismando("Maria Silva", "2010-05-18")

        // Chave gerada deve ser idêntica para as mesmas entradas
        assertEquals(chave1, chave2)
        assertEquals(32, chave1.length) // Comprimento padrão de uma hash MD5 em hex
    }

    @Test
    fun `gerarChaveCrismando deve ser insensivel a maiusculas, minusculas e espacos nas pontas`() {
        val chavePadrao = FileUtils.gerarChaveCrismando("ana clara", "2011-03-15")
        val chaveComVariacoes = FileUtils.gerarChaveCrismando("  ANA CLARA  ", " 2011-03-15 ")

        assertEquals(chavePadrao, chaveComVariacoes)
    }

    @Test
    fun `gerarChaveCrismando deve tratar data de nascimento nula sem quebrar`() {
        val chaveSemData = FileUtils.gerarChaveCrismando("João Pedro", null)
        val chaveComDataVazia = FileUtils.gerarChaveCrismando("João Pedro", "")

        assertEquals(chaveSemData, chaveComDataVazia)
        assertEquals(32, chaveSemData.length)
    }

    @Test
    fun `gerarChaveCrismando deve gerar hashes distintas para pessoas com nomes ou datas diferentes`() {
        val chaveAlunoA = FileUtils.gerarChaveCrismando("Lucas Santos", "2010-01-01")
        val chaveAlunoB = FileUtils.gerarChaveCrismando("Lucas Santos", "2010-01-02")
        val chaveAlunoC = FileUtils.gerarChaveCrismando("Marcos Santos", "2010-01-01")

        assertNotEquals(chaveAlunoA, chaveAlunoB)
        assertNotEquals(chaveAlunoA, chaveAlunoC)
    }
}