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
import com.example.chama.utils.removerAcentos
import com.example.data.PresencaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class MainViewModel(
    private val crismandoDao: CrismandoDao,
    private val presencaDao: PresencaDao
) : ViewModel() {
    val diaSelecionado = MutableStateFlow(LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toString())
    val diasComChamada: StateFlow<List<String>> = presencaDao.buscarDiasComPresencas()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


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
    ){
        crismandos, presencas ->
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

    fun exportarParaCSV(): String {
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

                if(LocalDate.parse(data) <= LocalDate.now()){
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

    fun limparDatabase(){
        presencaDao.deleteAllPresencas()
        crismandoDao.deleteAllCrismandos()
    }

    fun importarDadosCsv(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")

                    val reader = inputStream.bufferedReader()
                    val linhas = reader.readLines()

                    val datasBruta = linhas[0].split(",", limit = 2)[1].split(",")
                    val datasLista = datasBruta.map { dataString ->
                        LocalDate.parse(dataString, formatter).toString()
                    }

                    limparDatabase()

                    linhas.drop(1).forEach { linha ->
                        val colunas = linha.split(",", limit = 2)

                        val nome = colunas[0]
                        val presencasBruta = colunas.getOrNull(1)?.split(",") ?: emptyList()

                        val presencasLista = presencasBruta.map {
                            it.trim() == "O"
                        }

                        val presencas = mutableListOf<Presenca>()
                        val crismando = Crismando(nome = nome)

                        crismandoDao.inserir(crismando)

                        for (i in datasLista.indices){
                            presencas.add(
                                Presenca(
                                crismandoId = crismando.crismandoId,
                                datasLista[i],
                                presencasLista[i]
                                )
                            )
                        }

                        presencaDao.gerarListaPresenca(presencas)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}