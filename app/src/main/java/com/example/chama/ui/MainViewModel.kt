package com.example.chama

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chama.data.Crismando
import com.example.chama.data.CrismandoDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val dao: CrismandoDao) : ViewModel() {

    // A lista original que vem do banco (sempre atualizada)
    val crismandosOriginal: StateFlow<List<Crismando>> = dao.getAllCrismandos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // O estado do texto que o usuário digita
    var textoPesquisa = mutableStateOf("")
        private set

    // No seu MainViewModel.kt
    var idSelecionado = mutableStateOf<Long?>(null)
        private set

    fun selecionar(id: Long) {
        // Se clicar no que já está selecionado, ele desmarca (toggle)
        idSelecionado.value = if (idSelecionado.value == id) null else id
    }

    fun onDigitacao(novoTexto: String) {
        textoPesquisa.value = novoTexto
    }
}