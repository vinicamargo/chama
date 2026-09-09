package com.example.chama.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `removerAcentos deve remover acentos agudos, circunflexos, tis e graves`() {
        assertEquals("Joao", "João".removerAcentos())
        assertEquals("Andre", "André".removerAcentos())
        assertEquals("Vitoria", "Vitória".removerAcentos())
        assertEquals("Avo e avo", "Avô e avó".removerAcentos())
        assertEquals("Aquele", "Àquele".removerAcentos())
    }

    @Test
    fun `removerAcentos deve remover cedilha`() {
        assertEquals("Lourenco", "Lourenço".removerAcentos())
        assertEquals("CONCEICAO", "CONCEIÇÃO".removerAcentos())
        assertEquals("Franca", "França".removerAcentos())
    }

    @Test
    fun `removerAcentos deve manter caracteres sem acento inalterados`() {
        assertEquals("Lucas Santos", "Lucas Santos".removerAcentos())
        assertEquals("123456", "123456".removerAcentos())
        assertEquals("teste_123@email.com", "teste_123@email.com".removerAcentos())
    }

    @Test
    fun `removerAcentos deve manter espacos e pontuacoes intactos`() {
        val entrada = "  Olá, mundo! Tudo bem?  "
        val esperado = "  Ola, mundo! Tudo bem?  "
        assertEquals(esperado, entrada.removerAcentos())
    }

    @Test
    fun `removerAcentos deve lidar com string vazia`() {
        assertEquals("", "".removerAcentos())
    }
}