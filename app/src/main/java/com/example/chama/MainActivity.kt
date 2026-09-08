package com.example.chama

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chama.data.AppDatabase
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.screens.TelaListasPresencas
import com.example.chama.ui.screens.TelaPainelGerencial
import com.example.chama.ui.screens.TelaPrincipal
import com.example.chama.ui.screens.TelaRifas
import com.example.chama.ui.theme.CHAMATheme
import com.example.chama.utils.NotificacaoAgendador

class MainActivity : ComponentActivity() {

    // Estado reativo para capturar a notificação tanto com app fechado quanto em segundo plano
    private var crismandoIdDestino by mutableStateOf<Long?>(null)
    private var abrirPainelPorNotificacao by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val viewModel = MainViewModel(
            db.crismandoDao(),
            db.presencaDao(),
            db.vendedorDao(),
            db.rifaDao()
        )

        NotificacaoAgendador.agendarNotificacaoDiaria(this)

        // Processa os extras caso a Activity tenha subido diretamente pelo clique
        processarIntentNotificacao(intent)

        setContent {
            CHAMATheme {
                val context = LocalContext.current

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Permissão concedida ou negada */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Se foi aberto pela notificação, navega imediatamente para o painel
                    LaunchedEffect(abrirPainelPorNotificacao) {
                        if (abrirPainelPorNotificacao) {
                            navController.navigate(Tela.PainelGerencial.rota)
                            abrirPainelPorNotificacao = false
                        }
                    }

                    NavHost(navController = navController, startDestination = Tela.Home.rota) {
                        composable(Tela.Home.rota) {
                            TelaPrincipal(
                                onIrParaLista = { navController.navigate(Tela.ListaPresenca.rota) },
                                onIrParaRifas = { navController.navigate(Tela.Rifas.rota) },
                                onIrParaPainelGerencial = { navController.navigate(Tela.PainelGerencial.rota) },
                                viewModel = viewModel
                            )
                        }
                        composable(Tela.ListaPresenca.rota) {
                            TelaListasPresencas(viewModel = viewModel)
                        }
                        composable(Tela.Rifas.rota) {
                            TelaRifas(viewModel)
                        }
                        composable(Tela.PainelGerencial.rota) {
                            TelaPainelGerencial(
                                viewModel = viewModel,
                                crismandoIdInicial = crismandoIdDestino,
                                onVoltar = {
                                    crismandoIdDestino = null
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processarIntentNotificacao(intent)
    }

    private fun processarIntentNotificacao(intent: Intent?) {
        if (intent?.getBooleanExtra("abrir_painel_gerencial", false) == true) {
            val id = intent.getLongExtra("crismando_detalhes_id", -1L)
            crismandoIdDestino = if (id != -1L) id else null
            abrirPainelPorNotificacao = true
        }
    }
}

sealed class Tela(val rota: String) {
    object Home : Tela("home")
    object ListaPresenca : Tela("listaPresenca")
    object Rifas : Tela("rifas")
    object PainelGerencial : Tela("painelGerencial")
}

enum class FiltroPresenca {
    TODOS, PRESENTES, AUSENTES
}