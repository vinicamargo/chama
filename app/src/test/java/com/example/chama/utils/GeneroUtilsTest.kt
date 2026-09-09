package com.example.chama.utils

import com.example.chama.data.entity.Genero
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneroUtilsTest {

    @Test
    fun `deve inferir nomes femininos que terminam em a`() {
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Maria da Silva"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("JULIANA SANTOS"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Ana Clara"))
    }

    @Test
    fun `deve inferir nomes femininos da lista de excecao que nao terminam em a`() {
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Beatriz Costa"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Alice Ferreira"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Raquel Gomes"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Yasmin Souza"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Carmen Miranda"))
    }

    @Test
    fun `deve inferir nomes masculinos da lista de excecao que fogem do padrao`() {
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Felipe Santos"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Guilherme Henrique"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Davi Lucca"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Alexandre Magno"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Breno Silva"))
    }

    @Test
    fun `deve tratar acentuacao e caracteres diacriticos corretamente`() {
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Cauã Pereira"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("André Luiz"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("João Pedro"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Inês Maria"))
    }

    @Test
    fun `deve respeitar padroes morfologicos masculinos e femininos de sufixo`() {
        // Sufixos femininos: ine, ete, ely, ah
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Francine Oliveira"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Janete Rodrigues"))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("Deborah Secco"))

        // Sufixos masculinos: on, el, or, son, mar
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Alison Barbosa"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Gabriel"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Vitor Hugo"))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("Valdemar"))
    }

    @Test
    fun `deve tratar casos de borda com espacos extras, nulos e vazios`() {
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero(null))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero(""))
        assertEquals(Genero.MASCULINO, GeneroUtils.inferirGenero("    "))
        assertEquals(Genero.FEMININO, GeneroUtils.inferirGenero("   Camila   Oliveira   "))
    }
}