package com.example.chama.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.chama.utils.NormalizacaoUtils
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

class MainViewModel(
    private val crismandoDao: CrismandoDao,
    private val presencaDao: PresencaDao,
    private val vendedorDao: VendedorDao,
    private val rifaDao: RifaDao
) : ViewModel() {

    val dataDeHoje = LocalDate.of(2026, 11, 27)

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
        viewModelScope.launch(Dispatchers.IO) {
            val novoId = crismandoDao.inserir(crismando)

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
        viewModelScope.launch(Dispatchers.IO) {
            val isPresenteHoje = presencaDao.buscarPresencaDoDiaPorCrismando(id, data)
            presencaDao.atualizarPresenca(id, data, !isPresenteHoje)
        }
    }

    val todasPresencas: StateFlow<List<Presenca>> = presencaDao.buscarTodasAsPresencas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun exportarPresencasCSV(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")

        val crismandos = listaCrismandosOriginal.value
        val datas = diasComChamada.value.sorted()
        val datasFormatadas = datas.map { LocalDate.parse(it).format(formatter) }
        val todasPresencas = presencaDao.buscarTodasAsPresencasStatic()

        val csv = StringBuilder()

        csv.append("\uFEFF")

        csv.append("Nome")
        datasFormatadas.forEach { data -> csv.append(",$data") }
        csv.append("\n")

        crismandos.forEach { crismando ->
            csv.append(crismando.nome)

            datas.forEach { data ->
                var status = ""

                if (LocalDate.parse(data) <= LocalDate.now()) {
                    val registro = todasPresencas.find {
                        it.crismandoId == crismando.crismandoId && it.data == data
                    }

                    status = if (registro?.estaPresente == true) "O" else "F"
                }
                csv.append(",$status")
            }
            csv.append("\n")
        }

        return csv.toString()
    }

    fun limparDatabase() {
        presencaDao.deleteAllPresencas()
        vendedorDao.deletarVendedoresCRISMANDO()
        crismandoDao.deleteAllCrismandos()
    }

    fun importarDadosCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = inputStream.bufferedReader()
                    val linhas = reader.readLines().filter { it.isNotBlank() }
                    if (linhas.isEmpty()) return@use

                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")

                    val cabecalhoColunas = linhas[0].split(",")
                    if (cabecalhoColunas.size <= 6) return@use

                    val datasLista = cabecalhoColunas.drop(6).mapNotNull { dataStr ->
                        runCatching { LocalDate.parse(dataStr.trim(), formatter).toString() }.getOrNull()
                    }

                    if (datasLista.isEmpty()) return@use

                    limparDatabase()

                    linhas.drop(1).forEach { linha ->
                        val colunas = linha.split(",")
                        val nome = NormalizacaoUtils.normalizarNome(colunas.getOrNull(0))
                        if (nome.isBlank()) return@forEach

                        val fotoUrl = colunas.getOrNull(1)?.trim()?.ifBlank { null }
                        val dataNasc = NormalizacaoUtils.normalizarDataNascimento(colunas.getOrNull(2))
                        val tel = NormalizacaoUtils.normalizarTelefone(colunas.getOrNull(3))
                        val nomeResp = NormalizacaoUtils.normalizarNome(colunas.getOrNull(4)).ifBlank { null }
                        val telResp = NormalizacaoUtils.normalizarTelefone(colunas.getOrNull(5))

                        val crismando = Crismando(
                            nome = nome,
                            fotoUrl = fotoUrl,
                            dataNascimento = dataNasc,
                            telefone = tel,
                            nomeResponsavel = nomeResp,
                            telefoneResponsavel = telResp
                        )

                        val crismandoId = crismandoDao.inserir(crismando)

                        vendedorDao.inserirVendedor(
                            Vendedor(vendedorId = crismandoId, tipo = TipoVendedor.CRISMANDO)
                        )

                        // Coleta as presenças marcadas para cada domingo (ou seta false se vazio)
                        val presencasColunas = if (colunas.size > 6) colunas.drop(6) else emptyList()

                        val listaPresencas = datasLista.mapIndexed { index, dataIso ->
                            val valor = presencasColunas.getOrNull(index)?.trim()?.uppercase()
                            val estaPresente = valor in setOf("O", "P", "1", "TRUE", "SIM", "X", "PRESENTE")

                            Presenca(
                                crismandoId = crismandoId,
                                data = dataIso,
                                estaPresente = estaPresente
                            )
                        }

                        if (listaPresencas.isNotEmpty()) {
                            presencaDao.gerarListaPresenca(listaPresencas)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun registrarVendedor(nome: String, tipoVendedor: TipoVendedor) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun vincularVendedorAoBloco(vendedorId: Long, bloco: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            rifaDao.vincularVendedorAoBloco(vendedorId, bloco)
        }
    }

    fun desvincularVendedorDoBloco(bloco: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            rifaDao.desvincularVendedorDoBloco(bloco)
        }
    }

    fun alternarPagamentoRifa(rifa: Rifa) {
        viewModelScope.launch(Dispatchers.IO) {
            rifaDao.atualizarPagamentoBloco(rifa.bloco, !rifa.estaPaga)
        }
    }

    fun exportarRifasCSV(): String {
        val rifas = listaRifas.value
        val nomesVendedores = mapaNomeVendedores.value

        val blocosAgrupados = rifas.groupBy { it.bloco }

        val csv = StringBuilder()
        csv.append("\uFEFF")
        csv.append("Bloco, Range Rifas, Vendedor, Status Pagamento\n")

        blocosAgrupados.toSortedMap().forEach { (numBloco, rifasDoBloco) ->
            val primeiro = rifasDoBloco.first()
            val rangeRifas = "${primeiro.numero}-${primeiro.numero + 9}"
            val nome = nomesVendedores[primeiro.vendedorId] ?: "Sem vendedor"
            val statusPgto = if (primeiro.estaPaga) "Pago" else "Pendente"
            csv.append("$numBloco,$rangeRifas,$nome,$statusPgto\n")
        }

        return csv.toString()
    }

    fun atualizarCrismando(crismando: Crismando) {
        viewModelScope.launch(Dispatchers.IO) {
            crismandoDao.atualizar(crismando)
        }
    }

    fun excluirCrismando(crismandoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            rifaDao.desvincularRifasDoVendedor(crismandoId)
            presencaDao.deletarPresencasPorCrismando(crismandoId)
            vendedorDao.deletarVendedorPorId(crismandoId)
            crismandoDao.deletarCrismando(crismandoId)
        }
    }

    fun gerarBlocosEmLote(quantidadeBlocos: Int) {
        if (quantidadeBlocos <= 0) return
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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
}