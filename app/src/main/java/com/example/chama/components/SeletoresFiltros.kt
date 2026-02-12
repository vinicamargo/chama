package com.example.chama.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import com.example.chama.FiltroPresenca
import com.example.chama.MainViewModel
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
    val diasDeCrismaStrings by viewModel.diasComPresencas.collectAsState(initial = emptyList())

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
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            diasDeCrisma.forEach { dataLocalDate ->

                val dataFormatada = dataLocalDate.format(formatter)

                DropdownMenuItem(
                    text = { Text(text = dataFormatada) },
                    onClick = {
                        selectedOption = dataFormatada
                        expanded = false
                        viewModel.atualizarData(dataLocalDate.toString())
                    }
                )
            }
        }
    }
}

@Composable
fun SeletorDeFiltroPresenca(
    viewModel: MainViewModel,
    modifier: Modifier
){
    val filtroSelecionado by viewModel.filtroPresencaAtual

    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        FiltroBtn("Todos", filtroSelecionado == FiltroPresenca.TODOS) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.TODOS)
        }
        FiltroBtn("Presentes", filtroSelecionado == FiltroPresenca.PRESENTES) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.PRESENTES)
        }
        FiltroBtn("Ausentes", filtroSelecionado == FiltroPresenca.AUSENTES) {
            viewModel.alterarFiltroPresenca(FiltroPresenca.AUSENTES)
        }
    }
}

@Composable
fun FiltroBtn(label: String, selecionado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = Color.Black
        )
    ) {
        Text(label)
    }
}