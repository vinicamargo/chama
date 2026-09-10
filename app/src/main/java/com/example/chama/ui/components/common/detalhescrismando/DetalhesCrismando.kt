package com.example.chama.ui.components.common.detalhescrismando

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.chama.ui.components.common.detalhescrismando.sections.SecaoDadosContato
import com.example.chama.ui.components.common.detalhescrismando.sections.SecaoFrequencia
import com.example.chama.ui.components.common.detalhescrismando.sections.SecaoRifasVinculadas
import com.example.chama.ui.components.common.detalhescrismando.sections.SecaoSacramentos
import com.example.chama.utils.FileUtils
import java.io.File
import java.time.LocalDate

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

    // Estados de expansão/compressão de cada seção
    var expandirFrequencia by remember { mutableStateOf(false) }
    var expandirSacramentos by remember { mutableStateOf(false) }
    var expandirContatos by remember { mutableStateOf(true) }
    var expandirRifas by remember { mutableStateOf(false) }

    // Diálogos
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

            // Avatar & Nome
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(135.dp)
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
                            modifier = Modifier.size(68.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = crismando.nome,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SecaoDadosContato(
                crismando = crismando,
                corDestaque = corDestaque,
                isExpandido = expandirContatos,
                onToggleExpandir = { expandirContatos = !expandirContatos }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SecaoFrequencia(
                totalPresentes = totalPresentes,
                totalFaltas = totalFaltas,
                totalEncontrosRealizados = totalEncontrosRealizados,
                porcentagemPresenca = porcentagemPresenca,
                corDestaque = corDestaque,
                isExpandido = expandirFrequencia,
                onToggleExpandir = { expandirFrequencia = !expandirFrequencia }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SecaoSacramentos(
                crismando = crismando,
                corDestaque = corDestaque,
                isExpandido = expandirSacramentos,
                onToggleExpandir = { expandirSacramentos = !expandirSacramentos }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            SecaoRifasVinculadas(
                blocosVinculados = blocosVinculados,
                corDestaque = corDestaque,
                isExpandido = expandirRifas,
                onToggleExpandir = { expandirRifas = !expandirRifas },
                onAbrirVincular = { showVincularBlocoDialog = true },
                onClicarBloco = { blocoItem -> blocoSelecionadoParaAcoes = blocoItem }
            )

            Spacer(modifier = Modifier.height(20.dp))

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