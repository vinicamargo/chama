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
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.screens.TelaListasPresencas
import com.example.chama.ui.screens.TelaPrincipal
import com.example.chama.ui.screens.TelaRifas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val viewModel = MainViewModel(
            db.crismandoDao(),
            db.presencaDao(),
            db.vendedorDao(),
            db.rifaDao()
        )

        setContent {
            CHAMATheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Tela.Home.rota) {
                        composable(Tela.Home.rota) {
                            TelaPrincipal(
                                onIrParaLista = {navController.navigate(Tela.ListaPresenca.rota)},
                                onIrParaRifas = {navController.navigate(Tela.Rifas.rota)},
                                viewModel = viewModel
                            )
                        }
                        composable(Tela.ListaPresenca.rota) {
                            TelaListasPresencas(viewModel = viewModel)
                        }
                        composable(Tela.Rifas.rota) {
                            TelaRifas(viewModel)
                        }
                    }
                }
            }
        }
    }
}

sealed class Tela(val rota: String) {
    object Home : Tela("home")
    object ListaPresenca : Tela("listaPresenca")

    object Rifas: Tela("rifas")
}

enum class FiltroPresenca {
    TODOS, PRESENTES, AUSENTES
}