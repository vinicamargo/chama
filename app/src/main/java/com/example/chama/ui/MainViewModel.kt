package com.example.chama

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chama.data.Crismando
import com.example.chama.data.CrismandoDao
import com.example.chama.data.Presenca
import com.example.data.PresencaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class MainViewModel(
    private val crismandoDao: CrismandoDao,
    private val presencaDao: PresencaDao
) : ViewModel() {

    private val dataHoje = LocalDate.now().toString()

    val presencasDoDia = presencaDao.buscarPresencasDoDia(dataHoje)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaCrismandosOriginal: StateFlow<List<Crismando>> = crismandoDao.getAllCrismandos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var filtroPresencaAtual = mutableStateOf(FiltroPresenca.TODOS)
        private set

    val listaCrismandosFiltrada: StateFlow<List<Crismando>> = combine(
        listaCrismandosOriginal,
        snapshotFlow { textoPesquisa.value },
        snapshotFlow { filtroPresencaAtual.value },
        presencasDoDia
    ) { original, busca, filtro, presencas ->
        val porNome = if (busca.isBlank()) original else {
            original.filter { it.nome.contains(busca, ignoreCase = true) }
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
        verificarEGerarPresencasDeHoje()
    }

    private fun verificarEGerarPresencasDeHoje() {
        val hoje = LocalDate.now()

        if (hoje.dayOfWeek == DayOfWeek.WEDNESDAY) {
            viewModelScope.launch(Dispatchers.IO) {
                // Pega a lista atual de crismandos
                val listaCrismandos: List<Crismando> = crismandoDao.getAllCrismandosStatic()

                val novasPresencas = listaCrismandos.map {
                    Presenca(crismandoId = it.crismandoId, data = dataHoje, estaPresente = false)
                }
                presencaDao.gerarListaPresenca(novasPresencas)
            }
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

    fun alternarPresenca(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val isPresenteHoje = presencaDao.buscarPresencaDoDiaPorCrismando(id, dataHoje)
            presencaDao.atualizarPresenca(id, dataHoje, !isPresenteHoje)
        }
    }
}