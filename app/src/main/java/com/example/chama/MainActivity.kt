package com.example.chama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.chama.data.AppDatabase
import com.example.chama.ui.theme.CHAMATheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chama.components.ConfirmacaoBottomCard
import com.example.chama.components.CrismandoCard
import com.example.chama.components.SeletorDeFiltroData
import com.example.chama.components.SeletorDeFiltroPresenca
import com.example.chama.screens.TelaCrismandos
import com.example.chama.screens.TelaExportador
import com.example.chama.screens.TelaPrincipal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val viewModel = MainViewModel(db.crismandoDao(), db.presencaDao())

        setContent {
            CHAMATheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Tela.Home.rota) {
                        composable(Tela.Home.rota) {
                            TelaPrincipal(
                                onIrParaLista = {navController.navigate(Tela.ListaHoje.rota)},
                                onIrParaExportador = {navController.navigate(Tela.Exportador.rota)}
                            )
                        }
                        composable(Tela.ListaHoje.rota) {
                            TelaCrismandos(viewModel = viewModel)
                        }
                        composable(Tela.Exportador.rota) {
                            TelaExportador(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

sealed class Tela(val rota: String) {
    object Home : Tela("home")
    object ListaHoje : Tela("listaHoje")
    object Exportador : Tela("exportador")
}

enum class FiltroPresenca {
    TODOS, PRESENTES, AUSENTES
}