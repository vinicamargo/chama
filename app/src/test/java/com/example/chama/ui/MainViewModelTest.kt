package com.example.chama.ui

import androidx.compose.runtime.snapshots.Snapshot
import com.example.chama.FiltroPresenca
import com.example.chama.data.dao.CrismandoDao
import com.example.chama.data.dao.PresencaDao
import com.example.chama.data.dao.RifaDao
import com.example.chama.data.dao.VendedorDao
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Genero
import com.example.chama.data.entity.Presenca
import com.example.chama.data.entity.Rifa
import com.example.chama.data.entity.Vendedor
import com.example.chama.utils.TipoVendedor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val crismandoDao: CrismandoDao = mockk(relaxed = true)
    private val presencaDao: PresencaDao = mockk(relaxed = true)
    private val vendedorDao: VendedorDao = mockk(relaxed = true)
    private val rifaDao: RifaDao = mockk(relaxed = true)

    private val diasComChamadaFlow = MutableStateFlow<List<String>>(listOf("2026-09-06", "2026-09-13"))
    private val todosCrismandosFlow = MutableStateFlow<List<Crismando>>(emptyList())
    private val todasPresencasFlow = MutableStateFlow<List<Presenca>>(emptyList())
    private val todasRifasFlow = MutableStateFlow<List<Rifa>>(emptyList())
    private val todosVendedoresFlow = MutableStateFlow<List<Vendedor>>(emptyList())
    private val presencasPorDataFlow = MutableStateFlow<List<Presenca>>(emptyList())

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { presencaDao.buscarDiasComPresencas() } returns diasComChamadaFlow
        every { crismandoDao.getAllCrismandos() } returns todosCrismandosFlow
        every { presencaDao.buscarTodasAsPresencas() } returns todasPresencasFlow
        every { rifaDao.getRifas() } returns todasRifasFlow
        every { vendedorDao.getAllVendedores() } returns todosVendedoresFlow
        every { presencaDao.buscarPresencasPorData(any()) } returns presencasPorDataFlow

        viewModel = MainViewModel(crismandoDao, presencaDao, vendedorDao, rifaDao)
        viewModel.alterarData("2026-09-06")
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filtro por nome deve ignorar acentos e maiusculas`() = runTest {
        backgroundScope.launch { viewModel.listaCrismandosOriginal.collect {} }
        backgroundScope.launch { viewModel.listaCrismandosFiltrada.collect {} }
        backgroundScope.launch { viewModel.presencasDoDia.collect {} }

        val crismandos = listOf(
            Crismando(crismandoId = 1, nome = "João Pedro"),
            Crismando(crismandoId = 2, nome = "Ana Clara"),
            Crismando(crismandoId = 3, nome = "Clara Francisca")
        )
        todosCrismandosFlow.value = crismandos
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.alterarFiltroNome("joao")
        Snapshot.sendApplyNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.listaCrismandosFiltrada.value.size)
        assertEquals("João Pedro", viewModel.listaCrismandosFiltrada.value.first().nome)

        viewModel.alterarFiltroNome("CLARA")
        Snapshot.sendApplyNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.listaCrismandosFiltrada.value.size)
    }

    @Test
    fun `filtro de presenca deve filtrar corretamente entre PRESENTES e AUSENTES`() = runTest {
        backgroundScope.launch { viewModel.listaCrismandosOriginal.collect {} }
        backgroundScope.launch { viewModel.listaCrismandosFiltrada.collect {} }
        backgroundScope.launch { viewModel.presencasDoDia.collect {} }

        val crismando1 = Crismando(crismandoId = 1, nome = "Ana")
        val crismando2 = Crismando(crismandoId = 2, nome = "Bruno")
        todosCrismandosFlow.value = listOf(crismando1, crismando2)

        presencasPorDataFlow.value = listOf(
            Presenca(crismandoId = 1, data = "2026-09-06", estaPresente = true),
            Presenca(crismandoId = 2, data = "2026-09-06", estaPresente = false)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        Snapshot.sendApplyNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.listaCrismandosFiltrada.value.size)
        assertEquals(1L, viewModel.listaCrismandosFiltrada.value.first().crismandoId)

        viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        Snapshot.sendApplyNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.listaCrismandosFiltrada.value.size)
        assertEquals(2L, viewModel.listaCrismandosFiltrada.value.first().crismandoId)
    }

    @Test
    fun `totalPresentes e totalAusentes devem calcular metricas com precisao`() = runTest {
        backgroundScope.launch { viewModel.listaCrismandosOriginal.collect {} }
        backgroundScope.launch { viewModel.totalPresentes.collect {} }
        backgroundScope.launch { viewModel.totalAusentes.collect {} }
        backgroundScope.launch { viewModel.presencasDoDia.collect {} }

        todosCrismandosFlow.value = listOf(
            Crismando(crismandoId = 1, nome = "Ana"),
            Crismando(crismandoId = 2, nome = "Bruno"),
            Crismando(crismandoId = 3, nome = "Carlos")
        )

        presencasPorDataFlow.value = listOf(
            Presenca(crismandoId = 1, data = "2026-09-06", estaPresente = true),
            Presenca(crismandoId = 2, data = "2026-09-06", estaPresente = true),
            Presenca(crismandoId = 3, data = "2026-09-06", estaPresente = false)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.totalPresentes.value)
        assertEquals(1, viewModel.totalAusentes.value)
    }

    @Test
    fun `selecionarCrismando deve alternar selecao ao clicar no mesmo elemento`() {
        val crismando = Crismando(crismandoId = 10, nome = "Lucas")

        viewModel.selecionarCrismando(crismando)
        assertEquals(crismando, viewModel.crismandoSelecionado.value)

        viewModel.selecionarCrismando(crismando)
        assertNull(viewModel.crismandoSelecionado.value)
    }

    @Test
    fun `registrarCrismando deve inferir genero caso nulo e vincular como vendedor e nas presencas`() = runTest {
        coEvery { crismandoDao.inserir(any()) } returns 101L

        val crismandoSemGenero = Crismando(
            crismandoId = 0,
            nome = "Beatriz Santos",
            genero = null
        )

        viewModel.registrarCrismando(crismandoSemGenero)

        val crismandoSlot = slot<Crismando>()
        coVerify(timeout = 3000) { crismandoDao.inserir(capture(crismandoSlot)) }
        assertEquals(Genero.FEMININO, crismandoSlot.captured.genero)

        coVerify(timeout = 3000) {
            vendedorDao.inserirVendedor(match {
                it.vendedorId == 101L && it.tipo == TipoVendedor.CRISMANDO
            })
        }

        val presencasSlot = slot<List<Presenca>>()
        coVerify(timeout = 3000) { presencaDao.gerarListaPresenca(capture(presencasSlot)) }
        assertEquals(2, presencasSlot.captured.size)
        assertTrue(presencasSlot.captured.all { !it.estaPresente && it.crismandoId == 101L })
    }

    @Test
    fun `alternarPresenca deve inverter estado booleano da presenca do dia`() = runTest {
        coEvery { presencaDao.buscarPresencaDoDiaPorCrismando(1L, "2026-09-06") } returns true

        viewModel.alternarPresenca(1L, "2026-09-06")

        coVerify(timeout = 3000) { presencaDao.atualizarPresenca(1L, "2026-09-06", false) }
    }

    @Test
    fun `gerarBlocosEmLote deve criar blocos com 10 rifas cada a partir do ultimo numero`() = runTest {
        coEvery { rifaDao.getMaiorNumeroRifa() } returns 20

        viewModel.gerarBlocosEmLote(2)

        val rifasSlot = slot<List<Rifa>>()
        coVerify(timeout = 3000) { rifaDao.inserirRifas(capture(rifasSlot)) }

        val rifasCriadas = rifasSlot.captured
        assertEquals(20, rifasCriadas.size)
        assertEquals(21, rifasCriadas.first().numero)
        assertEquals(3, rifasCriadas.first().bloco)
        assertEquals(40, rifasCriadas.last().numero)
        assertEquals(4, rifasCriadas.last().bloco)
    }

    @Test
    fun `excluirUltimosBlocos sem forcar deve abortar se houver rifas em uso`() = runTest {
        coEvery { rifaDao.contarRifasEmUsoNosUltimosBlocos(1) } returns 3
        var resultadoSucesso: Boolean? = null
        var totalEmUsoRetornado = -1
        val latch = CountDownLatch(1)

        viewModel.excluirUltimosBlocos(1, forcar = false) { sucesso, emUso ->
            resultadoSucesso = sucesso
            totalEmUsoRetornado = emUso
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { rifaDao.excluirUltimosBlocos(any()) }
        assertFalse(resultadoSucesso ?: true)
        assertEquals(3, totalEmUsoRetornado)
    }

    @Test
    fun `excluirUltimosBlocos forcado deve deletar mesmo com rifas em uso`() = runTest {
        coEvery { rifaDao.contarRifasEmUsoNosUltimosBlocos(1) } returns 3
        var resultadoSucesso: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.excluirUltimosBlocos(1, forcar = true) { sucesso, _ ->
            resultadoSucesso = sucesso
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(timeout = 3000, exactly = 1) { rifaDao.excluirUltimosBlocos(1) }
        assertTrue(resultadoSucesso ?: false)
    }

    @Test
    fun `excluirCrismando deve remover dependencias em rifas presencas vendedor e crismando`() = runTest {
        val crismandoId = 55L

        viewModel.excluirCrismando(crismandoId)

        coVerify(timeout = 3000) { rifaDao.desvincularRifasDoVendedor(crismandoId) }
        coVerify(timeout = 3000) { presencaDao.deletarPresencasPorCrismando(crismandoId) }
        coVerify(timeout = 3000) { vendedorDao.deletarVendedorPorId(crismandoId) }
        coVerify(timeout = 3000) { crismandoDao.deletarCrismando(crismandoId) }
    }

    @Test
    fun `exportarBackupCompletoCSV deve gerar estrutura valida com presencas e blocos de rifa`() = runTest {
        todosCrismandosFlow.value = listOf(
            Crismando(
                crismandoId = 1L,
                nome = "Mariana Silva",
                dataNascimento = "2010-04-12",
                telefone = "11988887777",
                nomeResponsavel = "Carlos Silva",
                telefoneResponsavel = "11999998888"
            )
        )
        diasComChamadaFlow.value = listOf("2026-09-06")
        todasRifasFlow.value = listOf(
            Rifa(numero = 1, bloco = 1, vendedorId = 1L, estaPaga = true),
            Rifa(numero = 11, bloco = 2, vendedorId = 1L, estaPaga = false)
        )
        every { presencaDao.buscarTodasAsPresencasStatic() } returns listOf(
            Presenca(crismandoId = 1L, data = "2026-09-06", estaPresente = true)
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val csv = viewModel.exportarBackupCompletoCSV()

        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("Nome,FotoUrl,DataNascimento,Telefone,NomeResponsavel,TelefoneResponsavel,BlocosRifa,06/09/26"))
        assertTrue(csv.contains("Mariana Silva,,2010-04-12,11988887777,Carlos Silva,11999998888,\"1;2\",O"))
    }
}