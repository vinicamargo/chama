package com.example.chama.ui.components.presencas

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chama.ui.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorDeFiltroData(
    viewModel: MainViewModel,
    modifier: Modifier
) {
    val diasDeCrismaStrings by viewModel.diasComChamada.collectAsState(initial = emptyList())
    val diaSelecionadoString by viewModel.diaSelecionado.collectAsState()

    val diasDeCrisma = remember(diasDeCrismaStrings, viewModel.dataDeHoje) {
        diasDeCrismaStrings.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }.filter { it <= viewModel.dataDeHoje }.sorted()
    }

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }

    val textoExibicao = remember(diaSelecionadoString) {
        if (diaSelecionadoString.isNotBlank()) {
            runCatching {
                LocalDate.parse(diaSelecionadoString).format(formatter)
            }.getOrDefault(diaSelecionadoString)
        } else {
            "Selecione uma data"
        }
    }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth(),
            value = textoExibicao,
            onValueChange = {},
            readOnly = true,
            label = { Text("Data da lista") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            diasDeCrisma.forEach { dataLocalDate ->
                val dataFormatada = dataLocalDate.format(formatter)

                DropdownMenuItem(
                    text = { Text(text = dataFormatada) },
                    onClick = {
                        isDropdownExpanded = false
                        viewModel.alterarData(dataLocalDate.toString())
                    }
                )
            }
        }
    }
}