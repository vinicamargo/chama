package com.example.chama.ui

import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    private val crismandoDao: CrismandoDao = mockk(relaxed = true)
    private val presencaDao: PresencaDao = mockk(relaxed = true)
    private val vendedorDao: VendedorDao = mockk(relaxed = true)
    private val rifaDao: RifaDao = mockk(relaxed = true)

    private val diasComChamadaFlow = MutableStateFlow(listOf("2026-09-20", "2026-09-27"))
    private val crismandosFlow = MutableStateFlow(emptyList<Crismando>())
    private val presencasPorDataFlow = MutableStateFlow(emptyList<Presenca>())
    private val vendedoresFlow = MutableStateFlow(emptyList<Vendedor>())
    private val rifasFlow = MutableStateFlow(emptyList<Rifa>())
    private val todasPresencasFlow = MutableStateFlow(emptyList<Presenca>())

    private lateinit var viewModel: MainViewModel

    private val crismandoMock1 = Crismando(crismandoId = 1L, nome = "Lucas Cavalcanti")
    private val crismandoMock2 = Crismando(crismandoId = 2L, nome = "Mariana Costa")

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        crismandosFlow.value = listOf(crismandoMock1, crismandoMock2)
        presencasPorDataFlow.value = listOf(
            Presenca(crismandoId = 1L, data = "2026-09-20", estaPresente = true),
            Presenca(crismandoId = 2L, data = "2026-09-20", estaPresente = false)
        )
        diasComChamadaFlow.value = listOf("2026-09-20", "2026-09-27")
        vendedoresFlow.value = emptyList()
        rifasFlow.value = emptyList()
        todasPresencasFlow.value = emptyList()

        coEvery { presencaDao.buscarDiasComPresencas() } returns diasComChamadaFlow
        coEvery { crismandoDao.getAllCrismandos() } returns crismandosFlow
        coEvery { presencaDao.buscarPresencasPorData(any()) } returns presencasPorDataFlow
        coEvery { vendedorDao.getAllVendedores() } returns vendedoresFlow
        coEvery { rifaDao.getRifas() } returns rifasFlow
        coEvery { presencaDao.buscarTodasAsPresencas() } returns todasPresencasFlow
        coEvery { presencaDao.buscarTodasAsPresencasStatic() } returns emptyList()

        viewModel = MainViewModel(crismandoDao, presencaDao, vendedorDao, rifaDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.subscribeToFlows() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listaCrismandosFiltrada.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.presencasDoDia.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.totalPresentes.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.totalAusentes.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listaVendedores.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.listaVendedoresFiltrados.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.mapaNomeVendedores.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.todasPresencas.collect {}
        }
    }

    @Test
    fun testInicializacaoDeDiasESelecaoDefault() = runTest {
        subscribeToFlows()
        advanceUntilIdle()

        assertTrue(viewModel.diaSelecionado.value.isNotBlank())
        assertEquals(2, viewModel.diasComChamada.value.size)
    }

    @Test
    fun testInit_comDiasSemUltimoDomingo_selecionaPrimeiroDia() = runTest {
        val mockPresencaDao = mockk<PresencaDao>(relaxed = true)
        coEvery { mockPresencaDao.buscarDiasComPresencas() } returns flowOf(listOf("2025-01-01", "2025-01-08"))
        coEvery { mockPresencaDao.buscarPresencasPorData(any()) } returns flowOf(emptyList())

        val localViewModel = MainViewModel(crismandoDao, mockPresencaDao, vendedorDao, rifaDao, testDispatcher)
        advanceUntilIdle()

        assertEquals("2025-01-01", localViewModel.diaSelecionado.value)
    }

    @Test
    fun testInit_comDiasVazios_diaSelecionadoPermaneceVazio() = runTest {
        val mockPresencaDao = mockk<PresencaDao>(relaxed = true)
        coEvery { mockPresencaDao.buscarDiasComPresencas() } returns flowOf(emptyList())
        coEvery { mockPresencaDao.buscarPresencasPorData(any()) } returns flowOf(emptyList())

        val localViewModel = MainViewModel(crismandoDao, mockPresencaDao, vendedorDao, rifaDao, testDispatcher)
        advanceUntilIdle()

        assertEquals("", localViewModel.diaSelecionado.value)
    }

    @Test
    fun testFiltroPorNomeComAcentos() = runTest {
        subscribeToFlows()
        advanceUntilIdle()

        viewModel.alterarFiltroNome("lucas")
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        assertEquals(1, viewModel.listaCrismandosFiltrada.value.size)
        assertEquals("Lucas Cavalcanti", viewModel.listaCrismandosFiltrada.value.first().nome)

        viewModel.alterarFiltroNome("")
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        assertEquals(2, viewModel.listaCrismandosFiltrada.value.size)
    }

    @Test
    fun testAlterarFiltroNome_resetaCrismandoSelecionado() = runTest {
        subscribeToFlows()
        viewModel.selecionarCrismando(crismandoMock1)
        assertEquals(crismandoMock1, viewModel.crismandoSelecionado.value)

        viewModel.alterarFiltroNome("Mariana")
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        assertNull(viewModel.crismandoSelecionado.value)
        assertEquals("Mariana", viewModel.filtroNomeSelecionado.value)
    }

    @Test
    fun testFiltroPresencaPresentesEAusentes() = runTest {
        subscribeToFlows()
        advanceUntilIdle()

        viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        assertEquals(1, viewModel.listaCrismandosFiltrada.value.size)
        assertEquals(1L, viewModel.listaCrismandosFiltrada.value.first().crismandoId)

        viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        assertEquals(1, viewModel.listaCrismandosFiltrada.value.size)
        assertEquals(2L, viewModel.listaCrismandosFiltrada.value.first().crismandoId)

        viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        assertEquals(2, viewModel.listaCrismandosFiltrada.value.size)
    }

    @Test
    fun testSelecionarCrismando_alternaSelecao() {
        assertNull(viewModel.crismandoSelecionado.value)

        viewModel.selecionarCrismando(crismandoMock1)
        assertEquals(crismandoMock1, viewModel.crismandoSelecionado.value)

        viewModel.selecionarCrismando(crismandoMock1)
        assertNull(viewModel.crismandoSelecionado.value)

        viewModel.selecionarCrismando(crismandoMock2)
        assertEquals(crismandoMock2, viewModel.crismandoSelecionado.value)

        viewModel.selecionarCrismando(null)
        assertNull(viewModel.crismandoSelecionado.value)
    }

    @Test
    fun testTotalPresentesEAusentes() = runTest {
        subscribeToFlows()
        advanceUntilIdle()

        assertEquals(1, viewModel.totalPresentes.value)
        assertEquals(1, viewModel.totalAusentes.value)
    }

    @Test
    fun testListaVendedoresEFiltragem() = runTest {
        val vendedorExterno = Vendedor(vendedorId = 10L, tipo = TipoVendedor.EXTERNO, nomeExterno = "Pedro Rocha")
        val colaborador = Vendedor(vendedorId = 11L, tipo = TipoVendedor.COLABORADOR, nomeExterno = null)

        vendedoresFlow.value = listOf(vendedorExterno, colaborador)

        subscribeToFlows()
        advanceUntilIdle()

        val lista = viewModel.listaVendedores.value
        assertEquals(4, lista.size)

        val mapa = viewModel.mapaNomeVendedores.value
        assertEquals("Pedro Rocha", mapa[10L])
        assertEquals("Vendedor Externo", mapa[11L])
        assertEquals("Lucas Cavalcanti", mapa[1L])

        viewModel.alterarFiltroNome("pedro")
        Snapshot.sendApplyNotifications()
        advanceUntilIdle()

        val filtrados = viewModel.listaVendedoresFiltrados.value
        assertEquals(1, filtrados.size)
        assertEquals("Pedro Rocha", filtrados.first().nome)
    }

    @Test
    fun testListaRifasESelecaoRifa() = runTest {
        val rifa1 = Rifa(numero = 1, bloco = 1)
        val rifa2 = Rifa(numero = 2, bloco = 1)

        rifasFlow.value = listOf(rifa1, rifa2)

        subscribeToFlows()
        advanceUntilIdle()

        assertEquals(2, viewModel.listaRifas.value.size)
        assertNull(viewModel.rifaSelecionada.value)

        viewModel.selecionarRifa(rifa1)
        assertEquals(rifa1, viewModel.rifaSelecionada.value)

        viewModel.selecionarRifa(rifa1)
        assertNull(viewModel.rifaSelecionada.value)

        viewModel.selecionarRifa(rifa2)
        assertEquals(rifa2, viewModel.rifaSelecionada.value)
    }

    @Test
    fun testRegistrarCrismando_semGeneroInformaGeneroEIniciaPresencas() = runTest {
        coEvery { crismandoDao.inserir(any()) } returns 100L

        val novo = Crismando(nome = "Fernanda Silva", genero = null)
        viewModel.registrarCrismando(novo)
        advanceUntilIdle()

        coVerify(timeout = 2000) {
            crismandoDao.inserir(match { it.genero == Genero.FEMININO })
        }
        coVerify(timeout = 2000) {
            vendedorDao.inserirVendedor(Vendedor(vendedorId = 100L, tipo = TipoVendedor.CRISMANDO))
        }
        coVerify(timeout = 2000) {
            presencaDao.gerarListaPresenca(match {
                it.size == 2 && it.all { p -> p.crismandoId == 100L && !p.estaPresente }
            })
        }
    }

    @Test
    fun testRegistrarCrismando_comGeneroMantemGenero() = runTest {
        coEvery { crismandoDao.inserir(any()) } returns 101L

        val novo = Crismando(nome = "Ariel", genero = Genero.MASCULINO)
        viewModel.registrarCrismando(novo)
        advanceUntilIdle()

        coVerify(timeout = 2000) {
            crismandoDao.inserir(match { it.genero == Genero.MASCULINO })
        }
    }

    @Test
    fun testAlterarData() {
        viewModel.alterarData("2026-10-04")
        assertEquals("2026-10-04", viewModel.diaSelecionado.value)
    }

    @Test
    fun testAlternarPresenca_quandoPresente_mudaParaAusente() = runTest {
        coEvery { presencaDao.buscarPresencaDoDiaPorCrismando(1L, "2026-09-20") } returns true

        viewModel.alternarPresenca(1L, "2026-09-20")
        advanceUntilIdle()

        coVerify(timeout = 2000) { presencaDao.atualizarPresenca(1L, "2026-09-20", false) }
    }

    @Test
    fun testAlternarPresenca_quandoAusente_mudaParaPresente() = runTest {
        coEvery { presencaDao.buscarPresencaDoDiaPorCrismando(2L, "2026-09-20") } returns false

        viewModel.alternarPresenca(2L, "2026-09-20")
        advanceUntilIdle()

        coVerify(timeout = 2000) { presencaDao.atualizarPresenca(2L, "2026-09-20", true) }
    }

    @Test
    fun testObterTodasPresencasAtualizadas() = runTest {
        val presencas = listOf(Presenca(crismandoId = 1L, data = "2026-09-20", estaPresente = true))
        coEvery { presencaDao.buscarTodasAsPresencasStatic() } returns presencas

        val resultado = viewModel.obterTodasPresencasAtualizadas()
        assertEquals(1, resultado.size)
        assertEquals(1L, resultado.first().crismandoId)
    }

    @Test
    fun testLimparDatabase() {
        viewModel.limparDatabase()

        coVerify { presencaDao.deleteAllPresencas() }
        coVerify { vendedorDao.deletarVendedoresCRISMANDO() }
        coVerify { crismandoDao.deleteAllCrismandos() }
    }

    @Test
    fun testRegistrarVendedor() = runTest {
        viewModel.registrarVendedor("João Paulo", TipoVendedor.EXTERNO)
        advanceUntilIdle()

        coVerify(timeout = 2000) {
            vendedorDao.inserirVendedor(match {
                it.nomeExterno == "João Paulo" && it.tipo == TipoVendedor.EXTERNO
            })
        }
    }

    @Test
    fun testVincularEDesvincularVendedorDoBloco() = runTest {
        coEvery { rifaDao.getMaiorNumeroBloco() } returns 10
        coEvery { rifaDao.buscarDonoDoBloco(3) } returns null

        viewModel.vincularVendedorAoBloco(10L, 3)
        advanceUntilIdle()
        coVerify(timeout = 2000) { rifaDao.vincularVendedorAoBloco(10L, 3) }

        viewModel.desvincularVendedorDoBloco(3)
        advanceUntilIdle()
        coVerify(timeout = 2000) { rifaDao.desvincularVendedorDoBloco(3) }
    }

    @Test
    fun testAlternarPagamentoRifa() = runTest {
        val rifa = Rifa(numero = 1, bloco = 1, estaPaga = false)
        viewModel.alternarPagamentoRifa(rifa)
        advanceUntilIdle()

        coVerify(timeout = 2000) { rifaDao.atualizarPagamentoBloco(1, true) }
    }

    @Test
    fun testAtualizarCrismando() = runTest {
        viewModel.atualizarCrismando(crismandoMock1)
        advanceUntilIdle()

        coVerify(timeout = 2000) { crismandoDao.atualizar(crismandoMock1) }
    }

    @Test
    fun testExcluirCrismando() = runTest {
        viewModel.excluirCrismando(1L)
        advanceUntilIdle()

        coVerify(timeout = 2000) { rifaDao.desvincularRifasDoVendedor(1L) }
        coVerify(timeout = 2000) { presencaDao.deletarPresencasPorCrismando(1L) }
        coVerify(timeout = 2000) { vendedorDao.deletarVendedorPorId(1L) }
        coVerify(timeout = 2000) { crismandoDao.deletarCrismando(1L) }
    }

    @Test
    fun testGerarBlocosEmLote_quantidadeInvalida_naoFazNada() = runTest {
        viewModel.gerarBlocosEmLote(0)
        viewModel.gerarBlocosEmLote(-5)
        advanceUntilIdle()

        coVerify(exactly = 0) { rifaDao.getMaiorNumeroRifa() }
    }

    @Test
    fun testGerarBlocosEmLote_quantidadeValida_criaRifas() = runTest {
        coEvery { rifaDao.getMaiorNumeroRifa() } returns 10

        viewModel.gerarBlocosEmLote(2)
        advanceUntilIdle()

        coVerify(timeout = 2000) {
            rifaDao.inserirRifas(match { rifas ->
                rifas.size == 20 &&
                        rifas.first().numero == 11 &&
                        rifas.first().bloco == 2 &&
                        rifas.last().numero == 30 &&
                        rifas.last().bloco == 3
            })
        }
    }

    @Test
    fun testExcluirUltimosBlocos_quantidadeInvalida_naoFazNada() = runTest {
        viewModel.excluirUltimosBlocos(0)
        advanceUntilIdle()

        coVerify(exactly = 0) { rifaDao.contarRifasEmUsoNosUltimosBlocos(any()) }
    }

    @Test
    fun testExcluirUltimosBlocos_emUsoSemForcar_retornaFalha() = runTest {
        coEvery { rifaDao.contarRifasEmUsoNosUltimosBlocos(2) } returns 3

        var sucesso: Boolean? = null
        var emUso: Int? = null

        viewModel.excluirUltimosBlocos(2, forcar = false) { s, u ->
            sucesso = s
            emUso = u
        }

        advanceUntilIdle()

        coVerify {
            rifaDao.contarRifasEmUsoNosUltimosBlocos(2)
        }

        assertEquals(false, sucesso)
        assertEquals(3, emUso)
        coVerify(exactly = 0) { rifaDao.excluirUltimosBlocos(any()) }
    }

    @Test
    fun testExcluirUltimosBlocos_emUsoComForcar_retornaSucesso() = runTest {
        coEvery { rifaDao.contarRifasEmUsoNosUltimosBlocos(2) } returns 3

        var sucesso: Boolean? = null

        viewModel.excluirUltimosBlocos(2, forcar = true) { s, _ ->
            sucesso = s
        }

        advanceUntilIdle()

        coVerify { rifaDao.excluirUltimosBlocos(2) }

        assertEquals(true, sucesso)
    }

    @Test
    fun testExcluirUltimosBlocos_semUso_retornaSucesso() = runTest {
        coEvery { rifaDao.contarRifasEmUsoNosUltimosBlocos(2) } returns 0

        var sucesso: Boolean? = null

        viewModel.excluirUltimosBlocos(2) { s, _ ->
            sucesso = s
        }

        advanceUntilIdle()

        coVerify { rifaDao.excluirUltimosBlocos(2) }

        assertEquals(true, sucesso)
    }

    @Test
    fun testExportarBackupCompletoCSV() = runTest {
        coEvery { presencaDao.buscarTodasAsPresencasStatic() } returns listOf(
            Presenca(crismandoId = 1L, data = "2026-09-20", estaPresente = true),
            Presenca(crismandoId = 2L, data = "2026-09-20", estaPresente = false)
        )
        rifasFlow.value = listOf(Rifa(numero = 1, bloco = 1, vendedorId = 1L))

        subscribeToFlows()
        advanceUntilIdle()

        val csv = viewModel.exportarBackupCompletoCSV()

        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("Lucas Cavalcanti"))
        assertTrue(csv.contains("Mariana Costa"))
        assertTrue(csv.contains("\"1\""))
    }

    @Test
    fun testExportarBackupCompletoZip() = runTest {
        subscribeToFlows()
        advanceUntilIdle()

        val mockContext = mockk<Context>(relaxed = true)
        val tempDir = Files.createTempDirectory("test_zip").toFile()
        every { mockContext.cacheDir } returns tempDir

        val zip = viewModel.exportarBackupCompletoZip(mockContext)

        assertTrue(zip.exists())
        assertEquals("backup_geral_chama.zip", zip.name)
    }

    @Test
    fun testImportarBackupZip_comSucesso() = runTest {
        val tempDir = Files.createTempDirectory("test_import").toFile()
        val zipFile = File(tempDir, "backup.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("dados.csv"))
            val csvContent = "Nome,FotoUrl,DataNascimento,Telefone,NomeResponsavel,TelefoneResponsavel,IsBatizado,CertidaoBatismoEntregue,ParoquiaBatismo,TemPrimeiraComunhao,BlocosRifa,20/09/26\n" +
                    "Lucas Cavalcanti,,2000-01-01,11999999999,,,,,,,1;2,O\n"
            zos.write(csvContent.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val mockContext = mockk<Context>(relaxed = true)
        val mockUri = mockk<Uri>()
        every { mockContext.cacheDir } returns tempDir
        every { mockContext.contentResolver.openInputStream(mockUri) } answers { FileInputStream(zipFile) }

        coEvery { crismandoDao.inserir(any()) } returns 50L

        viewModel.importarBackupZip(mockContext, mockUri)
        advanceUntilIdle()

        coVerify(timeout = 2000) { presencaDao.deleteAllPresencas() }
        coVerify(timeout = 2000) { crismandoDao.inserir(match { it.nome == "Lucas Cavalcanti" }) }
        coVerify(timeout = 2000) { vendedorDao.inserirVendedor(Vendedor(vendedorId = 50L, tipo = TipoVendedor.CRISMANDO)) }
        coVerify(timeout = 2000) { rifaDao.vincularVendedorAoBloco(50L, 1) }
        coVerify(timeout = 2000) { rifaDao.vincularVendedorAoBloco(50L, 2) }
        coVerify(timeout = 2000) { presencaDao.gerarListaPresenca(match { it.first().estaPresente }) }
    }

    @Test
    fun testImportarBackupZip_comCsvVazioOuInexistente_naoProcessa() = runTest {
        val tempDir = Files.createTempDirectory("test_import_empty").toFile()
        val zipFile = File(tempDir, "backup_empty.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("outro.txt"))
            zos.write("conteudo".toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val mockContext = mockk<Context>(relaxed = true)
        val mockUri = mockk<Uri>()
        every { mockContext.cacheDir } returns tempDir
        every { mockContext.contentResolver.openInputStream(mockUri) } answers { FileInputStream(zipFile) }

        viewModel.importarBackupZip(mockContext, mockUri)
        advanceUntilIdle()

        coVerify(exactly = 0) { crismandoDao.inserir(any()) }
    }
}