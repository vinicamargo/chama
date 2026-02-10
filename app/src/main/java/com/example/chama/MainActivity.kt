package com.example.chama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chama.components.CrismandoCard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext, lifecycleScope)

        val viewModel = MainViewModel(db.crismandoDao())

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
            painter = painterResource(id = R.mipmap.logo_foreground), // Troque pelo nome do seu arquivo
            contentDescription = "Logo do App",
            modifier = Modifier.size(300.dp)
        )

        // 2. Um texto personalizado
        Text(
            text = "CHAMA 1.0",
//            modifier = Modifier.padding(top = 10.dp),
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
    val listaOriginal by viewModel.crismandosOriginal.collectAsState()
    val textoBusca by viewModel.textoPesquisa
    val idSelecionado by viewModel.idSelecionado

    val listaFiltrada = remember(textoBusca, listaOriginal) {
        listaOriginal.filter { crismando ->
            crismando.nome.contains(textoBusca, ignoreCase = true)
        }
    }

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

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(listaFiltrada) { crismando ->
                CrismandoCard(
                    crismando,
                    selecionado = idSelecionado == crismando.id,
                    onClick = {viewModel.selecionar(crismando.id)}
                )
            }
        }
    }
}

sealed class Tela(val rota: String) {
    object Home : Tela("home")
    object Lista : Tela("lista")
}