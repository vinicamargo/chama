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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chama.data.entity.Crismando
import com.example.chama.utils.DataVisualTransformation
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DialogFormularioCrismando(
    crismandoParaEditar: Crismando? = null, // null = Novo | preenchido = Edição
    corDestaque: Color = MaterialTheme.colorScheme.primary,
    onSalvar: (Crismando) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdicao = crismandoParaEditar != null

    // 1. Estados dos Campos
    var nome by remember { mutableStateOf(crismandoParaEditar?.nome ?: "") }

    // Converte ISO (AAAA-MM-DD) para DDMMYYYY se for edição, permitindo a máscara correta
    var dataNascimentoDigitos by remember {
        val formatada = crismandoParaEditar?.dataNascimento?.let {
            runCatching {
                val data = LocalDate.parse(it)
                data.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
            }.getOrNull()
        } ?: ""
        mutableStateOf(formatada)
    }

    var telefone by remember { mutableStateOf(crismandoParaEditar?.celular ?: "") }
    var nomeResponsavel by remember { mutableStateOf(crismandoParaEditar?.relacionamentoResponsavel ?: "") }
    var telefoneResponsavel by remember { mutableStateOf(crismandoParaEditar?.celularResponsavel ?: "") }

    var isBatizado by remember { mutableStateOf(crismandoParaEditar?.isBatizado ?: true) }
    var certidaoEntregue by remember { mutableStateOf(crismandoParaEditar?.certidaoBatismoEntregue ?: false) }
    var paroquiaBatismo by remember { mutableStateOf(crismandoParaEditar?.paroquiaBatismo ?: "") }
    var temPrimeiraComunhao by remember { mutableStateOf(crismandoParaEditar?.temPrimeiraComunhao ?: true) }

    var showConfirmacaoDialog by remember { mutableStateOf(false) }

    // Helper para converter os dígitos em ISO ou null
    fun parseDataParaIso(digitos: String): String? {
        return runCatching {
            if (digitos.length == 8) {
                val dtf = DateTimeFormatter.ofPattern("ddMMyyyy")
                LocalDate.parse(digitos, dtf).toString()
            } else null
        }.getOrNull()
    }

    fun buildCrismando(): Crismando {
        return Crismando(
            crismandoId = crismandoParaEditar?.crismandoId ?: 0L,
            nome = nome.trim(),
            fotoUrl = crismandoParaEditar?.fotoUrl,
            dataNascimento = parseDataParaIso(dataNascimentoDigitos),
            celular = telefone.trim().ifBlank { null },
            relacionamentoResponsavel = nomeResponsavel.trim().ifBlank { null },
            celularResponsavel = telefoneResponsavel.trim().ifBlank { null },
            isBatizado = isBatizado,
            certidaoBatismoEntregue = if (isBatizado) certidaoEntregue else false,
            paroquiaBatismo = if (isBatizado) paroquiaBatismo.trim().ifBlank { null } else null,
            temPrimeiraComunhao = if (isBatizado) temPrimeiraComunhao else false
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdicao) "Editar Crismando" else "Novo Crismando",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
                    label = { Text("Nome completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dataNascimentoDigitos,
                    onValueChange = { input ->
                        dataNascimentoDigitos = input.filter { it.isDigit() }.take(8)
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
                    value = telefone,
                    onValueChange = { input ->
                        telefone = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("Telefone (apenas números)") },
                    placeholder = { Text("Ex: 11987654321") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Responsável",
                    style = MaterialTheme.typography.labelLarge,
                    color = corDestaque,
                    fontWeight = FontWeight.Bold
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
                    onValueChange = { input ->
                        telefoneResponsavel = input.filter { it.isDigit() }.take(11)
                    },
                    label = { Text("Telefone do Responsável") },
                    placeholder = { Text("Ex: 11987654321") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Vida Sacramental",
                    style = MaterialTheme.typography.labelLarge,
                    color = corDestaque,
                    fontWeight = FontWeight.Bold
                )

                // 1. Batismo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isBatizado = !isBatizado
                            if (!isBatizado) {
                                temPrimeiraComunhao = false
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

                // Campos dependentes do Batismo
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

                // 2. Primeira Comunhão
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
                        if (isEdicao) {
                            showConfirmacaoDialog = true
                        } else {
                            onSalvar(buildCrismando())
                            onDismiss()
                        }
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

    // Confirmação de alterações (somente para edição)
    if (showConfirmacaoDialog && crismandoParaEditar != null) {
        val crismandoNovo = buildCrismando()

        val alteracoes = remember(crismandoParaEditar, crismandoNovo) {
            val lista = mutableListOf<String>()
            fun diff(campo: String, antigo: Any?, novo: Any?) {
                if (antigo != novo) {
                    val txtAntigo = antigo?.toString()?.ifBlank { "Vazio" } ?: "Vazio"
                    val txtNovo = novo?.toString()?.ifBlank { "Vazio" } ?: "Vazio"
                    lista.add("• $campo: $txtAntigo ➔ $txtNovo")
                }
            }
            fun boolTxt(b: Boolean) = if (b) "Sim" else "Não"

            diff("Nome", crismandoParaEditar.nome, crismandoNovo.nome)
            diff("Nascimento", crismandoParaEditar.dataNascimento, crismandoNovo.dataNascimento)
            diff("Telefone", crismandoParaEditar.celular, crismandoNovo.celular)
            diff("Responsável", crismandoParaEditar.relacionamentoResponsavel, crismandoNovo.relacionamentoResponsavel)
            diff("Tel. Responsável", crismandoParaEditar.celularResponsavel, crismandoNovo.celularResponsavel)
            diff("Batizado", boolTxt(crismandoParaEditar.isBatizado), boolTxt(crismandoNovo.isBatizado))
            diff("Certidão Entregue", boolTxt(crismandoParaEditar.certidaoBatismoEntregue), boolTxt(crismandoNovo.certidaoBatismoEntregue))
            diff("Paróquia Batismo", crismandoParaEditar.paroquiaBatismo, crismandoNovo.paroquiaBatismo)
            diff("1ª Comunhão", boolTxt(crismandoParaEditar.temPrimeiraComunhao), boolTxt(crismandoNovo.temPrimeiraComunhao))
            lista
        }

        AlertDialog(
            onDismissRequest = { showConfirmacaoDialog = false },
            title = {
                Text(text = "Confirmar Alterações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                            Text(text = item, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmacaoDialog = false
                        onSalvar(crismandoNovo)
                        onDismiss()
                    }
                ) {
                    Text(if (alteracoes.isEmpty()) "Fechar" else "Confirmar", fontWeight = FontWeight.Bold, color = corDestaque)
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