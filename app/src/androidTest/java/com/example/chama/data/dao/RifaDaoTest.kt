package com.example.chama.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chama.data.AppDatabase
import com.example.chama.data.entity.Rifa
import com.example.chama.data.entity.Vendedor
import com.example.chama.utils.TipoVendedor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RifaDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var rifaDao: RifaDao
    private lateinit var vendedorDao: VendedorDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        rifaDao = db.rifaDao()
        vendedorDao = db.vendedorDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getMaiorNumeroRifa_deveRetornarZeroQuandoVazioEMaiorNumeroQuandoPopulada() {
        assertEquals(0, rifaDao.getMaiorNumeroRifa())

        val rifas = listOf(
            Rifa(numero = 1, bloco = 1, estaPaga = false, vendedorId = null),
            Rifa(numero = 25, bloco = 3, estaPaga = false, vendedorId = null),
            Rifa(numero = 10, bloco = 1, estaPaga = false, vendedorId = null)
        )
        rifaDao.inserirRifas(rifas)

        assertEquals(25, rifaDao.getMaiorNumeroRifa())
    }

    @Test
    fun vincularEDesvincularVendedor_deveAtualizarTodasAsRifasDoBloco() = runBlocking {
        // Cadastra o vendedor primeiro para respeitar a Foreign Key
        vendedorDao.inserirVendedor(
            Vendedor(vendedorId = 99L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Carlos")
        )

        val rifasBloco1 = (1..10).map { Rifa(numero = it, bloco = 1, estaPaga = false, vendedorId = null) }
        rifaDao.inserirRifas(rifasBloco1)

        rifaDao.vincularVendedorAoBloco(vendedorId = 99L, numBloco = 1)
        val rifasAtualizadas = rifaDao.getRifas().first()
        assertTrue(rifasAtualizadas.all { it.vendedorId == 99L })

        rifaDao.desvincularVendedorDoBloco(numBloco = 1)
        val rifasLimpa = rifaDao.getRifas().first()
        assertTrue(rifasLimpa.all { it.vendedorId == null })
    }

    @Test
    fun atualizarPagamentoBloco_deveMarcarTodasAsRifasDoBlocoComoPagas() = runBlocking {
        val rifas = listOf(
            Rifa(numero = 1, bloco = 1, estaPaga = false, vendedorId = null),
            Rifa(numero = 2, bloco = 1, estaPaga = false, vendedorId = null),
            Rifa(numero = 11, bloco = 2, estaPaga = false, vendedorId = null)
        )
        rifaDao.inserirRifas(rifas)

        rifaDao.atualizarPagamentoBloco(bloco = 1, estaPaga = true)

        val resultado = rifaDao.getRifas().first()
        assertTrue(resultado.filter { it.bloco == 1 }.all { it.estaPaga })
        assertFalse(resultado.first { it.bloco == 2 }.estaPaga)
    }

    @Test
    fun contarRifasEmUsoNosUltimosBlocos_deveIdentificarRifasPagasOuComVendedor() = runBlocking {
        // Cadastra o vendedor antes de vinculá-lo à rifa
        vendedorDao.inserirVendedor(
            Vendedor(vendedorId = 5L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Vendedor Teste")
        )

        val rifas = listOf(
            // Bloco 1 (livre)
            Rifa(numero = 1, bloco = 1, estaPaga = false, vendedorId = null),
            // Bloco 2 (com vendedor)
            Rifa(numero = 11, bloco = 2, estaPaga = false, vendedorId = 5L),
            // Bloco 3 (pago)
            Rifa(numero = 21, bloco = 3, estaPaga = true, vendedorId = null)
        )
        rifaDao.inserirRifas(rifas)

        // Verificando apenas o último bloco (bloco 3)
        assertEquals(1, rifaDao.contarRifasEmUsoNosUltimosBlocos(quantidadeBlocos = 1))

        // Verificando os 2 últimos blocos (bloco 2 e 3)
        assertEquals(2, rifaDao.contarRifasEmUsoNosUltimosBlocos(quantidadeBlocos = 2))
    }

    @Test
    fun excluirUltimosBlocos_deveRemoverApenasOsBlocosComMaiorNumero() = runBlocking {
        val rifas = (1..30).map { numero ->
            Rifa(
                numero = numero,
                bloco = ((numero - 1) / 10) + 1,
                estaPaga = false,
                vendedorId = null
            )
        }
        rifaDao.inserirRifas(rifas)

        // Exclui o último bloco (bloco 3 - números 21 a 30)
        rifaDao.excluirUltimosBlocos(quantidadeBlocos = 1)

        val restantes = rifaDao.getRifas().first()
        assertEquals(20, restantes.size)
        assertTrue(restantes.none { it.bloco == 3 })
        assertEquals(20, rifaDao.getMaiorNumeroRifa())
    }

    @Test
    fun getRifaPorNumero_deveRetornarRifaCorrespondente() {
        val rifas = listOf(
            Rifa(numero = 10, bloco = 1, estaPaga = false, vendedorId = null),
            Rifa(numero = 11, bloco = 2, estaPaga = true, vendedorId = null)
        )
        rifaDao.inserirRifas(rifas)

        val rifaRecuperada = rifaDao.getRifaPorNumero(11)

        assertEquals(11, rifaRecuperada.numero)
        assertEquals(2, rifaRecuperada.bloco)
        assertTrue(rifaRecuperada.estaPaga)
    }

    @Test
    fun getMaiorNumeroBloco_deveRetornarZeroQuandoVazioEMaiorNumeroQuandoPopulada() {
        assertEquals(0, rifaDao.getMaiorNumeroBloco())

        val rifas = listOf(
            Rifa(numero = 1, bloco = 1, estaPaga = false, vendedorId = null),
            Rifa(numero = 25, bloco = 3, estaPaga = false, vendedorId = null),
            Rifa(numero = 15, bloco = 2, estaPaga = false, vendedorId = null)
        )
        rifaDao.inserirRifas(rifas)

        assertEquals(3, rifaDao.getMaiorNumeroBloco())
    }

    @Test
    fun desvincularRifasDoVendedor_deveLimparApenasAsRifasDoVendedorAlvo() = runBlocking {
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 1L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Vendedor 1"))
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 2L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Vendedor 2"))

        val rifas = listOf(
            Rifa(numero = 1, bloco = 1, estaPaga = false, vendedorId = 1L),
            Rifa(numero = 2, bloco = 1, estaPaga = false, vendedorId = 1L),
            Rifa(numero = 11, bloco = 2, estaPaga = false, vendedorId = 2L)
        )
        rifaDao.inserirRifas(rifas)

        rifaDao.desvincularRifasDoVendedor(1L)

        val lista = rifaDao.getRifas().first()
        assertTrue(lista.filter { it.bloco == 1 }.all { it.vendedorId == null })
        assertEquals(2L, lista.first { it.bloco == 2 }.vendedorId)
    }

    @Test
    fun buscarDonoDoBloco_deveRetornarBlocoDonoInfoQuandoVinculadoACrismando() = runBlocking {
        val crismandoDao = db.crismandoDao()
        val crismandoId = crismandoDao.inserir(
            com.example.chama.data.entity.Crismando(nome = "Lucas Teste")
        )

        vendedorDao.inserirVendedor(
            Vendedor(vendedorId = crismandoId, tipo = TipoVendedor.CRISMANDO)
        )

        val rifa = Rifa(numero = 41, bloco = 5, estaPaga = false, vendedorId = crismandoId)
        rifaDao.inserirRifas(listOf(rifa))

        val dono = rifaDao.buscarDonoDoBloco(5)
        org.junit.Assert.assertNotNull(dono)
        assertEquals(crismandoId, dono?.vendedorId)
        assertEquals("Lucas Teste", dono?.nomeVendedor)

        val semDono = rifaDao.buscarDonoDoBloco(99)
        org.junit.Assert.assertNull(semDono)
    }
}