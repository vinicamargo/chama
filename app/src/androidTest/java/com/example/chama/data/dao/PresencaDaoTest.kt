package com.example.chama.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chama.data.AppDatabase
import com.example.chama.data.entity.Presenca
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
class PresencaDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var presencaDao: PresencaDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        presencaDao = db.presencaDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun gerarListaPresencaEBuscarDias_deveCadastrarEDevolverDatasDistintas() = runBlocking {
        val presencas = listOf(
            Presenca(crismandoId = 1, data = "2026-09-06", estaPresente = false),
            Presenca(crismandoId = 2, data = "2026-09-06", estaPresente = true),
            Presenca(crismandoId = 1, data = "2026-09-13", estaPresente = false)
        )
        presencaDao.gerarListaPresenca(presencas)

        val dias = presencaDao.buscarDiasComPresencas().first()
        assertEquals(2, dias.size)
        assertTrue(dias.contains("2026-09-06"))
        assertTrue(dias.contains("2026-09-13"))
    }

    @Test
    fun atualizarPresenca_deveModificarApenasORegistroDoAlunoNoDiaEspecificado() {
        val presencas = listOf(
            Presenca(crismandoId = 1, data = "2026-09-06", estaPresente = false),
            Presenca(crismandoId = 1, data = "2026-09-13", estaPresente = false)
        )
        presencaDao.gerarListaPresenca(presencas)

        presencaDao.atualizarPresenca(crismandoId = 1, data = "2026-09-06", status = true)

        val presenteDia6 = presencaDao.buscarPresencaDoDiaPorCrismando(1, "2026-09-06")
        val presenteDia13 = presencaDao.buscarPresencaDoDiaPorCrismando(1, "2026-09-13")

        assertTrue(presenteDia6)
        assertFalse(presenteDia13)
    }

    @Test
    fun deletarPresencasPorCrismando_deveRemoverSomentePresencasDoCrismandoAlvo() {
        val presencas = listOf(
            Presenca(crismandoId = 10, data = "2026-09-06", estaPresente = true),
            Presenca(crismandoId = 10, data = "2026-09-13", estaPresente = true),
            Presenca(crismandoId = 20, data = "2026-09-06", estaPresente = true)
        )
        presencaDao.gerarListaPresenca(presencas)

        presencaDao.deletarPresencasPorCrismando(10)

        val todas = presencaDao.buscarTodasAsPresencasStatic()
        assertEquals(1, todas.size)
        assertEquals(20L, todas.first().crismandoId)
    }
}