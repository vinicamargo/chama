package com.example.chama.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipBackupUtils {

    suspend fun criarZipBackup(
        context: Context,
        conteudoCsv: String,
        crismandosComFoto: List<Triple<String, String, String?>> // ChaveUnica, Nome, FotoRef
    ): File = withContext(Dispatchers.IO) {
        val zipFile = File(context.cacheDir, "backup_geral_chama.zip")
        if (zipFile.exists()) zipFile.delete()

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // 1. Grava o CSV na raiz
            zos.putNextEntry(ZipEntry("dados.csv"))
            zos.write(conteudoCsv.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 2. Grava as fotos nomeadas pela chave estável (ex: fotos/perfil_abc123.jpg)
            for ((chaveUnica, _, fotoRef) in crismandosComFoto) {
                if (fotoRef.isNullOrBlank()) continue

                val streamFoto = abrirStreamFoto(context, fotoRef)
                if (streamFoto != null) {
                    streamFoto.use { input ->
                        zos.putNextEntry(ZipEntry("fotos/perfil_$chaveUnica.jpg"))
                        input.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
        }
        zipFile
    }

    private fun abrirStreamFoto(context: Context, origem: String): InputStream? {
        return runCatching {
            when {
                origem.startsWith("http://") || origem.startsWith("https://") -> {
                    val conn = (URL(origem).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                    }
                    if (conn.responseCode == HttpURLConnection.HTTP_OK) conn.inputStream else null
                }
                origem.startsWith("content://") -> {
                    context.contentResolver.openInputStream(Uri.parse(origem))
                }
                else -> {
                    val file = File(origem)
                    if (file.exists() && file.isFile) FileInputStream(file) else null
                }
            }
        }.getOrNull()
    }

    fun descompactarZipBackup(
        context: Context,
        uriZip: Uri
    ): Pair<File, Map<String, String>> {
        val pastaFotosDestino = File(context.filesDir, "fotos_crismandos").apply { mkdirs() }
        val pastaCacheTemp = File(context.cacheDir, "temp_unzip_${System.currentTimeMillis()}").apply { mkdirs() }
        var arquivoCsvExtraido: File? = null
        val mapaNovosCaminhosFotos = mutableMapOf<String, String>()

        context.contentResolver.openInputStream(uriZip)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                // Dentro do laço de leitura do ZipInputStream:
                while (entry != null) {
                    val nome = entry.name

                    if (nome == "dados.csv") {
                        val csvTemp = File(pastaCacheTemp, "dados.csv")

                        val destinoDirCanonical = pastaCacheTemp.canonicalPath
                        val arquivoDestinoCanonical = csvTemp.canonicalPath
                        if (!arquivoDestinoCanonical.startsWith(destinoDirCanonical + File.separator)) {
                            throw SecurityException("Entrada ZIP inválida tentando sair do diretório: $nome")
                        }

                        FileOutputStream(csvTemp).use { fos -> zis.copyTo(fos) }
                        arquivoCsvExtraido = csvTemp
                    } else if (nome.startsWith("fotos/") && !entry.isDirectory) {
                        val nomeArquivoDestino = File(nome).name
                        val arquivoFotoDestino = File(pastaFotosDestino, nomeArquivoDestino)

                        val destinoDirCanonical = pastaFotosDestino.canonicalPath
                        val arquivoDestinoCanonical = arquivoFotoDestino.canonicalPath
                        if (!arquivoDestinoCanonical.startsWith(destinoDirCanonical + File.separator)) {
                            throw SecurityException("Tentativa de Zip Slip detectada na foto: $nome")
                        }

                        FileOutputStream(arquivoFotoDestino).use { fos -> zis.copyTo(fos) }

                        val chave = nomeArquivoDestino
                            .removePrefix("perfil_")
                            .substringBeforeLast(".")
                        if (chave.isNotBlank()) {
                            mapaNovosCaminhosFotos[chave] = arquivoFotoDestino.absolutePath
                        }
                    }

                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        return Pair(
            arquivoCsvExtraido ?: throw IllegalStateException("Arquivo dados.csv não encontrado no ZIP"),
            mapaNovosCaminhosFotos
        )
    }
}