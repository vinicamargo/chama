package com.example.chama.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.chama.R
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.theme.GermaniaOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TelaPrincipal(
    viewModel: MainViewModel,
    onIrParaLista: () -> Unit,
    onIrParaPainelGerencial: () -> Unit,
    onIrParaRifas: () -> Unit,
    onIrParaGestaoEventos: () -> Unit
) {
    val context = LocalContext.current
    val solidRedBackground = Color(0xFF5B0000)

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importarBackupZip(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(solidRedBackground)
    ) {
        // Ações de Backup no Topo
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    zipPickerLauncher.launch(
                        arrayOf(
                            "application/zip",
                            "application/x-zip-compressed",
                            "application/octet-stream",
                            "*/*"
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Importar Backup ZIP",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        val zipFile = viewModel.exportarBackupCompletoZip(context)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            zipFile
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar Backup Geral (.zip)"))
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Exportar Backup ZIP",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Conteúdo central
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.espirito_santo),
                contentDescription = "Logo do App",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CHAMA",
                fontSize = 50.sp,
                textAlign = TextAlign.Center,
                fontFamily = GermaniaOne,
                color = Color.White
            )

            Text(
                text = "Gerenciador da catequese de crisma",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp),
                fontFamily = GermaniaOne,
                color = Color.White.copy(alpha = 0.95f)
            )

            Text(
                text = "2026/2027",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 24.dp),
                fontFamily = GermaniaOne,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Linha 1: Presenças e Painel Gerencial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CardMenuInicial(
                        titulo = "Presenças",
                        subtitulo = "Chamada e faltas",
                        icone = Icons.Default.Checklist,
                        onClick = onIrParaLista,
                        modifier = Modifier.weight(1f)
                    )

                    CardMenuInicial(
                        titulo = "Painel",
                        subtitulo = "Relatórios gerais",
                        icone = Icons.Default.Assessment,
                        onClick = onIrParaPainelGerencial,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Linha 2: Rifas e Eventos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CardMenuInicial(
                        titulo = "Rifas",
                        subtitulo = "Blocos e acertos",
                        icone = Icons.Default.ConfirmationNumber,
                        onClick = onIrParaRifas,
                        modifier = Modifier.weight(1f)
                    )

                    CardMenuInicial(
                        titulo = "Eventos",
                        subtitulo = "Retiros e passeios",
                        icone = Icons.Default.Event,
                        onClick = onIrParaGestaoEventos,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardMenuInicial(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)
    val cardBackground = Color(0x33000000)
    val cardBorderColor = Color(0x55FFFFFF)

    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clip(cardShape)
            .background(cardBackground)
            .border(1.5.dp, cardBorderColor, cardShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = titulo,
                fontFamily = GermaniaOne,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitulo,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )
        }
    }
}