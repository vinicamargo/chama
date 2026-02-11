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
import com.example.chama.components.SeletorDeFiltro

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
                            TelaPrincipal(onIrParaLista = {
                                navController.navigate(Tela.Lista.rota)
                            })
                        }

                        composable(Tela.Lista.rota) {
                            TelaCrismandos(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelaPrincipal(onIrParaLista: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.logo_foreground),
            contentDescription = "Logo do App",
            modifier = Modifier.size(300.dp)
        )
        Text(
            text = "CHAMA 1.0",
            fontSize = 32.sp
        )
        Text(
            text = "Lista de chamada dos crismandos 25/26",
            modifier = Modifier.padding(top = 15.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIrParaLista,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Ver Lista de Crismandos")
        }
    }
}

@Composable
fun TelaCrismandos(viewModel: MainViewModel) {
    val listaCrismandosFiltrada by viewModel.listaCrismandosFiltrada.collectAsState()
    val presencas by viewModel.presencasDoDia.collectAsState()
    val textoBusca by viewModel.textoPesquisa
    val crismandoSelecionado by viewModel.crismandoSelecionado

    val isCrismandoSelecionadoPresente = remember(crismandoSelecionado, presencas) {
        val estaPresente = presencas.find {
            it.crismandoId == crismandoSelecionado?.crismandoId
        }?.estaPresente ?: false
        estaPresente
    }

    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 30.dp)) {
            OutlinedTextField(
                value = textoBusca,
                onValueChange = { viewModel.onDigitacao(it) },
                label = { Text("Filtrar por nome") },
                modifier = Modifier.fillMaxWidth()
            )

            SeletorDeFiltro(
                viewModel,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn {
                items(listaCrismandosFiltrada) { crismando ->
                    val presencaCrismando = presencas.find { it.crismandoId == crismando.crismandoId}?.estaPresente

                    CrismandoCard(
                        crismando,
                        estaPresente = presencaCrismando,
                        selecionado = crismando == crismandoSelecionado,
                        onClick = {viewModel.selecionar(crismando)}
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = crismandoSelecionado != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            ConfirmacaoBottomCard(
                isCrismandoPresente = isCrismandoSelecionadoPresente,
                nome = crismandoSelecionado?.nome,
                onConfirmar = {
                    crismandoSelecionado?.let {
                        viewModel.alternarPresenca(it.crismandoId)
                        viewModel.selecionar(null)
                    }
                },
                onCancelar = { viewModel.selecionar(null) }
            )
            }
        }

    }

sealed class Tela(val rota: String) {
    object Home : Tela("home")
    object Lista : Tela("lista")
}

enum class FiltroPresenca {
    TODOS, PRESENTES, AUSENTES
}