package com.example.chama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.example.chama.data.AppDatabase
import com.example.chama.ui.theme.CHAMATheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chama.ui.screens.TelaListasPresencas
import com.example.chama.ui.screens.TelaExportador
import com.example.chama.ui.screens.TelaPrincipal

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
                            TelaListasPresencas(viewModel = viewModel)
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