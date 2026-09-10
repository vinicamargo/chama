package com.example.chama.ui.components.common.detalhescrismando.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Crismando

@Composable
fun DialogEditarCrismando(
    crismando: Crismando,
    corDestaque: Color = MaterialTheme.colorScheme.primary,
    onSalvar: (Crismando) -> Unit,
    onDismiss: () -> Unit
) {
    // Dados Pessoais e Contato
    var nome by remember { mutableStateOf(crismando.nome) }
    var dataNascimento by remember { mutableStateOf(crismando.dataNascimento ?: "") }
    var telefone by remember { mutableStateOf(crismando.telefone ?: "") }
    var nomeResponsavel by remember { mutableStateOf(crismando.nomeResponsavel ?: "") }
    var telefoneResponsavel by remember { mutableStateOf(crismando.telefoneResponsavel ?: "") }

    // Sacramentos
    var isBatizado by remember { mutableStateOf(crismando.isBatizado) }
    var certidaoEntregue by remember { mutableStateOf(crismando.certidaoBatismoEntregue) }
    var paroquiaBatismo by remember { mutableStateOf(crismando.paroquiaBatismo ?: "") }
    var temPrimeiraComunhao by remember { mutableStateOf(crismando.temPrimeiraComunhao) }

    // Estado do Diálogo de Confirmação
    var showConfirmacaoDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Crismando",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Dados Pessoais",
                    style = MaterialTheme.typography.labelLarge,
                    color = corDestaque,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dataNascimento,
                    onValueChange = { dataNascimento = it },
                    label = { Text("Data de Nascimento (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = nomeResponsavel,
                    onValueChange = { nomeResponsavel = it },
                    label = { Text("Nome do Responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = telefoneResponsavel,
                    onValueChange = { telefoneResponsavel = it },
                    label = { Text("Telefone do Responsável") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Vida Sacramental",
                    style = MaterialTheme.typography.labelLarge,
                    color = corDestaque,
                    fontWeight = FontWeight.Bold
                )

                // 1. Switch Batismo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isBatizado = !isBatizado
                            if (!isBatizado) {
                                temPrimeiraComunhao = false // Regra canônica: sem batismo, sem comunhão
                                certidaoEntregue = false
                                paroquiaBatismo = ""
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("É Batizado?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (isBatizado) "Possui o sacramento do batismo" else "Receberá durante a catequese",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isBatizado,
                        onCheckedChange = { novoValor ->
                            isBatizado = novoValor
                            if (!novoValor) {
                                temPrimeiraComunhao = false
                                certidaoEntregue = false
                                paroquiaBatismo = ""
                            }
                        }
                    )
                }

                // 2. Detalhes do Batismo (Certidão e Paróquia) - Só exibe se for batizado
                AnimatedVisibility(
                    visible = isBatizado,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Checkbox Certidão Entregue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { certidaoEntregue = !certidaoEntregue },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = certidaoEntregue,
                                onCheckedChange = { certidaoEntregue = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Certidão de Batismo entregue",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Input Paróquia onde foi batizado
                        OutlinedTextField(
                            value = paroquiaBatismo,
                            onValueChange = { paroquiaBatismo = it },
                            label = { Text("Paróquia onde foi batizado") },
                            placeholder = { Text("Ex: São Geraldo Magella") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // 3. Switch Primeira Eucaristia
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = isBatizado) { temPrimeiraComunhao = !temPrimeiraComunhao }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Primeira Comunhão?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBatizado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                        Text(
                            text = when {
                                !isBatizado -> "Requer o Batismo prévio"
                                temPrimeiraComunhao -> "Já recebeu a 1ª Eucaristia"
                                else -> "Receberá durante a catequese"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isBatizado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = temPrimeiraComunhao,
                        onCheckedChange = { temPrimeiraComunhao = it },
                        enabled = isBatizado
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nome.isNotBlank()) {
                        showConfirmacaoDialog = true
                    }
                }
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold, color = corDestaque)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Diálogo de Confirmação com Lista de Alterações
    if (showConfirmacaoDialog) {
        val nomeTratado = nome.trim()
        val dataNascTratada = dataNascimento.trim().ifBlank { null }
        val telTratado = telefone.trim().ifBlank { null }
        val respTratado = nomeResponsavel.trim().ifBlank { null }
        val telRespTratado = telefoneResponsavel.trim().ifBlank { null }
        val certidaoTratada = if (isBatizado) certidaoEntregue else false
        val paroquiaTratada = if (isBatizado) paroquiaBatismo.trim().ifBlank { null } else null
        val comunhaoTratada = if (isBatizado) temPrimeiraComunhao else false

        // Mapeia todas as diferenças detectadas
        val alteracoes = remember(
            crismando,
            nomeTratado,
            dataNascTratada,
            telTratado,
            respTratado,
            telRespTratado,
            isBatizado,
            certidaoTratada,
            paroquiaTratada,
            comunhaoTratada
        ) {
            val lista = mutableListOf<String>()

            fun diff(campo: String, antigo: Any?, novo: Any?) {
                if (antigo != novo) {
                    val txtAntigo = antigo?.toString()?.ifBlank { "Vazio" } ?: "Vazio"
                    val txtNovo = novo?.toString()?.ifBlank { "Vazio" } ?: "Vazio"
                    lista.add("• $campo: $txtAntigo ➔ $txtNovo")
                }
            }

            fun boolTxt(b: Boolean) = if (b) "Sim" else "Não"

            diff("Nome", crismando.nome, nomeTratado)
            diff("Nascimento", crismando.dataNascimento, dataNascTratada)
            diff("Telefone", crismando.telefone, telTratado)
            diff("Responsável", crismando.nomeResponsavel, respTratado)
            diff("Tel. Responsável", crismando.telefoneResponsavel, telRespTratado)
            diff("Batizado", boolTxt(crismando.isBatizado), boolTxt(isBatizado))
            diff("Certidão Entregue", boolTxt(crismando.certidaoBatismoEntregue), boolTxt(certidaoTratada))
            diff("Paróquia Batismo", crismando.paroquiaBatismo, paroquiaTratada)
            diff("1ª Comunhão", boolTxt(crismando.temPrimeiraComunhao), boolTxt(comunhaoTratada))

            lista
        }

        AlertDialog(
            onDismissRequest = { showConfirmacaoDialog = false },
            title = {
                Text(
                    text = "Confirmar Alterações",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (alteracoes.isEmpty()) {
                        Text("Nenhum dado foi modificado.")
                    } else {
                        Text(
                            text = "As seguintes alterações serão salvas:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        alteracoes.forEach { item ->
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val crismandoAtualizado = crismando.copy(
                            nome = nomeTratado,
                            dataNascimento = dataNascTratada,
                            telefone = telTratado,
                            nomeResponsavel = respTratado,
                            telefoneResponsavel = telRespTratado,
                            isBatizado = isBatizado,
                            certidaoBatismoEntregue = certidaoTratada,
                            paroquiaBatismo = paroquiaTratada,
                            temPrimeiraComunhao = comunhaoTratada
                        )
                        showConfirmacaoDialog = false
                        onSalvar(crismandoAtualizado)
                        onDismiss()
                    }
                ) {
                    Text(
                        text = if (alteracoes.isEmpty()) "Fechar" else "Confirmar",
                        fontWeight = FontWeight.Bold,
                        color = corDestaque
                    )
                }
            },
            dismissButton = {
                if (alteracoes.isNotEmpty()) {
                    TextButton(onClick = { showConfirmacaoDialog = false }) {
                        Text("Voltar")
                    }
                }
            }
        )
    }
}