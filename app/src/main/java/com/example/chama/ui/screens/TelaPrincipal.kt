package com.example.chama.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.chama.ui.MainViewModel
import com.example.chama.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TelaPrincipal(
    viewModel: MainViewModel,
    onIrParaLista: () -> Unit,
    onIrParaRifas: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.mipmap.basilica_coat_of_arms_foreground),
            contentDescription = "Logo do App",
            modifier = Modifier.size(400.dp)
        )
        Text(
            text = "CHAMA",
            fontSize = 42.sp
        )
        Text(
            text = "Aplicativo auxiliar da catequese de crisma 25/26",
            modifier = Modifier.padding(top = 15.dp),
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(36.dp))

        Column()
        {
            Button(
                onClick = onIrParaLista,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Lista de Presença")
            }

            Spacer(modifier = Modifier.padding(vertical = 4.dp))

            Button(
                onClick = onIrParaRifas,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Gestão de rifas")
            }
        }
    }
}