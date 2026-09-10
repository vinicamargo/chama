package com.example.chama.ui.components.common.detalhescrismando.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Crismando
import com.example.chama.utils.DataVisualTransformation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DialogEditarCrismando(
    crismando: Crismando,
    corDestaque: Color = MaterialTheme.colorScheme.primary,
    onSalvar: (Crismando) -> Unit,
    onDismiss: () -> Unit
) {
    var nomeEdit by remember(crismando) { mutableStateOf(crismando.nome) }
    var dataNascEdit by remember(crismando) {
        val ddmmyyyy = runCatching {
            crismando.dataNascimento?.let {
                LocalDate.parse(it).format(DateTimeFormatter.ofPattern("ddMMyyyy"))
            }
        }.getOrNull() ?: ""
        mutableStateOf(ddmmyyyy)
    }
    var telEdit by remember(crismando) { mutableStateOf(crismando.telefone ?: "") }
    var respEdit by remember(crismando) { mutableStateOf(crismando.nomeResponsavel ?: "") }
    var telRespEdit by remember(crismando) { mutableStateOf(crismando.telefoneResponsavel ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Editar Crismando", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nomeEdit,
                    onValueChange = { nomeEdit = it },
                    label = { Text("Nome completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dataNascEdit,
                    onValueChange = { input ->
                        dataNascEdit = input.filter { it.isDigit() }.take(8)
                    },
                    label = { Text("Data de Nascimento") },
                    placeholder = { Text("DD/MM/AAAA") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DataVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = telEdit,
                    onValueChange = { input ->
                        telEdit = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("Telefone (apenas números)") },
                    placeholder = { Text("Ex: 11987654321") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedTextField(
                    value = respEdit,
                    onValueChange = { respEdit = it },
                    label = { Text("Nome do Responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = telRespEdit,
                    onValueChange = { input ->
                        telRespEdit = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("Telefone do Responsável") },
                    placeholder = { Text("Ex: 11987654321") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nomeEdit.isNotBlank()) {
                        val dataIso = runCatching {
                            if (dataNascEdit.length == 8) {
                                val dtf = DateTimeFormatter.ofPattern("ddMMyyyy")
                                LocalDate.parse(dataNascEdit, dtf).toString()
                            } else {
                                null
                            }
                        }?.getOrNull()

                        val crismandoAtualizado = crismando.copy(
                            nome = nomeEdit.trim(),
                            dataNascimento = dataIso,
                            telefone = telEdit.trim().ifBlank { null },
                            nomeResponsavel = respEdit.trim().ifBlank { null },
                            telefoneResponsavel = telRespEdit.trim().ifBlank { null }
                        )

                        onSalvar(crismandoAtualizado)
                        onDismiss()
                    }
                }
            ) {
                Text("Salvar Alterações", fontWeight = FontWeight.Bold, color = corDestaque)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
