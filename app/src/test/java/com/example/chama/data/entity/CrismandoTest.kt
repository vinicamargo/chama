package com.example.chama.data.entity

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class CrismandoTest {

    @Test
    fun idade_deveCobrirTodosOsRamosDeDataNascimento() {
        val dataDezAnosAtras = LocalDate.now().minusYears(10).toString()
        val crismandoComIdade = Crismando(nome = "Lucas", dataNascimento = dataDezAnosAtras)
        Assert.assertEquals(10, crismandoComIdade.idade)

        val crismandoDataNula = Crismando(nome = "Ana", dataNascimento = null)
        Assert.assertNull(crismandoDataNula.idade)

        val crismandoDataVazia = Crismando(nome = "Pedro", dataNascimento = "   ")
        Assert.assertNull(crismandoDataVazia.idade)

        val crismandoDataInvalida = Crismando(nome = "Carla", dataNascimento = "31/02/2010")
        Assert.assertNull(crismandoDataInvalida.idade)
    }

    @Test
    fun batismoPendenteDocumentacao_deveCobrirTodasAsCombinacoesLogicas() {
        val naoBatizado = Crismando(nome = "João", isBatizado = false)
        Assert.assertFalse(naoBatizado.batismoPendenteDocumentacao)

        val semParoquia = Crismando(
            nome = "Maria",
            isBatizado = true,
            batizadoNaDiocese = true,
            paroquiaBatismo = ""
        )
        Assert.assertTrue(semParoquia.batismoPendenteDocumentacao)

        val foraDioceseSemCertidao = Crismando(
            nome = "Marcos",
            isBatizado = true,
            batizadoNaDiocese = false,
            certidaoBatismoEntregue = false,
            certidaoBatismoUrl = null
        )
        Assert.assertTrue(foraDioceseSemCertidao.batismoPendenteDocumentacao)

        val regular = Crismando(
            nome = "Bia",
            isBatizado = true,
            batizadoNaDiocese = true,
            paroquiaBatismo = "Paróquia Central"
        )
        Assert.assertFalse(regular.batismoPendenteDocumentacao)
    }
}
