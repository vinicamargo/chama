package com.example.chama.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chama.BuildConfig
import com.example.chama.FiltroPresenca
import com.example.chama.data.entity.Crismando
import com.example.chama.data.dao.CrismandoDao
import com.example.chama.data.entity.Presenca
import com.example.chama.data.entity.Vendedor
import com.example.chama.utils.TipoVendedor
import com.example.chama.utils.removerAcentos
import com.example.chama.data.dao.PresencaDao
import com.example.chama.data.dao.RifaDao
import com.example.chama.data.dao.VendedorDao
import com.example.chama.data.entity.Rifa
import com.example.chama.data.model.PessoaVendedora
import com.example.chama.utils.FileUtils
import com.example.chama.utils.GeneroUtils
import com.example.chama.utils.NormalizacaoUtils
import com.example.chama.utils.ZipBackupUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.zip.ZipInputStream
import kotlin.random.Random

sealed interface StatusBlocoRifa {
    data object Vazio : StatusBlocoRifa
    data object Disponivel : StatusBlocoRifa
    data class Ocupado(val nomeResponsavel: String, val isMesmoCrismando: Boolean) : StatusBlocoRifa
    data class Inexistente(val ultimoBloco: Int) : StatusBlocoRifa
}

sealed interface VinculoBlocoResult {
    data object Sucesso : VinculoBlocoResult
    data class BlocoOcupado(val nomeResponsavel: String) : VinculoBlocoResult
    data class BlocoInexistente(val ultimoBloco: Int) : VinculoBlocoResult
    data class ErroGenerico(val mensagem: String) : VinculoBlocoResult
}

class MainViewModel(
    private val crismandoDao: CrismandoDao,
    private val presencaDao: PresencaDao,
    private val vendedorDao: VendedorDao,
    private val rifaDao: RifaDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val dataDeHoje: LocalDate = if (BuildConfig.DATA_CORTE_MOCK.isNotBlank()) {
        runCatching { LocalDate.parse(BuildConfig.DATA_CORTE_MOCK) }.getOrDefault(LocalDate.now())
    } else {
        LocalDate.now()
    }

    val diaSelecionado = MutableStateFlow("")

    val diasComChamada: StateFlow<List<String>> = presencaDao.buscarDiasComPresencas()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            diasComChamada.collect { dias ->
                if (dias.isNotEmpty()) {
                    val ultimoDomingo = dataDeHoje
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                        .toString()

                    val diasOrdenados = dias.sorted()

                    if (diaSelecionado.value.isBlank() || diaSelecionado.value !in dias) {
                        diaSelecionado.value = if (dias.contains(ultimoDomingo)) {
                            ultimoDomingo
                        } else {
                            diasOrdenados.first()
                        }
                    }
                }
            }
        }
    }

    var filtroNomeSelecionado = mutableStateOf("")
        private set

    var filtroPresencaSelecionado = mutableStateOf(FiltroPresenca.TODOS)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val presencasDoDia: StateFlow<List<Presenca>> = diaSelecionado
        .flatMapLatest { data ->
            presencaDao.buscarPresencasPorData(data)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaCrismandosOriginal: StateFlow<List<Crismando>> = crismandoDao.getAllCrismandos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val listaCrismandosFiltrada: StateFlow<List<Crismando>> = combine(
        listaCrismandosOriginal,
        snapshotFlow { filtroNomeSelecionado.value },
        snapshotFlow { filtroPresencaSelecionado.value },
        presencasDoDia
    ) { original, busca, filtro, presencas ->
        val porNome = if (busca.isBlank()) original else {
            original.filter {
                val nomeLimpo = it.nome.removerAcentos()
                val buscaLimpa = busca.removerAcentos()

                nomeLimpo.contains(buscaLimpa, ignoreCase = true)
            }
        }
        when (filtro) {
            FiltroPresenca.TODOS -> porNome
            FiltroPresenca.PRESENTES -> porNome.filter { c ->
                presencas.any { it.crismandoId == c.crismandoId && it.estaPresente }
            }
            FiltroPresenca.AUSENTES -> porNome.filter { c ->
                val p = presencas.find { it.crismandoId == c.crismandoId }
                p == null || !p.estaPresente
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var crismandoSelecionado = mutableStateOf<Crismando?>(null)
        private set

    val totalPresentes: StateFlow<Int> = combine(
        listaCrismandosOriginal,
        presencasDoDia
    ) { crismandos, presencas ->
        crismandos.count { c ->
            presencas.any { p -> p.crismandoId == c.crismandoId && p.estaPresente }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAusentes: StateFlow<Int> = combine(
        listaCrismandosOriginal,
        totalPresentes
    ) { todos, presentes ->
        todos.size - presentes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val listaVendedores: StateFlow<List<PessoaVendedora>> = combine(
        vendedorDao.getAllVendedores(),
        crismandoDao.getAllCrismandos()
    ) { vendedores, crismandos ->
        val listaV = vendedores.filter { it.tipo != TipoVendedor.CRISMANDO }.map {
            PessoaVendedora(it.vendedorId, it.nomeExterno ?: "Vendedor Externo", it.tipo)
        }
        val listaC = crismandos.map {
            PessoaVendedora(it.crismandoId, it.nome, TipoVendedor.CRISMANDO)
        }
        (listaV + listaC).sortedBy { it.nome }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaVendedoresFiltrados: StateFlow<List<PessoaVendedora>> = combine(
        listaVendedores,
        snapshotFlow { filtroNomeSelecionado.value }
    ) { vendedores, busca ->
        if (busca.isBlank()) {
            vendedores
        } else {
            val buscaLimpa = busca.removerAcentos()
            vendedores.filter { v ->
                v.nome.removerAcentos().contains(buscaLimpa, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mapaNomeVendedores: StateFlow<Map<Long, String>> = listaVendedores
        .map { lista ->
            lista.associateBy({ it.id }, { it.nome })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val listaRifas: StateFlow<List<Rifa>> = rifaDao.getRifas()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var rifaSelecionada = mutableStateOf<Rifa?>(null)
        private set

    fun registrarCrismando(crismando: Crismando) {
        viewModelScope.launch(ioDispatcher) {
            val crismandoComGenero = if (crismando.genero == null) {
                crismando.copy(genero = GeneroUtils.inferirGenero(crismando.nome))
            } else {
                crismando
            }

            val novoId = crismandoDao.inserir(crismandoComGenero)

            vendedorDao.inserirVendedor(
                Vendedor(
                    vendedorId = novoId,
                    tipo = TipoVendedor.CRISMANDO
                )
            )

            val todosDiasCrisma = diasComChamada.value
            val listaPresencaInicial = todosDiasCrisma.map { data ->
                Presenca(
                    crismandoId = novoId,
                    data = data,
                    estaPresente = false
                )
            }
            presencaDao.gerarListaPresenca(listaPresencaInicial)
        }
    }

    fun alterarFiltroNome(novoTexto: String) {
        filtroNomeSelecionado.value = novoTexto
        crismandoSelecionado.value = null
    }

    fun alterarFiltroPresenca(novoFiltro: FiltroPresenca) {
        filtroPresencaSelecionado.value = novoFiltro
    }

    fun alterarData(novaData: String) {
        diaSelecionado.value = novaData
    }

    fun selecionarCrismando(crismando: Crismando?) {
        crismandoSelecionado.value = if (crismandoSelecionado.value?.crismandoId == crismando?.crismandoId)
            null else crismando
    }

    fun alternarPresenca(id: Long, data: String) {
        viewModelScope.launch(ioDispatcher) {
            val isPresenteHoje = presencaDao.buscarPresencaDoDiaPorCrismando(id, data)
            presencaDao.atualizarPresenca(id, data, !isPresenteHoje)
        }
    }

    val todasPresencas: StateFlow<List<Presenca>> = presencaDao.buscarTodasAsPresencas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun obterTodasPresencasAtualizadas(): List<Presenca> {
        return withContext(ioDispatcher) {
            presencaDao.buscarTodasAsPresencasStatic()
        }
    }

    fun limparDatabase() {
        presencaDao.deleteAllPresencas()
        vendedorDao.deletarVendedoresCRISMANDO()
        crismandoDao.deleteAllCrismandos()
    }

    fun registrarVendedor(nome: String, tipoVendedor: TipoVendedor) {
        viewModelScope.launch(ioDispatcher) {
            val vendedor = Vendedor(
                vendedorId = Random.nextLong(1, Long.MAX_VALUE),
                tipo = tipoVendedor,
                nomeExterno = nome
            )
            vendedorDao.inserirVendedor(vendedor)
        }
    }

    fun selecionarRifa(rifa: Rifa?) {
        rifaSelecionada.value = if (rifaSelecionada.value?.numero == rifa?.numero)
            null else rifa
    }

    suspend fun checarStatusBloco(bloco: Int, crismandoIdAtual: Long): StatusBlocoRifa = withContext(ioDispatcher) {
        val ultimoBloco = rifaDao.getMaiorNumeroBloco()
        if (bloco > ultimoBloco || ultimoBloco == 0) {
            return@withContext StatusBlocoRifa.Inexistente(ultimoBloco)
        }

        val dono = rifaDao.buscarDonoDoBloco(bloco)
        return@withContext when {
            dono == null -> StatusBlocoRifa.Disponivel
            dono.vendedorId == crismandoIdAtual -> StatusBlocoRifa.Ocupado(dono.nomeVendedor, isMesmoCrismando = true)
            else -> StatusBlocoRifa.Ocupado(dono.nomeVendedor, isMesmoCrismando = false)
        }
    }

    fun vincularVendedorAoBloco(
        vendedorId: Long,
        bloco: Int,
        onResultado: (VinculoBlocoResult) -> Unit = {}
    ) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val ultimoBloco = rifaDao.getMaiorNumeroBloco()
                if (bloco > ultimoBloco || ultimoBloco == 0) {
                    withContext(Dispatchers.Main) { onResultado(VinculoBlocoResult.BlocoInexistente(ultimoBloco)) }
                    return@launch
                }

                val dono = rifaDao.buscarDonoDoBloco(bloco)
                when {
                    dono == null -> {
                        rifaDao.vincularVendedorAoBloco(vendedorId, bloco)
                        withContext(Dispatchers.Main) { onResultado(VinculoBlocoResult.Sucesso) }
                    }
                    dono.vendedorId == vendedorId -> {
                        withContext(Dispatchers.Main) { onResultado(VinculoBlocoResult.BlocoOcupado("este mesmo crismando")) }
                    }
                    else -> {
                        withContext(Dispatchers.Main) { onResultado(VinculoBlocoResult.BlocoOcupado(dono.nomeVendedor)) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResultado(VinculoBlocoResult.ErroGenerico(e.localizedMessage ?: "Erro ao vincular")) }
            }
        }
    }

    fun desvincularVendedorDoBloco(bloco: Int) {
        viewModelScope.launch(ioDispatcher) {
            rifaDao.desvincularVendedorDoBloco(bloco)
        }
    }

    fun alternarPagamentoRifa(rifa: Rifa) {
        viewModelScope.launch(ioDispatcher) {
            rifaDao.atualizarPagamentoBloco(rifa.bloco, !rifa.estaPaga)
        }
    }

    fun atualizarCrismando(crismando: Crismando) {
        viewModelScope.launch(ioDispatcher) {
            crismandoDao.atualizar(crismando)
        }
    }

    fun excluirCrismando(crismandoId: Long) {
        viewModelScope.launch(ioDispatcher) {
            rifaDao.desvincularRifasDoVendedor(crismandoId)
            presencaDao.deletarPresencasPorCrismando(crismandoId)
            vendedorDao.deletarVendedorPorId(crismandoId)
            crismandoDao.deletarCrismando(crismandoId)
        }
    }

    fun gerarBlocosEmLote(quantidadeBlocos: Int) {
        if (quantidadeBlocos <= 0) return
        viewModelScope.launch(ioDispatcher) {
            val ultimoNumero = rifaDao.getMaiorNumeroRifa()
            val totalRifasParaCriar = quantidadeBlocos * 10

            val novasRifas = (1..totalRifasParaCriar).map { offset ->
                val num = ultimoNumero + offset
                val blocoCalculado = ((num - 1) / 10) + 1

                Rifa(
                    numero = num,
                    bloco = blocoCalculado,
                    estaPaga = false,
                    vendedorId = null
                )
            }
            rifaDao.inserirRifas(novasRifas)
        }
    }

    fun excluirUltimosBlocos(
        quantidadeBlocos: Int,
        forcar: Boolean = false,
        onResultado: (sucesso: Boolean, emUso: Int) -> Unit = { _, _ -> }
    ) {
        if (quantidadeBlocos <= 0) return
        viewModelScope.launch(ioDispatcher) {
            val emUso = rifaDao.contarRifasEmUsoNosUltimosBlocos(quantidadeBlocos)

            if (emUso > 0 && !forcar) {
                withContext(Dispatchers.Main) {
                    onResultado(false, emUso)
                }
                return@launch
            }

            rifaDao.excluirUltimosBlocos(quantidadeBlocos)
            withContext(Dispatchers.Main) {
                onResultado(true, 0)
            }
        }
    }

    fun importarBackupZip(context: Context, uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            try {
                var arquivoCsv: File? = null
                val pastaFotosDestino = File(context.filesDir, "fotos_crismandos").apply { mkdirs() }
                val fotosRestauradas = mutableMapOf<String, String>()

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val nomeArquivo = File(entry.name).name
                            if (entry.name == "dados.csv" || entry.name.endsWith(".csv")) {
                                val tempCsv = File(context.cacheDir, "dados_temp_${System.currentTimeMillis()}.csv")
                                FileOutputStream(tempCsv).use { fos -> zis.copyTo(fos) }
                                arquivoCsv = tempCsv
                            } else if (entry.name.startsWith("fotos/") && !entry.isDirectory) {
                                val arquivoFoto = File(pastaFotosDestino, nomeArquivo)
                                FileOutputStream(arquivoFoto).use { fos -> zis.copyTo(fos) }
                                val chaveHash = nomeArquivo.substringBeforeLast(".").removePrefix("perfil_")
                                fotosRestauradas[chaveHash] = arquivoFoto.absolutePath
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                }

                if (arquivoCsv == null || !arquivoCsv.exists()) return@launch

                val linhas = arquivoCsv.readLines().filter { it.isNotBlank() }
                if (linhas.size <= 1) {
                    arquivoCsv.delete()
                    return@launch
                }

                val cabecalho = parseCsvLine(linhas[0])
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")

                // ⚠️ As datas começam após BlocosRifa (Índice 22)
                val indiceInicioDatas = 22
                val datasLista = if (cabecalho.size > indiceInicioDatas) {
                    cabecalho.drop(indiceInicioDatas).mapNotNull { dataStr ->
                        runCatching { LocalDate.parse(dataStr.trim(), formatter).toString() }.getOrNull()
                    }
                } else emptyList()

                val linhasDados = linhas.drop(1).map { parseCsvLine(it) }

                // BlocosRifa agora está no índice 21
                val maiorBloco = linhasDados.maxOfOrNull { colunas ->
                    val blocosTexto = colunas.getOrNull(21)?.trim() ?: ""
                    if (blocosTexto.isNotBlank()) {
                        blocosTexto.split(";", ",").mapNotNull { it.trim().toIntOrNull() }.maxOrNull() ?: 0
                    } else 0
                } ?: 0

                limparDatabase()

                if (maiorBloco > 0) {
                    val totalRifas = maiorBloco * 10
                    val listaRifasIniciais = (1..totalRifas).map { numero ->
                        Rifa(
                            numero = numero,
                            bloco = ((numero - 1) / 10) + 1,
                            estaPaga = false,
                            vendedorId = null
                        )
                    }
                    rifaDao.inserirRifas(listaRifasIniciais)
                }

                linhasDados.forEach { colunas ->
                    val nome = NormalizacaoUtils.normalizarNome(colunas.getOrNull(0))
                    if (nome.isBlank()) return@forEach

                    val fotoCsv = colunas.getOrNull(1)?.trim()?.ifBlank { null }
                    val dataNasc = NormalizacaoUtils.normalizarDataNascimento(colunas.getOrNull(2))
                    val cpf = colunas.getOrNull(3)?.filter { it.isDigit() }?.ifBlank { null }
                    val celular = NormalizacaoUtils.normalizarTelefone(colunas.getOrNull(4))

                    val cidNasc = colunas.getOrNull(5)?.trim()?.ifBlank { null }
                    val ufNasc = colunas.getOrNull(6)?.trim()?.ifBlank { null }
                    val paisNasc = colunas.getOrNull(7)?.trim()?.ifBlank { "Brasil" } ?: "Brasil"
                    val endereco = colunas.getOrNull(8)?.trim()?.ifBlank { null }
                    val cep = colunas.getOrNull(9)?.filter { it.isDigit() }?.ifBlank { null }
                    val cidAtual = colunas.getOrNull(10)?.trim()?.ifBlank { "Santo André" } ?: "Santo André"

                    val nomePai = NormalizacaoUtils.normalizarNome(colunas.getOrNull(11)).ifBlank { null }
                    val nomeMae = NormalizacaoUtils.normalizarNome(colunas.getOrNull(12)).ifBlank { null }
                    val relResp = colunas.getOrNull(13)?.trim()?.ifBlank { null }
                    val celResp = NormalizacaoUtils.normalizarTelefone(colunas.getOrNull(14))

                    // Sacramentos (Índices 15 a 20)
                    val isBatizadoStr = colunas.getOrNull(15)?.trim() ?: "S"
                    val isBatizado = isBatizadoStr.equals("S", ignoreCase = true) || isBatizadoStr == "1"

                    val batNaDioceseStr = colunas.getOrNull(16)?.trim() ?: "S"
                    val batizadoNaDiocese = batNaDioceseStr.equals("S", ignoreCase = true) || batNaDioceseStr == "1"

                    val paroquiaBatismo = colunas.getOrNull(17)?.trim()?.ifBlank { null }
                    val cidadeBatismo = colunas.getOrNull(18)?.trim()?.ifBlank { null }

                    val certidaoEntregueStr = colunas.getOrNull(19)?.trim() ?: "N"
                    val certidaoEntregue = certidaoEntregueStr.equals("S", ignoreCase = true) || certidaoEntregueStr == "1"

                    val temPrimeiraComunhaoStr = colunas.getOrNull(20)?.trim() ?: "S"
                    val temPrimeiraComunhao = temPrimeiraComunhaoStr.equals("S", ignoreCase = true) || temPrimeiraComunhaoStr == "1"

                    val chaveFoto = FileUtils.gerarChaveCrismando(nome, dataNasc)
                    val fotoFinal = fotosRestauradas[chaveFoto] ?: fotoCsv

                    val crismando = Crismando(
                        crismandoId = 0L,
                        nome = nome,
                        dataNascimento = dataNasc,
                        cpf = cpf,
                        celular = celular,
                        fotoUrl = fotoFinal,
                        genero = GeneroUtils.inferirGenero(nomeCompleto = nome),
                        cidadeNascimento = cidNasc,
                        estadoNascimento = ufNasc,
                        paisNascimento = paisNasc,
                        endereco = endereco,
                        cep = cep,
                        cidadeAtual = cidAtual,
                        nomePai = nomePai,
                        nomeMae = nomeMae,
                        relacionamentoResponsavel = relResp,
                        celularResponsavel = celResp,
                        isBatizado = isBatizado,
                        batizadoNaDiocese = batizadoNaDiocese,
                        paroquiaBatismo = paroquiaBatismo,
                        cidadeBatismo = cidadeBatismo,
                        certidaoBatismoEntregue = certidaoEntregue,
                        temPrimeiraComunhao = temPrimeiraComunhao
                    )

                    val novoId = crismandoDao.inserir(crismando)

                    vendedorDao.inserirVendedor(
                        Vendedor(vendedorId = novoId, tipo = TipoVendedor.CRISMANDO)
                    )

                    // Blocos vinculados (Índice 21)
                    val blocosTexto = colunas.getOrNull(21)?.trim() ?: ""
                    if (blocosTexto.isNotBlank()) {
                        val blocosDoCrismando = blocosTexto.split(";", ",").mapNotNull { it.trim().toIntOrNull() }
                        blocosDoCrismando.forEach { numBloco ->
                            rifaDao.vincularVendedorAoBloco(novoId, numBloco)
                        }
                    }

                    // Presenças (Índice 22 em diante)
                    val presencasColunas = if (colunas.size > indiceInicioDatas) colunas.drop(indiceInicioDatas) else emptyList()
                    val listaPresencas = datasLista.mapIndexed { i, dataIso ->
                        val valor = presencasColunas.getOrNull(i)?.trim() ?: ""
                        val presente = valor.equals("O", ignoreCase = true) || valor.equals("P", ignoreCase = true)

                        Presenca(
                            crismandoId = novoId,
                            data = dataIso,
                            estaPresente = presente
                        )
                    }

                    if (listaPresencas.isNotEmpty()) {
                        presencaDao.gerarListaPresenca(listaPresencas)
                    }
                }

                arquivoCsv.delete()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun exportarBackupCompletoZip(context: Context): File = withContext(ioDispatcher) {
        val dadosCsv = exportarBackupCompletoCSV()
        val crismandos = listaCrismandosOriginal.value

        val fotos = crismandos.map { crismando ->
            val chave = FileUtils.gerarChaveCrismando(crismando.nome, crismando.dataNascimento)
            Triple(chave, crismando.nome, crismando.fotoUrl)
        }

        ZipBackupUtils.criarZipBackup(
            context = context,
            conteudoCsv = dadosCsv,
            crismandosComFoto = fotos
        )
    }

    fun exportarBackupCompletoCSV(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")

        val crismandos = listaCrismandosOriginal.value
        val todasPresencas = presencaDao.buscarTodasAsPresencasStatic()
        val rifas = listaRifas.value
        val datasIso = diasComChamada.value.sorted()
        val datasFormatadas = datasIso.map { dataIso ->
            runCatching { LocalDate.parse(dataIso).format(formatter) }.getOrDefault(dataIso)
        }

        val mapaPresencas = todasPresencas.associate { presenca ->
            Pair(presenca.crismandoId, presenca.data) to presenca.estaPresente
        }

        val mapaBlocosPorCrismando = rifas
            .filter { it.vendedorId != null }
            .groupBy { it.vendedorId!! }
            .mapValues { (_, rifasDoVendedor) ->
                rifasDoVendedor.map { it.bloco }.distinct().sorted()
            }

        val csv = StringBuilder()
        csv.append("\uFEFF") // BOM UTF-8 para Excel

        // 22 colunas de metadados + datas dos encontros
        val colunasCabecalho = listOf(
            "Nome", "FotoUrl", "DataNascimento", "CPF", "Celular",
            "CidadeNascimento", "EstadoNascimento", "PaisNascimento",
            "Endereco", "CEP", "CidadeAtual", "NomePai", "NomeMae",
            "RelacionamentoResponsavel", "CelularResponsavel",
            "IsBatizado", "BatizadoNaDiocese", "ParoquiaBatismo", "CidadeBatismo",
            "CertidaoBatismoEntregue", "TemPrimeiraComunhao", "BlocosRifa"
        ) + datasFormatadas

        csv.append(colunasCabecalho.joinToString(",")).append("\n")

        crismandos.forEach { crismando ->
            val blocosDoCrismando = mapaBlocosPorCrismando[crismando.crismandoId] ?: emptyList()
            val textoBlocos = if (blocosDoCrismando.isNotEmpty()) {
                "\"${blocosDoCrismando.joinToString(";")}\""
            } else {
                ""
            }

            val dadosCadastrais = listOf(
                crismando.nome,
                crismando.fotoUrl ?: "",
                crismando.dataNascimento ?: "",
                crismando.cpf ?: "",
                crismando.celular ?: "",
                crismando.cidadeNascimento ?: "",
                crismando.estadoNascimento ?: "",
                crismando.paisNascimento ?: "Brasil",
                crismando.endereco ?: "",
                crismando.cep ?: "",
                crismando.cidadeAtual ?: "Santo André",
                crismando.nomePai ?: "",
                crismando.nomeMae ?: "",
                crismando.relacionamentoResponsavel ?: "",
                crismando.celularResponsavel ?: "",
                if (crismando.isBatizado) "S" else "N",
                if (crismando.batizadoNaDiocese) "S" else "N",
                crismando.paroquiaBatismo ?: "",
                crismando.cidadeBatismo ?: "",
                if (crismando.certidaoBatismoEntregue) "S" else "N",
                if (crismando.temPrimeiraComunhao) "S" else "N",
                textoBlocos
            )

            val statusPresencas = datasIso.map { dataStr ->
                val estaPresente = mapaPresencas[Pair(crismando.crismandoId, dataStr)] ?: false
                if (estaPresente) "O" else "F"
            }

            val linhaCompleta = (dadosCadastrais + statusPresencas).joinToString(",")
            csv.append(linhaCompleta).append("\n")
        }

        return csv.toString()
    }

    private fun parseCsvLine(linha: String): List<String> {
        val colunas = mutableListOf<String>()
        var dentroDeAspas = false
        val sb = StringBuilder()

        for (ch in linha) {
            when {
                ch == '\"' -> dentroDeAspas = !dentroDeAspas
                ch == ',' && !dentroDeAspas -> {
                    colunas.add(sb.toString().trim().removeSurrounding("\""))
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        colunas.add(sb.toString().trim().removeSurrounding("\""))
        return colunas
    }
}