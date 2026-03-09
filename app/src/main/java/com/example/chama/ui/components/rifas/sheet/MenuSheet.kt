package com.example.chama.ui.components.rifas.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Rifa
import com.example.chama.ui.MainViewModel
import kotlin.collections.get

@Composable
fun MenuSheet(
    viewModel: MainViewModel,
    onAlterarVendedor: () -> Unit,
    onRemoverVendedor: () -> Unit,
    onAlternarIsPago: () -> Unit,
    rifaSelecionada: Rifa
) {
    val mapaNomeVendedores by viewModel.mapaNomeVendedores.collectAsState()
    val isPago = rifaSelecionada.estaPaga

    var titleAlternarPagamento = ""
    var subtitleAlternarPagamento = ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gerenciar bloco ${rifaSelecionada.bloco}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = mapaNomeVendedores[rifaSelecionada.vendedorId]?.let { "Vendedor(a): $it"
                } ?: "Sem vendedor",
                modifier = Modifier.padding(top = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
        )

        MenuSheetItem(
            titulo = "${if (rifaSelecionada.vendedorId == null) "Vincular" else ("Alterar")} vendedor",
            subtitulo = "Vincular crismando, catequista ou vendedor externo",
            onClick = onAlterarVendedor
        )

        if (isPago){
            titleAlternarPagamento = "Cancelar pagamento"
            subtitleAlternarPagamento = "Informar pagamento não efetuado para o bloco"
        } else {
            titleAlternarPagamento = "Confirmar pagamento"
            subtitleAlternarPagamento = "Informar pagamento efetuado para o bloco"
        }

        if (rifaSelecionada.vendedorId != null) {
            MenuSheetItem(
                titulo = "Remover vendedor",
                subtitulo = "Remover vendedor vinculado",
                onClick = onRemoverVendedor
            )

            MenuSheetItem(
                titulo = titleAlternarPagamento,
                subtitulo = subtitleAlternarPagamento,
                onClick = onAlternarIsPago
            )
        }
    }
}