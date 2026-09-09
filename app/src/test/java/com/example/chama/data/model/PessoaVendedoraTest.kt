package com.example.chama.data.model

import com.example.chama.utils.TipoVendedor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PessoaVendedoraTest {

    @Test
    fun testPessoaVendedoraPropriedadesEMetodosSinteticos() {
        val pessoa1 = PessoaVendedora(id = 1L, nome = "Lucas", tipo = TipoVendedor.CRISMANDO)
        val pessoa2 = PessoaVendedora(id = 1L, nome = "Lucas", tipo = TipoVendedor.CRISMANDO)
        val pessoaDiferente = PessoaVendedora(id = 2L, nome = "Maria", tipo = TipoVendedor.EXTERNO)

        // Getters e Construtor
        assertEquals(1L, pessoa1.id)
        assertEquals("Lucas", pessoa1.nome)
        assertEquals(TipoVendedor.CRISMANDO, pessoa1.tipo)

        // Destructuring (component1, component2, component3)
        val (id, nome, tipo) = pessoa1
        assertEquals(1L, id)
        assertEquals("Lucas", nome)
        assertEquals(TipoVendedor.CRISMANDO, tipo)

        // Equals e HashCode
        assertEquals(pessoa1, pessoa2)
        assertNotEquals(pessoa1, pessoaDiferente)
        assertEquals(pessoa1.hashCode(), pessoa2.hashCode())

        // Copy
        val copia = pessoa1.copy(nome = "Lucas Modificado")
        assertEquals("Lucas Modificado", copia.nome)
        assertEquals(1L, copia.id)

        // ToString
        assertTrue(pessoa1.toString().contains("Lucas"))
    }
}