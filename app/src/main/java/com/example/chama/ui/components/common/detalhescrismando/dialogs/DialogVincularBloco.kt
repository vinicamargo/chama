package com.example.chama.ui.components.common.detalhescrismando.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chama.ui.StatusBlocoRifa
import kotlinx.coroutines.delay

@Composable
fun DialogVincularBloco(
    nomeCrismando: String,
    crismandoId: Long,
    corDestaque: Color = MaterialTheme.colorScheme.primary,
    onVerificarBloco: suspend (numeroBloco: Int) -> StatusBlocoRifa = { StatusBlocoRifa.Disponivel },
    onVincularBloco: (crismandoId: Long, numeroBloco: Int, onError: (String) -> Unit, onSuccess: () -> Unit) -> Unit = { _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    var numeroBlocoInput by remember { mutableStateOf("") }
    var erroBlocoInput by remember { mutableStateOf<String?>(null) }
    var isProcessandoBloco by remember { mutableStateOf(false) }
    var statusBloco by remember { mutableStateOf<StatusBlocoRifa>(StatusBlocoRifa.Vazio) }
    var isVerificando by remember { mutableStateOf(false) }

    LaunchedEffect(numeroBlocoInput) {
        val bloco = numeroBlocoInput.toIntOrNull()
        if (bloco == null || bloco <= 0) {
            statusBloco = StatusBlocoRifa.Vazio
            erroBlocoInput = null
            isVerificando = false
            return@LaunchedEffect
        }

        isVerificando = true
        delay(300)
        val resultado = onVerificarBloco(bloco)
        statusBloco = resultado
        isVerificando = false

        erroBlocoInput = when (resultado) {
            is StatusBlocoRifa.Inexistente -> {
                if (resultado.ultimoBloco > 0) "Último bloco existente: ${resultado.ultimoBloco}" else "Não há rifas cadastradas"
            }
            is StatusBlocoRifa.Ocupado -> {
                if (resultado.isMesmoCrismando) "Já está com este crismando" else "Em posse de ${resultado.nomeResponsavel}"
            }
            else -> null
        }
    }

    val isBlocoValido = statusBloco is StatusBlocoRifa.Disponivel

    AlertDialog(
        onDismissRequest = {
            if (!isProcessandoBloco) onDismiss()
        },
        icon = {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = corDestaque,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text("Vincular Bloco de Rifas", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Informe o número do bloco a ser entregue para $nomeCrismando.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = numeroBlocoInput,
                    onValueChange = { input ->
                        numeroBlocoInput = input.filter { it.isDigit() }
                    },
                    label = { Text("Número do Bloco") },
                    placeholder = { Text("Ex: 1, 2, 15...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = erroBlocoInput != null,
                    supportingText = {
                        when {
                            isVerificando -> {
                                Text("Verificando disponibilidade...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            erroBlocoInput != null -> {
                                Text(erroBlocoInput!!, color = MaterialTheme.colorScheme.error)
                            }
                            isBlocoValido -> {
                                Text("✓ Bloco disponível", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val numeroBloco = numeroBlocoInput.toIntOrNull()
                if (numeroBloco != null && numeroBloco > 0) {
                    val inicio = (numeroBloco - 1) * 10 + 1
                    val fim = inicio + 9
                    Text(
                        text = "Bilhetes: $inicio ao $fim",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isBlocoValido && !isProcessandoBloco && !isVerificando,
                onClick = {
                    val bloco = numeroBlocoInput.toIntOrNull() ?: return@TextButton
                    isProcessandoBloco = true
                    onVincularBloco(
                        crismandoId,
                        bloco,
                        { mensagemErro ->
                            isProcessandoBloco = false
                            erroBlocoInput = mensagemErro
                        },
                        {
                            isProcessandoBloco = false
                            onDismiss()
                        }
                    )
                }
            ) {
                Text(
                    text = if (isProcessandoBloco) "Salvando..." else "Vincular",
                    fontWeight = FontWeight.Bold,
                    color = if (isBlocoValido) corDestaque else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isProcessandoBloco,
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}
