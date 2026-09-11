package com.example.chama.ui.components.common.detalhescrismando.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.example.chama.ui.components.common.detalhescrismando.BlocoItemUI

@Composable
fun DialogAcoesBloco(
    blocoItem: BlocoItemUI,
    nomeCrismando: String,
    corDestaque: Color,
    onDismiss: () -> Unit,
    onAlternarPagamento: (numeroBloco: Int, estaPagoAtual: Boolean) -> Unit,
    onDesvincular: (numeroBloco: Int) -> Unit
) {
    var showConfirmarPagamento by remember { mutableStateOf(false) }
    var showConfirmarDesvinculo by remember { mutableStateOf(false) }

    val inicio = (blocoItem.numero - 1) * 10 + 1
    val fim = inicio + 9

    if (!showConfirmarPagamento && !showConfirmarDesvinculo) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = corDestaque,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Bloco ${blocoItem.numero}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bilhetes: $inicio ao $fim", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (blocoItem.estaPago) "Situação: Pago" else "Situação: Pendente",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (blocoItem.estaPago) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { showConfirmarPagamento = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (blocoItem.estaPago) Icons.Default.RemoveCircleOutline else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (blocoItem.estaPago) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (blocoItem.estaPago) "Marcar como Não Pago" else "Marcar como Pago",
                            color = if (blocoItem.estaPago) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                    }

                    OutlinedButton(
                        onClick = { showConfirmarDesvinculo = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desvincular do Crismando")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Fechar") }
            }
        )
    }

    if (showConfirmarPagamento) {
        val novoStatus = if (blocoItem.estaPago) "NÃO PAGO" else "PAGO"
        AlertDialog(
            onDismissRequest = { showConfirmarPagamento = false },
            title = { Text("Confirmar Pagamento", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja marcar todos os 10 bilhetes do Bloco ${blocoItem.numero} como $novoStatus?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAlternarPagamento(blocoItem.numero, blocoItem.estaPago)
                        showConfirmarPagamento = false
                        onDismiss()
                    }
                ) {
                    Text("Confirmar", fontWeight = FontWeight.Bold, color = corDestaque)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmarPagamento = false }) { Text("Cancelar") }
            }
        )
    }

    if (showConfirmarDesvinculo) {
        AlertDialog(
            onDismissRequest = { showConfirmarDesvinculo = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Desvincular Bloco?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Tem certeza que deseja retirar o Bloco ${blocoItem.numero} de posse de $nomeCrismando? O bloco voltará a ficar disponível.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDesvincular(blocoItem.numero)
                        showConfirmarDesvinculo = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Desvincular", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmarDesvinculo = false }) { Text("Cancelar") }
            }
        )
    }
}