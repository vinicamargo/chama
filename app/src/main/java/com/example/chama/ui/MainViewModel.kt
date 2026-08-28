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
import com.example.chama.utils.GeneroUtils
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

    val dataDeHoje: LocalDate = if (BuildConfig.FLAVOR == "dev") {
        LocalDate.of(2026, 12, 2)
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
        val datasIso = diasComChamada.value.sorted()
        val datasFormatadas = datasIso.map { dataIso ->
            runCatching { LocalDate.parse(dataIso).format(formatter) }.getOrDefault(dataIso)
        }
        val todasPresencas = presencaDao.buscarTodasAsPresencasStatic()

        // Mapeamento rápido por (crismandoId, dataIso)
        val mapaPresencas = todasPresencas.associate { presenca ->
            Pair(presenca.crismandoId, presenca.data) to presenca.estaPresente
        }

        val csv = StringBuilder()
        csv.append("\uFEFF") // BOM UTF-8 para compatibilidade com Excel

        // 1. Cabeçalho com 6 colunas fixas + datas dos encontros
        val colunasCabecalho = listOf(
            "Nome",
            "FotoUrl",
            "DataNascimento",
            "Telefone",
            "NomeResponsavel",
            "TelefoneResponsavel"
        ) + datasFormatadas
        csv.append(colunasCabecalho.joinToString(",")).append("\n")

        crismandos.forEach { crismando ->
            val dadosCadastrais = listOf(
                crismando.nome,
                crismando.fotoUrl ?: "",
                crismando.dataNascimento ?: "",
                crismando.telefone ?: "",
                crismando.nomeResponsavel ?: "",
                crismando.telefoneResponsavel ?: ""
            )

            val statusPresencas = datasIso.map { dataStr ->
                val dataEncontro = runCatching { LocalDate.parse(dataStr) }.getOrNull()

                if (dataEncontro != null && dataEncontro <= dataDeHoje) {
                    val estaPresente = mapaPresencas[Pair(crismando.crismandoId, dataStr)] ?: false
                    if (estaPresente) "O" else "F"
                } else {
                    ""
                }
            }

            val linhaCompleta = (dadosCadastrais + statusPresencas).joinToString(",")
            csv.append(linhaCompleta).append("\n")
        }

        return csv.toString()
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

        // 1. Mapeamento de presenças por (crismandoId, dataIso)
        val mapaPresencas = todasPresencas.associate { presenca ->
            Pair(presenca.crismandoId, presenca.data) to presenca.estaPresente
        }

        // 2. Mapeamento de blocos de rifas por crismandoId
        val mapaBlocosPorCrismando = rifas
            .filter { it.vendedorId != null }
            .groupBy { it.vendedorId!! }
            .mapValues { (_, rifasDoVendedor) ->
                rifasDoVendedor.map { it.bloco }.distinct().sorted()
            }

        val csv = StringBuilder()
        csv.append("\uFEFF") // BOM UTF-8 para compatibilidade com Excel

        // 3. Cabeçalho Padronizado: 6 Cadastrais + Rifas + Datas
        val colunasCabecalho = listOf(
            "Nome",
            "FotoUrl",
            "DataNascimento",
            "Telefone",
            "NomeResponsavel",
            "TelefoneResponsavel",
            "BlocosRifa"
        ) + datasFormatadas
        csv.append(colunasCabecalho.joinToString(",")).append("\n")

        // 4. Linhas com dados consolidados
        crismandos.forEach { crismando ->
            val blocosDoCrismando = mapaBlocosPorCrismando[crismando.crismandoId] ?: emptyList()
            val textoBlocos = if (blocosDoCrismando.isNotEmpty()) {
                "\"${blocosDoCrismando.joinToString(";")}\"" // Ex: "1;2;3"
            } else {
                ""
            }

            val dadosCadastrais = listOf(
                crismando.nome,
                crismando.fotoUrl ?: "",
                crismando.dataNascimento ?: "",
                crismando.telefone ?: "",
                crismando.nomeResponsavel ?: "",
                crismando.telefoneResponsavel ?: "",
                textoBlocos
            )

            // Presenças padronizadas com 'O' ou 'F' até a data limite parametrizada
            val statusPresencas = datasIso.map { dataStr ->
                val dataEncontro = runCatching { LocalDate.parse(dataStr) }.getOrNull()

                if (dataEncontro != null && dataEncontro <= dataDeHoje) {
                    val estaPresente = mapaPresencas[Pair(crismando.crismandoId, dataStr)] ?: false
                    if (estaPresente) "O" else "F"
                } else {
                    "" // Datas futuras ficam vazias
                }
            }

            val linhaCompleta = (dadosCadastrais + statusPresencas).joinToString(",")
            csv.append(linhaCompleta).append("\n")
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
                    if (linhas.size <= 1) return@use

                    val cabecalho = parseCsvLine(linhas[0])
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")

                    // As datas iniciam na 8ª coluna (índice 7)
                    val datasLista = if (cabecalho.size > 7) {
                        cabecalho.drop(7).mapNotNull { dataStr ->
                            runCatching { LocalDate.parse(dataStr.trim(), formatter).toString() }.getOrNull()
                        }
                    } else {
                        emptyList()
                    }

                    // 1. Pré-leitura: Descobre o maior bloco citado no CSV
                    val linhasDados = linhas.drop(1).map { parseCsvLine(it) }
                    var maiorBloco = 0
                    linhasDados.forEach { colunas ->
                        val blocosTexto = colunas.getOrNull(6)?.trim() ?: ""
                        if (blocosTexto.isNotBlank()) {
                            val nums = blocosTexto.split(";", ",").mapNotNull { it.trim().toIntOrNull() }
                            val maxLinha = nums.maxOrNull() ?: 0
                            if (maxLinha > maiorBloco) {
                                maiorBloco = maxLinha
                            }
                        }
                    }

                    // 2. Limpa banco de dados anterior
                    limparDatabase()

                    // 3. Se houver blocos, gera todos de 1 até o maiorBloco
                    if (maiorBloco > 0) {
                        val totalRifas = maiorBloco * 10
                        val listaRifasIniciais = (1..totalRifas).map { numero ->
                            val numBloco = ((numero - 1) / 10) + 1
                            Rifa(
                                numero = numero,
                                bloco = numBloco,
                                estaPaga = false,
                                vendedorId = null
                            )
                        }
                        rifaDao.inserirRifas(listaRifasIniciais)
                    }

                    // 4. Cadastra crismandos, vincula blocos e registra presenças
                    linhasDados.forEach { colunas ->
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
                            telefoneResponsavel = telResp,
                            genero = GeneroUtils.inferirGenero(nome)
                        )

                        val novoId = crismandoDao.inserir(crismando)

                        vendedorDao.inserirVendedor(
                            Vendedor(vendedorId = novoId, tipo = TipoVendedor.CRISMANDO)
                        )

                        // Vincula os blocos do crismando (se preenchidos)
                        val blocosTexto = colunas.getOrNull(6)?.trim() ?: ""
                        if (blocosTexto.isNotBlank()) {
                            val blocosDoCrismando = blocosTexto
                                .split(";", ",")
                                .mapNotNull { it.trim().toIntOrNull() }

                            blocosDoCrismando.forEach { numBloco ->
                                rifaDao.vincularVendedorAoBloco(novoId, numBloco)
                            }
                        }

                        // Registra presenças das datas (iniciando no índice 7)
                        val presencasColunas = if (colunas.size > 7) colunas.drop(7) else emptyList()
                        val listaPresencas = datasLista.mapIndexed { index, dataIso ->
                            val valorBruto = presencasColunas.getOrNull(index)
                            val estaPresente = NormalizacaoUtils.normalizarPresenca(valorBruto)

                            Presenca(
                                crismandoId = novoId,
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