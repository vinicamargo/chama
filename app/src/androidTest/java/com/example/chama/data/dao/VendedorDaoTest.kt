package com.example.chama.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chama.data.AppDatabase
import com.example.chama.data.entity.Vendedor
import com.example.chama.utils.TipoVendedor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VendedorDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var vendedorDao: VendedorDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        vendedorDao = db.vendedorDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun inserirERecuperar_deveListarVendedoresCorretamente() = runBlocking {
        val vendedorCrismando = Vendedor(vendedorId = 1L, tipo = TipoVendedor.CRISMANDO)
        val vendedorExterno = Vendedor(vendedorId = 2L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Padre Paulo")

        vendedorDao.inserirVendedor(vendedorCrismando)
        vendedorDao.inserirVendedor(vendedorExterno)

        val lista = vendedorDao.getAllVendedores().first()
        assertEquals(2, lista.size)
        assertTrue(lista.any { it.tipo == TipoVendedor.CRISMANDO && it.vendedorId == 1L })
        assertTrue(lista.any { it.tipo == TipoVendedor.EXTERNO && it.nomeExterno == "Padre Paulo" })
    }

    @Test
    fun deletarVendedorPorId_deveRemoverApenasOAlvo() = runBlocking {
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 10L, tipo = TipoVendedor.CRISMANDO))
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 20L, tipo = TipoVendedor.COLABORADOR, nomeExterno = "Tia Ana"))

        vendedorDao.deletarVendedorPorId(10L)

        val restantes = vendedorDao.getAllVendedores().first()
        assertEquals(1, restantes.size)
        assertEquals(20L, restantes.first().vendedorId)
    }

    @Test
    fun deletarVendedoresCRISMANDO_deveManterApenasExternosEColaboradores() = runBlocking {
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 1L, tipo = TipoVendedor.CRISMANDO))
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 2L, tipo = TipoVendedor.CRISMANDO))
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 3L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Vendedor Loja"))
        vendedorDao.inserirVendedor(Vendedor(vendedorId = 4L, tipo = TipoVendedor.COLABORADOR, nomeExterno = "Catequista"))

        vendedorDao.deletarVendedoresCRISMANDO()

        val restantes = vendedorDao.getAllVendedores().first()
        assertEquals(2, restantes.size)
        assertTrue(restantes.none { it.tipo == TipoVendedor.CRISMANDO })
    }
}