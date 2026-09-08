package com.example.chama.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalizacaoUtilsTest {

    @Test
    fun normalizarNome() {
        // Converte para Title Case mantendo preposições minúsculas e limpando espaços extras
        assertEquals("Ana Clara da Silveira", NormalizacaoUtils.normalizarNome("ANA CLARA DA SILVEIRA"))
        assertEquals("Enzo Gabriel dos Santos", NormalizacaoUtils.normalizarNome("  enzo   gabriel  DOS  santos  "))
        assertEquals("", NormalizacaoUtils.normalizarNome(null))
        assertEquals("", NormalizacaoUtils.normalizarNome("   "))
    }

    @Test
    fun normalizarTelefone() {
        // Remove caracteres especiais e o DDI 55
        assertEquals("11987654321", NormalizacaoUtils.normalizarTelefone("+55 (11) 98765-4321"))
        assertEquals("11987654321", NormalizacaoUtils.normalizarTelefone("11987654321"))

        // Números inválidos ou vazios devem retornar null
        assertNull(NormalizacaoUtils.normalizarTelefone("123"))
        assertNull(NormalizacaoUtils.normalizarTelefone(null))
        assertNull(NormalizacaoUtils.normalizarTelefone(""))
    }

    @Test
    fun normalizarDataNascimento() {
        // Suporta formato brasileiro DD/MM/AAAA, formatos com horário e ISO
        assertEquals("2011-03-15", NormalizacaoUtils.normalizarDataNascimento("15/03/2011"))
        assertEquals("2012-11-20", NormalizacaoUtils.normalizarDataNascimento("20/11/2012 14:30:00"))
        assertEquals("2010-05-08", NormalizacaoUtils.normalizarDataNascimento("2010-05-08"))

        // Entradas inválidas retornam null
        assertNull(NormalizacaoUtils.normalizarDataNascimento("data_invalida"))
        assertNull(NormalizacaoUtils.normalizarDataNascimento(null))
    }

    @Test
    fun normalizarPresenca() {
        // Marcadores válidos de presença
        assertTrue(NormalizacaoUtils.normalizarPresenca("O"))
        assertTrue(NormalizacaoUtils.normalizarPresenca("p"))
        assertTrue(NormalizacaoUtils.normalizarPresenca("1"))
        assertTrue(NormalizacaoUtils.normalizarPresenca("PRESENTE"))
        assertTrue(NormalizacaoUtils.normalizarPresenca("Sim"))

        // Faltas ou entradas vazias
        assertFalse(NormalizacaoUtils.normalizarPresenca(""))
        assertFalse(NormalizacaoUtils.normalizarPresenca(null))
        assertFalse(NormalizacaoUtils.normalizarPresenca("F"))
    }
}