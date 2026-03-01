package com.example.chama.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.example.chama.FiltroPresenca
import com.example.chama.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorDeFiltroData(
    viewModel: MainViewModel,
    modifier: Modifier
) {
    val diasDeCrismaStrings by viewModel.diasComChamada.collectAsState(initial = emptyList())

    val diasDeCrisma = diasDeCrismaStrings.map { LocalDate.parse(it) }

    val formatter = remember { DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy") }

    var expanded by remember { mutableStateOf(false) }

    val proximoDomingo = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    var selectedOption by remember { mutableStateOf(proximoDomingo.format(formatter)) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text("Data da lista") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            diasDeCrisma.forEach { dataLocalDate ->

                val dataFormatada = dataLocalDate.format(formatter)

                DropdownMenuItem(
                    text = { Text(text = dataFormatada) },
                    onClick = {
                        selectedOption = dataFormatada
                        expanded = false
                        viewModel.alterarData(dataLocalDate.toString())
                    }
                )
            }
        }
    }
}

@Composable
fun SeletorDeFiltroPresencaEAcoes(
    viewModel: MainViewModel,
    modifier: Modifier
){
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importarDadosCsv(context, it)
        }
    }

    val filtroSelecionado by viewModel.filtroPresencaSelecionado
    val totalPresentes by viewModel.totalPresentes.collectAsState()
    val totalAusentes by viewModel.totalAusentes.collectAsState()


    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        FiltroBtn(
            label = "Todos",
            icone = null,
            selecionado = filtroSelecionado == FiltroPresenca.TODOS
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
        }

        FiltroBtn(
            label = " ($totalPresentes)",
            icone = Icons.Default.CheckCircle,
            selecionado = filtroSelecionado == FiltroPresenca.PRESENTES
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        }

        FiltroBtn(
            label = " ($totalAusentes)",
            icone = Icons.Default.Close,
            selecionado = filtroSelecionado == FiltroPresenca.AUSENTES
        ) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        }

        FilledTonalIconButton(
            onClick = {
                launcher.launch("text/*")
            },
            shape = RoundedCornerShape(6.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0x9C1C6914),
                contentColor = Color.White
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Input,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        FilledTonalIconButton(
            onClick = {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    val dadosCsv = viewModel.exportarParaCSV()

                    val file = File(context.cacheDir, "relatorio_presenca.csv")
                    file.writeText(dadosCsv, charset = Charsets.UTF_8)

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Exportar Planilha"))
                }
            },
            shape = RoundedCornerShape(6.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0x9C1C6914),
                contentColor = Color.White
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),

                )
            }
        }
    }
}

@Composable
fun FiltroBtn(label: String, icone: ImageVector?, selecionado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = Color.Black
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icone != null){
                Icon(
                    imageVector = icone,
                    contentDescription = "Presentes",
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(label)
        }
    }
}