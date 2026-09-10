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
import androidx.compose.material.icons.filled.Cake
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SecaoDadosContato(
    crismando: Crismando,
    corDestaque: Color,
    isExpandido: Boolean,
    onToggleExpandir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SecaoColapsavelCard(
        titulo = "Dados & Contato",
        icone = Icons.Default.ContactPhone,
        corDestaque = corDestaque,
        isExpandido = isExpandido,
        onToggleExpandir = onToggleExpandir,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val dataFormatada = runCatching {
                crismando.dataNascimento?.let {
                    LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }
            }?.getOrNull() ?: crismando.dataNascimento ?: "Não informada"
            val idadeTexto = crismando.idade?.let { " ($it anos)" } ?: ""

            ItemInfoCard(
                icone = Icons.Default.Cake,
                titulo = "Data de Nascimento",
                valor = "$dataFormatada$idadeTexto",
                corIcone = corDestaque
            )

            ItemContatoCard(
                titulo = "Celular do Crismando",
                telefone = crismando.celular,
                corIcone = corDestaque,
                onLigar = { tel -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))) },
                onWhatsApp = { tel -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$tel"))) }
            )

            ItemInfoCard(
                icone = Icons.Default.FamilyRestroom,
                titulo = "Responsável",
                valor = crismando.relacionamentoResponsavel ?: "Não informado",
                corIcone = corDestaque
            )

            ItemContatoCard(
                titulo = "Celular do Responsável",
                telefone = crismando.celularResponsavel,
                corIcone = corDestaque,
                onLigar = { tel -> context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))) },
                onWhatsApp = { tel -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$tel"))) }
            )
        }
    }
}

@Composable
private fun ItemInfoCard(
    icone: ImageVector,
    titulo: String,
    valor: String,
    corIcone: Color = MaterialTheme.colorScheme.primary
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ItemContatoCard(
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
        else -> telefone ?: "Não informado"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = corIcone, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = telefoneFormatado, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }

            if (digitos.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onWhatsApp(digitos) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { onLigar(digitos) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Ligar", tint = corIcone, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}