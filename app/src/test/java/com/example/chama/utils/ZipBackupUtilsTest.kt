package com.example.chama.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class ZipBackupUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var cacheDir: File
    private lateinit var filesDir: File
    private lateinit var mockContentResolver: ContentResolver

    @Before
    fun setUp() {
        cacheDir = tempFolder.newFolder("cache")
        filesDir = tempFolder.newFolder("files")

        mockContext = mockk(relaxed = true)
        mockContentResolver = mockk(relaxed = true)

        every { mockContext.cacheDir } returns cacheDir
        every { mockContext.filesDir } returns filesDir
        every { mockContext.contentResolver } returns mockContentResolver
    }

    @Test
    fun `criarZipBackup deve compactar dados csv e fotos locais com chaves corretas`() = runBlocking {
        // Cria uma foto simulada no disco temporário
        val fotoSimulada = tempFolder.newFile("foto_origem.jpg").apply {
            writeBytes("conteudo_binario_foto".toByteArray())
        }

        val conteudoCsv = "Nome,FotoUrl,BlocosRifa\nLucas,${fotoSimulada.absolutePath},1"
        val chaveUnica = "fa2d2716"
        val crismandos = listOf(
            Triple(chaveUnica, "Lucas", fotoSimulada.absolutePath)
        )

        val zipGerado = ZipBackupUtils.criarZipBackup(mockContext, conteudoCsv, crismandos)

        assertTrue("O arquivo ZIP deve existir", zipGerado.exists())
        assertTrue("O arquivo ZIP não deve estar vazio", zipGerado.length() > 0)

        // Inspeciona os arquivos contidos dentro do pacote compactado
        val entradasEncontradas = mutableListOf<String>()
        ZipInputStream(FileInputStream(zipGerado)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entradasEncontradas.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        assertTrue("Deve conter dados.csv na raiz", entradasEncontradas.contains("dados.csv"))
        assertTrue(
            "Deve conter a foto na pasta fotos/ com a chave correta",
            entradasEncontradas.contains("fotos/perfil_$chaveUnica.jpg")
        )
    }

    @Test
    fun `descompactarZipBackup deve extrair csv e fotos mapeando as chaves unicas`() = runBlocking {
        // 1. Prepara e gera um ZIP prévio com CSV e foto
        val fotoOrigem = tempFolder.newFile("avatar.jpg").apply {
            writeText("pixels_da_imagem")
        }
        val chaveEsperada = "abc12345"
        val csvOriginal = "Nome,DataNascimento\nMaria,2011-05-10"

        val zipFile = ZipBackupUtils.criarZipBackup(
            context = mockContext,
            conteudoCsv = csvOriginal,
            crismandosComFoto = listOf(Triple(chaveEsperada, "Maria", fotoOrigem.absolutePath))
        )

        val mockUri = mockk<Uri>()
        every { mockContentResolver.openInputStream(mockUri) } answers { FileInputStream(zipFile) }

        // 2. Executa a descompactação
        val (csvExtraido, mapaFotos) = ZipBackupUtils.descompactarZipBackup(mockContext, mockUri)

        // 3. Validações
        assertTrue("Arquivo CSV extraído deve existir", csvExtraido.exists())
        assertEquals(csvOriginal, csvExtraido.readText(Charsets.UTF_8))

        assertTrue("Mapa deve conter a chave unica da foto", mapaFotos.containsKey(chaveEsperada))
        val caminhoNovaFoto = mapaFotos[chaveEsperada]
        val arquivoFotoRestaurada = File(caminhoNovaFoto!!)
        assertTrue("O arquivo físico restaurado deve existir no filesDir", arquivoFotoRestaurada.exists())
        assertEquals("pixels_da_imagem", arquivoFotoRestaurada.readText())
    }

    @Test(expected = IllegalStateException::class)
    fun `descompactarZipBackup deve lancar excecao se o arquivo dados csv nao existir no zip`() {
        // Cria um ZIP corrompido/incompleto sem dados.csv
        val zipInvalido = tempFolder.newFile("corrompido.zip")
        java.util.zip.ZipOutputStream(zipInvalido.outputStream()).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("outra_coisa.txt"))
            zos.write("sem_csv".toByteArray())
            zos.closeEntry()
        }

        val mockUri = mockk<Uri>()
        every { mockContentResolver.openInputStream(mockUri) } answers { FileInputStream(zipInvalido) }

        ZipBackupUtils.descompactarZipBackup(mockContext, mockUri)
    }
}