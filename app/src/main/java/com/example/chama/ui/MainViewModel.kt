package com.example.chama

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chama.data.Crismando
import com.example.chama.data.CrismandoDao
import com.example.chama.data.Presenca
import com.example.data.PresencaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.reflect.typeOf

class MainViewModel(
    private val crismandoDao: CrismandoDao,
    private val presencaDao: PresencaDao
) : ViewModel() {
    val proximoDomingo = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    val dataSelecionada = MutableStateFlow(proximoDomingo.toString())

    val diasComPresencas: StateFlow<List<String>> = presencaDao.buscarDiasComPresencas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaCrismandosOriginal: StateFlow<List<Crismando>> = crismandoDao.getAllCrismandos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var filtroPresencaAtual = mutableStateOf(FiltroPresenca.TODOS)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val presencasDoDia: StateFlow<List<Presenca>> = dataSelecionada
        .flatMapLatest { data ->
            presencaDao.buscarPresencasPorData(data)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun String.removerAcentos(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return temp.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    val listaCrismandosFiltrada: StateFlow<List<Crismando>> = combine(
        listaCrismandosOriginal,
        snapshotFlow { textoPesquisa.value },
        snapshotFlow { filtroPresencaAtual.value },
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

    var textoPesquisa = mutableStateOf("")
        private set

    var crismandoSelecionado = mutableStateOf<Crismando?>(null)
        private set

    fun alterarFiltroPresenca(novoFiltro: FiltroPresenca) {
        filtroPresencaAtual.value = novoFiltro
    }

    init {
        verificarEGerarPresencasDeDomingo()
    }

    private fun verificarEGerarPresencasDeDomingo() {
       viewModelScope.launch(Dispatchers.IO) {
            val listaCrismandos: List<Crismando> = crismandoDao.getAllCrismandosStatic()

            val novasPresencas = listaCrismandos.map {
                Presenca(crismandoId = it.crismandoId, data = proximoDomingo.toString(), estaPresente = false)
            }
            presencaDao.gerarListaPresenca(novasPresencas)
        }
    }
    fun selecionar(crismando: Crismando?) {
        crismandoSelecionado.value = if (crismandoSelecionado.value?.crismandoId == crismando?.crismandoId)
            null else crismando
    }

    fun onDigitacao(novoTexto: String) {
        textoPesquisa.value = novoTexto
        crismandoSelecionado.value = null
    }

    fun alternarPresenca(id: Long, data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isPresenteHoje = presencaDao.buscarPresencaDoDiaPorCrismando(id, data)
            presencaDao.atualizarPresenca(id, data, !isPresenteHoje)
        }
    }

    // Função que seu Spinner/Dropdown vai chamar
    fun atualizarData(novaData: String) {
        dataSelecionada.value = novaData
    }
}