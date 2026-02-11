package com.example.chama

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chama.data.Crismando
import com.example.chama.data.CrismandoDao
import com.example.chama.data.Presenca
import com.example.data.PresencaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val crismandosOriginal: StateFlow<List<Crismando>> = crismandoDao.getAllCrismandos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var textoPesquisa = mutableStateOf("")
        private set

    var idSelecionado = mutableStateOf<Long?>(null)
        private set

    init {
        verificarEGerarListaDeHoje()
    }

    private fun verificarEGerarListaDeHoje() {
        val hoje = LocalDate.now()

        if (hoje.dayOfWeek == DayOfWeek.TUESDAY) {
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

    var crismandoSelecionado = mutableStateOf<Crismando?>(null)
        private set

    fun selecionar(crismando: Crismando?) {
        // Se clicar no que já está selecionado, ele desmarca (toggle)
        crismandoSelecionado.value = if (crismandoSelecionado.value?.crismandoId == crismando?.crismandoId){
            null
        } else {
            crismando
        }
    }

    fun onDigitacao(novoTexto: String) {
        textoPesquisa.value = novoTexto
    }

    fun confirmarPresenca(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            // Salva como presente (true) para a data de hoje
            presencaDao.atualizarPresenca(id, dataHoje, true)
            // Limpa a seleção para fechar o card
            idSelecionado.value = null
        }
    }
}