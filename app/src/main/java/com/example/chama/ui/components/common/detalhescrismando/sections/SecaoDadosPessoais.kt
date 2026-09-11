package com.example.chama.ui.components.common.detalhescrismando.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Crismando
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SecaoDadosPessoais(
    crismando: Crismando,
    corDestaque: Color,
    isExpandido: Boolean,
    onToggleExpandir: () -> Unit,
    modifier: Modifier = Modifier
) {
    SecaoColapsavelCard(
        titulo = "Registro Civil e Origem",
        icone = Icons.Default.Badge,
        corDestaque = corDestaque,
        isExpandido = isExpandido,
        onToggleExpandir = onToggleExpandir,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val cpfFormatado = crismando.cpf?.let {
                if (it.length == 11) "${it.substring(0, 3)}.${it.substring(3, 6)}.${it.substring(6, 9)}-${it.substring(9)}" else it
            } ?: "Não informado"
            ItemDetalheTexto(icone = Icons.Default.Badge, rotulo = "CPF", valor = cpfFormatado, corIcone = corDestaque)

            // Formatação segura da data para dd/MM/yyyy
            val dataFormatada = runCatching {
                crismando.dataNascimento?.let {
                    LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }
            }.getOrNull() ?: crismando.dataNascimento ?: "Não inf."

            val idadeTxt = crismando.idade?.let { " ($it anos)" } ?: ""
            ItemDetalheTexto(
                icone = Icons.Default.Cake,
                rotulo = "Nascimento",
                valor = "$dataFormatada$idadeTxt",
                corIcone = corDestaque
            )

            val naturalidade = if (!crismando.cidadeNascimento.isNullOrBlank() || !crismando.estadoNascimento.isNullOrBlank()) {
                "${crismando.cidadeNascimento ?: ""} - ${crismando.estadoNascimento ?: ""}"
            } else "Não informada"
            ItemDetalheTexto(icone = Icons.Default.LocationCity, rotulo = "Naturalidade", valor = naturalidade, corIcone = corDestaque)

            ItemDetalheTexto(icone = Icons.Default.Public, rotulo = "Nacionalidade", valor = crismando.paisNascimento ?: "Brasil", corIcone = corDestaque)
        }
    }
}