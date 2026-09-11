package com.example.chama.ui.components.common.detalhescrismando.sections

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Crismando

@Composable
fun SecaoDadosContato(
    crismando: Crismando,
    corDestaque: Color,
    isExpandido: Boolean,
    onToggleExpandir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Resolve o nome direto do responsável com o parentesco para o cabeçalho
    val nomeResponsavel = when (crismando.relacionamentoResponsavel?.trim()?.lowercase()) {
        "pai" -> crismando.nomePai?.ifBlank { null }
        "mãe", "mae" -> crismando.nomeMae?.ifBlank { null }
        else -> null
    } ?: crismando.nomePai?.ifBlank { null }
    ?: crismando.nomeMae?.ifBlank { null }

    val tituloResponsavel = buildString {
        if (!nomeResponsavel.isNullOrBlank()) {
            append(nomeResponsavel)
            crismando.relacionamentoResponsavel?.let { append(" ($it)") }
        } else if (!crismando.relacionamentoResponsavel.isNullOrBlank()) {
            append("Responsável (${crismando.relacionamentoResponsavel})")
        } else {
            append("Responsável")
        }
    }

    SecaoColapsavelCard(
        titulo = "Contato",
        icone = Icons.Default.ContactPhone,
        corDestaque = corDestaque,
        isExpandido = isExpandido,
        onToggleExpandir = onToggleExpandir,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 1. Contato do Crismando
            ItemContatoCard(
                icone = Icons.Default.Phone,
                titulo = "Celular do Crismando",
                telefone = crismando.celular,
                corIcone = corDestaque,
                onLigar = { tel -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))) },
                onWhatsApp = { tel -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$tel"))) }
            )

            // 2. Contato do Responsável (Título direto com Nome + Parentesco, sem subtítulo abaixo)
            ItemContatoCard(
                icone = Icons.Default.FamilyRestroom,
                titulo = tituloResponsavel,
                telefone = crismando.celularResponsavel,
                corIcone = corDestaque,
                onLigar = { tel -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))) },
                onWhatsApp = { tel -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$tel"))) }
            )
        }
    }
}

@Composable
private fun ItemContatoCard(
    icone: ImageVector,
    titulo: String,
    telefone: String?,
    corIcone: Color = MaterialTheme.colorScheme.primary,
    onLigar: (String) -> Unit,
    onWhatsApp: (String) -> Unit
) {
    val digitos = telefone?.filter { it.isDigit() } ?: ""
    val telefoneFormatado = when (digitos.length) {
        11 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 7)}-${digitos.substring(7)}"
        10 -> "(${digitos.substring(0, 2)}) ${digitos.substring(2, 6)}-${digitos.substring(6)}"
        else -> telefone?.ifBlank { "Não informado" } ?: "Não informado"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = corIcone,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = telefoneFormatado,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (digitos.isNotBlank()) {
                Spacer(modifier = Modifier.width(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = { onWhatsApp(digitos) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onLigar(digitos) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Ligar",
                            tint = corIcone,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}