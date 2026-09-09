package com.example.chama.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chama.data.AppDatabase
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Genero
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CrismandoDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var crismandoDao: CrismandoDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        crismandoDao = db.crismandoDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun inserirERecuperar_deveGerarIdAutoincrementalEPersistirCampos() = runBlocking {
        val crismando = Crismando(
            crismandoId = 0,
            nome = "Ana Clara",
            fotoUrl = null,
            dataNascimento = "2011-03-15",
            telefone = "11988887777",
            nomeResponsavel = "Patrícia",
            telefoneResponsavel = "11988886666",
            genero = Genero.FEMININO
        )

        val idGerado = crismandoDao.inserir(crismando)
        assertTrue(idGerado > 0)

        val lista = crismandoDao.getAllCrismandos().first()
        assertEquals(1, lista.size)

        val recuperado = lista.first()
        assertEquals(idGerado, recuperado.crismandoId)
        assertEquals("Ana Clara", recuperado.nome)
        assertEquals(Genero.FEMININO, recuperado.genero)
    }

    @Test
    fun atualizar_deveModificarDadosDoCrismandoExistente() = runBlocking {
        val crismando = Crismando(nome = "Lucas Antigo", genero = Genero.MASCULINO)
        val id = crismandoDao.inserir(crismando)

        val crismandoModificado = Crismando(
            crismandoId = id,
            nome = "Lucas Atualizado",
            telefone = "11999990000",
            genero = Genero.MASCULINO
        )
        crismandoDao.atualizar(crismandoModificado)

        val crismandos = crismandoDao.getAllCrismandos().first()
        val atualizado = crismandos.first { it.crismandoId == id }
        assertEquals("Lucas Atualizado", atualizado.nome)
        assertEquals("11999990000", atualizado.telefone)
    }

    @Test
    fun deletarCrismando_deveRemoverApenasOIdEspecificado() = runBlocking {
        val id1 = crismandoDao.inserir(Crismando(nome = "Aluno 1"))
        val id2 = crismandoDao.inserir(Crismando(nome = "Aluno 2"))

        crismandoDao.deletarCrismando(id1)

        val restantes = crismandoDao.getAllCrismandos().first()
        assertEquals(1, restantes.size)
        assertEquals(id2, restantes.first().crismandoId)
    }

    @Test
    fun deleteAllCrismandos_deveEsvaziarTabela() = runBlocking {
        crismandoDao.inserir(Crismando(nome = "Aluno 1"))
        crismandoDao.inserir(Crismando(nome = "Aluno 2"))

        crismandoDao.deleteAllCrismandos()

        val vazia = crismandoDao.getAllCrismandos().first()
        assertTrue(vazia.isEmpty())
    }
}