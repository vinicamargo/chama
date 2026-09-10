package com.example.chama.ui.components.common.detalhescrismando.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Signpost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Crismando

@Composable
fun SecaoFiliacaoResidencia(
    crismando: Crismando,
    corDestaque: Color,
    isExpandido: Boolean,
    onToggleExpandir: () -> Unit,
    modifier: Modifier = Modifier
) {
    SecaoColapsavelCard(
        titulo = "Filiação e Residência",
        icone = Icons.Default.FamilyRestroom,
        corDestaque = corDestaque,
        isExpandido = isExpandido,
        onToggleExpandir = onToggleExpandir,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ItemDetalheTexto(icone = Icons.Default.FamilyRestroom, rotulo = "Nome do Pai", valor = crismando.nomePai ?: "Não informado", corIcone = corDestaque)
            ItemDetalheTexto(icone = Icons.Default.FamilyRestroom, rotulo = "Nome da Mãe", valor = crismando.nomeMae ?: "Não informado", corIcone = corDestaque)
            ItemDetalheTexto(icone = Icons.Default.Home, rotulo = "Endereço", valor = crismando.endereco ?: "Não informado", corIcone = corDestaque)

            val cidadeCep = "${crismando.cidadeAtual ?: "Santo André"}${crismando.cep?.let { " • CEP: $it" } ?: ""}"
            ItemDetalheTexto(icone = Icons.Default.Signpost, rotulo = "Cidade / CEP", valor = cidadeCep, corIcone = corDestaque)
        }
    }
}
