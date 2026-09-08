package com.example.chama.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

object FileUtils {

    fun gerarChaveCrismando(nome: String, dataNascimento: String?): String {
        val entrada = "${nome.trim().lowercase()}_${dataNascimento?.trim() ?: ""}"
        val bytes = MessageDigest.getInstance("MD5").digest(entrada.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun salvarFoto(
        context: Context,
        uriOrigem: Uri,
        nome: String,
        dataNascimento: String?
    ): String? {
        val chave = gerarChaveCrismando(nome, dataNascimento)
        val nomeArquivo = "perfil_${chave}.jpg"

        return runCatching {
            val pastaInterna = File(context.filesDir, "fotos_crismandos").apply { mkdirs() }
            val arquivoDestino = File(pastaInterna, nomeArquivo)

            context.contentResolver.openInputStream(uriOrigem)?.use { input ->
                FileOutputStream(arquivoDestino, false).use { output -> // false = sobrescreve
                    input.copyTo(output)
                }
            }
            arquivoDestino.absolutePath
        }.getOrNull()
    }
}