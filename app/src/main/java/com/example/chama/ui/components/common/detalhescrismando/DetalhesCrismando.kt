package com.example.chama.ui.components.common.detalhescrismando

import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.chama.data.entity.Crismando
import com.example.chama.ui.MainViewModel
import com.example.chama.ui.StatusBlocoRifa
import com.example.chama.ui.VinculoBlocoResult
import com.example.chama.ui.components.common.detalhescrismando.dialogs.DialogAcoesBloco
import com.example.chama.ui.components.common.detalhescrismando.dialogs.DialogEditarCrismando
import com.example.chama.ui.components.common.detalhescrismando.dialogs.DialogExclusaoCrismando
import com.example.chama.ui.components.common.detalhescrismando.dialogs.DialogOpcoesFoto
import com.example.chama.ui.components.common.detalhescrismando.dialogs.DialogVincularBloco
import com.example.chama.utils.FileUtils
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class BlocoItemUI(
    val numero: Int,
    val estaPago: Boolean
)

@Composable
fun DetalhesCrismandoContainer(
    crismando: Crismando,
    viewModel: MainViewModel,
    onFechar: () -> Unit,
    modifier: Modifier = Modifier,
    onExcluidoComSucesso: () -> Unit = onFechar
) {
    val listaRifas by viewModel.listaRifas.collectAsState()
    val diasComChamada by viewModel.diasComChamada.collectAsState()
    val todasPresencas by viewModel.todasPresencas.collectAsState()

    val blocos = remember(listaRifas, crismando) {
        listaRifas
            .filter { it.vendedorId == crismando.crismandoId }
            .groupBy { it.bloco }
            .map { (bloco, rifas) -> BlocoItemUI(numero = bloco, estaPago = rifas.all { it.estaPaga }) }
            .sortedBy { it.numero }
    }

    val dataDeHoje = viewModel.dataDeHoje
    val datasAteHoje = remember(diasComChamada) {
        diasComChamada.filter { runCatching { LocalDate.parse(it) <= dataDeHoje }.getOrDefault(false) }
    }

    val presencasDoCrismando = remember(todasPresencas, crismando, datasAteHoje) {
        todasPresencas.filter { it.crismandoId == crismando.crismandoId && it.data in datasAteHoje }
    }

    val totalEncontros = datasAteHoje.size
    val totalPresentes = presencasDoCrismando.count { it.estaPresente }
    val totalFaltas = totalEncontros - totalPresentes
    val porcentagem = if (totalEncontros > 0) (totalPresentes.toFloat() / totalEncontros) * 100f else 100f

    DetalhesCrismando(
        modifier = modifier,
        crismando = crismando,
        blocosVinculados = blocos,
        totalFaltas = totalFaltas,
        totalPresentes = totalPresentes,
        totalEncontrosRealizados = totalEncontros,
        porcentagemPresenca = porcentagem,
        onFechar = onFechar,
        onExcluir = { c ->
            viewModel.excluirCrismando(c.crismandoId)
            onExcluidoComSucesso()
        },
        onAtualizar = { crismandoAtualizado ->
            viewModel.atualizarCrismando(crismandoAtualizado)
        },
        onVerificarBloco = { bloco ->
            viewModel.checarStatusBloco(bloco, crismando.crismandoId)
        },
        onVincularBloco = { crismandoId, numeroBloco, onError, onSuccess ->
            viewModel.vincularVendedorAoBloco(crismandoId, numeroBloco) { resultado ->
                when (resultado) {
                    is VinculoBlocoResult.Sucesso -> onSuccess()
                    is VinculoBlocoResult.BlocoOcupado -> onError("Bloco em posse de ${resultado.nomeResponsavel}")
                    is VinculoBlocoResult.BlocoInexistente -> {
                        val msg = if (resultado.ultimoBloco > 0) {
                            "Último bloco existente: ${resultado.ultimoBloco}"
                        } else {
                            "Não há rifas cadastradas"
                        }
                        onError(msg)
                    }
                    is VinculoBlocoResult.ErroGenerico -> onError(resultado.mensagem)
                }
            }
        },
        onDesvincularBloco = { bloco ->
            viewModel.desvincularVendedorDoBloco(bloco)
        },
        onAlternarPagamentoBloco = { bloco, _ ->
            val rifaExemplo = listaRifas.firstOrNull { it.bloco == bloco }
            if (rifaExemplo != null) {
                viewModel.alternarPagamentoRifa(rifaExemplo)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalhesCrismando(
    modifier: Modifier = Modifier,
    crismando: Crismando,
    blocosVinculados: List<BlocoItemUI> = emptyList(),
    totalFaltas: Int = 0,
    totalPresentes: Int = 0,
    totalEncontrosRealizados: Int = 0,
    porcentagemPresenca: Float = 100f,
    corDestaque: Color = MaterialTheme.colorScheme.primary,
    onFechar: () -> Unit,
    onExcluir: (Crismando) -> Unit,
    onAtualizar: (Crismando) -> Unit,
    onVerificarBloco: suspend (numeroBloco: Int) -> StatusBlocoRifa = { StatusBlocoRifa.Disponivel },
    onVincularBloco: (crismandoId: Long, numeroBloco: Int, onError: (String) -> Unit, onSuccess: () -> Unit) -> Unit = { _, _, _, _ -> },
    onDesvincularBloco: (numeroBloco: Int) -> Unit = { _ -> },
    onAlternarPagamentoBloco: (numeroBloco: Int, estaPagoAtual: Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showConfirmarExclusaoDialog by remember { mutableStateOf(false) }
    var showEditarDialog by remember { mutableStateOf(false) }
    var showOpcoesFotoDialog by remember { mutableStateOf(false) }
    var showVincularBlocoDialog by remember { mutableStateOf(false) }
    var blocoSelecionadoParaAcoes by remember { mutableStateOf<BlocoItemUI?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val caminhoPermanente = FileUtils.salvarFoto(
                context = context,
                uriOrigem = uri,
                nome = crismando.nome,
                dataNascimento = crismando.dataNascimento
            )
            if (caminhoPermanente != null) {
                onAtualizar(crismando.copy(fotoUrl = caminhoPermanente))
            }
        }
    }

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result: CropImageView.CropResult ->
        if (result.isSuccessful) {
            val uriCortada: Uri? = result.uriContent ?: result.getUriFilePath(context, true)?.let { Uri.parse(it) }
            uriCortada?.let { uri ->
                val caminhoPermanente = FileUtils.salvarFoto(
                    context = context,
                    uriOrigem = uri,
                    nome = crismando.nome,
                    dataNascimento = crismando.dataNascimento
                )
                if (caminhoPermanente != null) {
                    onAtualizar(crismando.copy(fotoUrl = caminhoPermanente))
                }
            }
        }
    }

    fun iniciarRecorte(apenasCamera: Boolean = false, apenasGaleria: Boolean = false) {
        cropImageLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    cropShape = CropImageView.CropShape.OVAL,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressFormat = Bitmap.CompressFormat.JPEG,
                    outputCompressQuality = 90,
                    imageSourceIncludeCamera = !apenasGaleria,
                    imageSourceIncludeGallery = !apenasCamera
                )
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                IconButton(onClick = onFechar) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar detalhes")
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
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Frequência
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

            // Dados Pessoais
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

            // Contato
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

            // Responsável
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

            // Rifas Vinculadas
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rifas Vinculadas",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = corDestaque
                )

                TextButton(onClick = { showVincularBlocoDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = corDestaque)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vincular Bloco", color = corDestaque, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                            blocosVinculados.forEach { blocoItem ->
                                val inicio = (blocoItem.numero - 1) * 10 + 1
                                val fim = inicio + 9
                                val labelTexto = if (blocoItem.estaPago) {
                                    "Bloco ${blocoItem.numero} ($inicio-$fim) • Pago"
                                } else {
                                    "Bloco ${blocoItem.numero} ($inicio-$fim)"
                                }

                                InputChip(
                                    selected = blocoItem.estaPago,
                                    onClick = { blocoSelecionadoParaAcoes = blocoItem },
                                    label = {
                                        Text(
                                            text = labelTexto,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (blocoItem.estaPago) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        containerColor = corDestaque.copy(alpha = 0.15f),
                                        selectedContainerColor = Color(0xFF2E7D32).copy(alpha = 0.18f),
                                        selectedLabelColor = Color(0xFF1B5E20)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botões Editar / Excluir
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excluir")
                }
            }
        }
    }

    if (showVincularBlocoDialog) {
        DialogVincularBloco(
            nomeCrismando = crismando.nome,
            crismandoId = crismando.crismandoId,
            corDestaque = corDestaque,
            onVerificarBloco = onVerificarBloco,
            onDismiss = { showVincularBlocoDialog = false }
        )
    }

    blocoSelecionadoParaAcoes?.let { blocoItem ->
        DialogAcoesBloco(
            blocoItem = blocoItem,
            nomeCrismando = crismando.nome,
            corDestaque = corDestaque,
            onDismiss = { blocoSelecionadoParaAcoes = null },
            onAlternarPagamento = onAlternarPagamentoBloco,
            onDesvincular = onDesvincularBloco
        )
    }

    if (showOpcoesFotoDialog) {
        DialogOpcoesFoto(
            hasFoto = !crismando.fotoUrl.isNullOrBlank(),
            corDestaque = corDestaque,
            onTirarFoto = { iniciarRecorte(apenasCamera = true) },
            onEscolherGaleria = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoverFoto = {
                runCatching {
                    val fotoUrl = crismando.fotoUrl
                    if (fotoUrl != null) {
                        val uri = Uri.parse(fotoUrl)
                        if (uri.scheme == "content") {
                            context.contentResolver.delete(uri, null, null)
                        } else {
                            val file = File(fotoUrl)
                            if (file.exists()) file.delete()
                        }
                    }
                }
                onAtualizar(crismando.copy(fotoUrl = null))
            },
            onDismiss = { showOpcoesFotoDialog = false }
        )
    }

    if (showEditarDialog) {
        DialogEditarCrismando(
            crismando = crismando,
            corDestaque = corDestaque,
            onSalvar = onAtualizar,
            onDismiss = { showEditarDialog = false }
        )
    }

    if (showConfirmarExclusaoDialog) {
        DialogExclusaoCrismando(
            nomeCrismando = crismando.nome,
            blocosVinculados = blocosVinculados.map { it.numero },
            onConfirmarExclusao = { onExcluir(crismando) },
            onDismiss = { showConfirmarExclusaoDialog = false }
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