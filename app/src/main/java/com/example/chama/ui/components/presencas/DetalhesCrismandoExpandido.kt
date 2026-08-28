package com.example.chama.ui.components.presencas

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.chama.data.entity.Crismando
import com.example.chama.utils.DataVisualTransformation
import com.example.chama.utils.salvarFotoLocal
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalhesCrismandoExpandido(
    modifier: Modifier = Modifier,
    crismando: Crismando,
    blocosVinculados: List<Int> = emptyList(),
    totalFaltas: Int = 0,
    totalPresentes: Int = 0,
    totalEncontrosRealizados: Int = 0,
    porcentagemPresenca: Float = 100f,
    corDestaque: Color = MaterialTheme.colorScheme.primary,
    onFechar: () -> Unit,
    onExcluir: (Crismando) -> Unit,
    onAtualizar: (Crismando) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showConfirmarExclusaoDialog by remember { mutableStateOf(false) }
    var showEditarDialog by remember { mutableStateOf(false) }
    var showOpcoesFotoDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val caminhoLocal = salvarFotoLocal(context, it, crismando.crismandoId)
            if (caminhoLocal != null) {
                onAtualizar(crismando.copy(fotoUrl = caminhoLocal))
            }
        }
    }

    @Suppress("DEPRECATION")
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result: CropImageView.CropResult ->
        if (result.isSuccessful) {
            val uriCortada: Uri? = result.uriContent ?: result.getUriFilePath(context, true)?.let { Uri.parse(it) }
            uriCortada?.let { uri ->
                val caminhoLocal = salvarFotoLocal(context, uri, crismando.crismandoId)
                if (caminhoLocal != null) {
                    onAtualizar(crismando.copy(fotoUrl = caminhoLocal))
                }
            }
        }
    }

    fun iniciarRecorte(apenasCamera: Boolean = false, apenasGaleria: Boolean = false) {
        val cropOptions = CropImageOptions(
            cropShape = CropImageView.CropShape.OVAL,
            fixAspectRatio = true,
            aspectRatioX = 1,
            aspectRatioY = 1,
            guidelines = CropImageView.Guidelines.ON,
            outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG,
            outputCompressQuality = 90,
            imageSourceIncludeCamera = !apenasGaleria,
            imageSourceIncludeGallery = !apenasCamera
        )

        cropImageLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = cropOptions
            )
        )
    }

    var nomeEdit by remember(crismando) { mutableStateOf(crismando.nome) }
    var dataNascEdit by remember(crismando) {
        val ddmmyyyy = runCatching {
            crismando.dataNascimento?.let {
                LocalDate.parse(it).format(DateTimeFormatter.ofPattern("ddMMyyyy"))
            }
        }?.getOrNull() ?: ""
        mutableStateOf(ddmmyyyy)
    }
    var telEdit by remember(crismando) { mutableStateOf(crismando.telefone ?: "") }
    var respEdit by remember(crismando) { mutableStateOf(crismando.nomeResponsavel ?: "") }
    var telRespEdit by remember(crismando) { mutableStateOf(crismando.telefoneResponsavel ?: "") }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Informações do Crismando",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { showEditarDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar Crismando",
                        tint = corDestaque
                    )
                }

                IconButton(onClick = onFechar) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar detalhes"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Avatar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(corDestaque.copy(alpha = 0.2f))
                        .clickable { showOpcoesFotoDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (!crismando.fotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = crismando.fotoUrl,
                            contentDescription = "Foto de ${crismando.nome}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = crismando.nome,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "ID: ${crismando.crismandoId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Seção: Frequência
            Text(
                text = "Frequência dos Encontros",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = corDestaque
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Presença Geral",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%.0f%%".format(porcentagemPresenca),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (porcentagemPresenca >= 75f) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Presente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalPresentes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Faltas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalFaltas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$totalEncontrosRealizados",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (porcentagemPresenca / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2E7D32),
                        trackColor = Color(0xFFC62828).copy(alpha = 0.7f),
                        drawStopIndicator = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seção: Dados Pessoais
            Text(
                text = "Dados Pessoais",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = corDestaque
            )

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Seção: Contato
            Text(
                text = "Contato",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = corDestaque
            )

            Spacer(modifier = Modifier.height(10.dp))

            ItemContatoCard(
                titulo = "Telefone do Crismando",
                telefone = crismando.telefone,
                corIcone = corDestaque,
                onLigar = { tel ->
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                    context.startActivity(intent)
                },
                onWhatsApp = { tel ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$tel"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seção: Responsável
            Text(
                text = "Responsável",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = corDestaque
            )

            Spacer(modifier = Modifier.height(10.dp))

            ItemInfoCard(
                icone = Icons.Default.FamilyRestroom,
                titulo = "Nome do Pai / Mãe",
                valor = crismando.nomeResponsavel ?: "Não informado",
                corIcone = corDestaque
            )

            Spacer(modifier = Modifier.height(8.dp))

            ItemContatoCard(
                titulo = "Telefone do Responsável",
                telefone = crismando.telefoneResponsavel,
                corIcone = corDestaque,
                onLigar = { tel ->
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                    context.startActivity(intent)
                },
                onWhatsApp = { tel ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$tel"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seção: Rifas
            Text(
                text = "Rifas Vinculadas",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = corDestaque
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = corDestaque,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (blocosVinculados.isNotEmpty())
                                "${blocosVinculados.size} bloco(s) em posse"
                            else
                                "Nenhum bloco vinculado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (blocosVinculados.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            blocosVinculados.sorted().forEach { bloco ->
                                val inicio = (bloco - 1) * 10 + 1
                                val fim = inicio + 9
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Bloco $bloco ($inicio-$fim)", style = MaterialTheme.typography.labelSmall) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = corDestaque.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Editar + Excluir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditarDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = { showConfirmarExclusaoDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excluir")
                }
            }
        }
    }

    if (showOpcoesFotoDialog) {
        Dialog(onDismissRequest = { showOpcoesFotoDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Foto de Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TextButton(
                        onClick = {
                            showOpcoesFotoDialog = false
                            iniciarRecorte(apenasCamera = true)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = corDestaque,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Tirar Foto",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            showOpcoesFotoDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = corDestaque,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Escolher da Galeria",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (!crismando.fotoUrl.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                showOpcoesFotoDialog = false
                                runCatching {
                                    val file = File(crismando.fotoUrl)
                                    if (file.exists()) file.delete()
                                }
                                onAtualizar(crismando.copy(fotoUrl = null))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Remover Foto",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { showOpcoesFotoDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }

    if (showEditarDialog) {
        AlertDialog(
            onDismissRequest = { showEditarDialog = false },
            title = {
                Text(text = "Editar Crismando", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nomeEdit,
                        onValueChange = { nomeEdit = it },
                        label = { Text("Nome completo *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dataNascEdit,
                        onValueChange = { input ->
                            dataNascEdit = input.filter { it.isDigit() }.take(8)
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
                        value = telEdit,
                        onValueChange = { input ->
                            telEdit = input.filter { it.isDigit() }.take(11)
                        },
                        label = { Text("Telefone (apenas números)") },
                        placeholder = { Text("Ex: 11987654321") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedTextField(
                        value = respEdit,
                        onValueChange = { respEdit = it },
                        label = { Text("Nome do Responsável") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = telRespEdit,
                        onValueChange = { input ->
                            telRespEdit = input.filter { it.isDigit() }.take(11)
                        },
                        label = { Text("Telefone do Responsável") },
                        placeholder = { Text("Ex: 11987654321") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nomeEdit.isNotBlank()) {
                            val dataIso = runCatching {
                                if (dataNascEdit.length == 8) {
                                    val dtf = DateTimeFormatter.ofPattern("ddMMyyyy")
                                    LocalDate.parse(dataNascEdit, dtf).toString()
                                } else {
                                    null
                                }
                            }?.getOrNull()

                            val crismandoAtualizado = crismando.copy(
                                nome = nomeEdit.trim(),
                                dataNascimento = dataIso,
                                telefone = telEdit.trim().ifBlank { null },
                                nomeResponsavel = respEdit.trim().ifBlank { null },
                                telefoneResponsavel = telRespEdit.trim().ifBlank { null }
                            )

                            onAtualizar(crismandoAtualizado)
                            showEditarDialog = false
                        }
                    }
                ) {
                    Text("Salvar Alterações", fontWeight = FontWeight.Bold, color = corDestaque)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showConfirmarExclusaoDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmarExclusaoDialog = false },
            icon = {
                Icon(
                    imageVector = if (blocosVinculados.isNotEmpty()) Icons.Default.WarningAmber else Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Excluir Crismando?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tem certeza que deseja excluir ${crismando.nome}? Todas as presenças e informações registradas serão apagadas permanentemente."
                    )

                    if (blocosVinculados.isNotEmpty()) {
                        Text(
                            text = "ATENÇÃO: Este crismando possui ${blocosVinculados.size} bloco(s) de rifa vinculados (Blocos: ${blocosVinculados.joinToString(", ")}). Ao excluí-lo, esses blocos ficarão desvinculados.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmarExclusaoDialog = false
                        onExcluir(crismando)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Excluir Definitivamente", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmarExclusaoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = corIcone,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
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
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = corIcone,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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