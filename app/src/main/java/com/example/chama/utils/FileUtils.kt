package com.example.chama.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun salvarFotoLocal(context: Context, uri: Uri, crismandoId: Long): String? {
    return runCatching {
        val pastaFotos = File(context.filesDir, "fotos_crismandos").apply { mkdirs() }
        val arquivoDestino = File(pastaFotos, "crismando_${crismandoId}_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(arquivoDestino).use { output ->
                input.copyTo(output)
            }
        }
        arquivoDestino.absolutePath
    }.getOrNull()
}

fun criarUriTemporariaCamera(context: Context, crismandoId: Long): Pair<File, Uri> {
    val pastaFotos = File(context.filesDir, "fotos_crismandos").apply { mkdirs() }
    val arquivo = File(pastaFotos, "crismando_${crismandoId}_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        arquivo
    )
    return Pair(arquivo, uri)
}