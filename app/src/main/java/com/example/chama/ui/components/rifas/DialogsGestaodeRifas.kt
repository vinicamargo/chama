package com.example.chama.ui.components.rifas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// 1. Diálogo para Gerar Blocos
@Composable
fun DialogInserirBlocosEmLote(
    onDismiss: () -> Unit,
    onConfirmar: (quantidadeBlocos: Int) -> Unit
) {
    var quantidadeTexto by remember { mutableStateOf("") }
    val qtdBlocos = quantidadeTexto.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Gerar Novos Blocos", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Cada bloco contém 10 fichas. Informe quantos blocos deseja gerar:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = quantidadeTexto,
                    onValueChange = { input ->
                        quantidadeTexto = input.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("Quantidade de blocos") },
                    placeholder = { Text("Ex: 5 (50 fichas)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (qtdBlocos > 0) {
                    Text(
                        text = "Total de fichas geradas: ${qtdBlocos * 10}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qtdBlocos > 0) {
                        onConfirmar(qtdBlocos)
                        onDismiss()
                    }
                },
                enabled = qtdBlocos > 0,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Gerar Blocos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// 2. Diálogo para Excluir Últimos Blocos
@Composable
fun DialogExcluirUltimosBlocos(
    totalBlocosExistentes: Int,
    onDismiss: () -> Unit,
    onConfirmar: (quantidadeBlocos: Int) -> Unit
) {
    var quantidadeTexto by remember { mutableStateOf("") }
    val qtdBlocos = quantidadeTexto.toIntOrNull() ?: 0
    val ehValido = qtdBlocos in 1..totalBlocosExistentes

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text("Excluir Últimos Blocos", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Total no sistema: $totalBlocosExistentes bloco(s).\nInforme quantos dos últimos blocos cadastrados deseja excluir:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = quantidadeTexto,
                    onValueChange = { input ->
                        quantidadeTexto = input.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("Quantidade de blocos a remover") },
                    placeholder = { Text("Ex: 2") },
                    singleLine = true,
                    isError = quantidadeTexto.isNotBlank() && !ehValido,
                    supportingText = {
                        if (quantidadeTexto.isNotBlank() && !ehValido) {
                            Text("A quantidade deve ser entre 1 e $totalBlocosExistentes blocos.")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ehValido) {
                        onConfirmar(qtdBlocos)
                        onDismiss()
                    }
                },
                enabled = ehValido,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// 3. Alerta de Segurança se houver rifas em uso nos blocos
@Composable
fun DialogConfirmarExclusaoBlocosEmUso(
    quantidadeBlocos: Int,
    quantidadeRifasEmUso: Int,
    onDismiss: () -> Unit,
    onConfirmarForcar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = { Text("Atenção: Blocos em uso!", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "Nos $quantidadeBlocos últimos blocos selecionados para exclusão, há $quantidadeRifasEmUso ficha(s) que já foram marcadas como PAGAS ou possuem VENDEDOR vinculado.\n\nDeseja forçar a exclusão mesmo assim?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmarForcar()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sim, Forçar Exclusão")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}