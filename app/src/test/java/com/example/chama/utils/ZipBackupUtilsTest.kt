package com.example.chama.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
    fun `criarZipBackup deve ignorar referencias de foto nulas, vazias, em branco ou inexistentes`() = runBlocking {
        val conteudoCsv = "Nome,FotoUrl\nJoão,"
        val crismandos = listOf(
            Triple("c1", "João", null),
            Triple("c2", "Maria", ""),
            Triple("c3", "Pedro", "   "),
            Triple("c4", "Ana", "/caminho/arquivo/inexistente.jpg")
        )

        val zipGerado = ZipBackupUtils.criarZipBackup(mockContext, conteudoCsv, crismandos)

        val entradasEncontradas = mutableListOf<String>()
        ZipInputStream(FileInputStream(zipGerado)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entradasEncontradas.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        assertEquals(1, entradasEncontradas.size)
        assertTrue(entradasEncontradas.contains("dados.csv"))
    }

    @Test
    fun `criarZipBackup deve incluir fotos vindas de content Uri`() = runBlocking {
        mockkStatic(Uri::class)
        try {
            val contentUriStr = "content://media/external/images/media/99"
            val mockUri = mockk<Uri>()
            every { Uri.parse(contentUriStr) } returns mockUri
            every { mockContentResolver.openInputStream(mockUri) } answers {
                ByteArrayInputStream("conteudo_content_uri".toByteArray())
            }

            val conteudoCsv = "Nome,FotoUrl\nFotoContent,$contentUriStr"
            val crismandos = listOf(
                Triple("chave_content", "FotoContent", contentUriStr)
            )

            val zipGerado = ZipBackupUtils.criarZipBackup(mockContext, conteudoCsv, crismandos)

            val entradasEncontradas = mutableListOf<String>()
            ZipInputStream(FileInputStream(zipGerado)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    entradasEncontradas.add(entry.name)
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            assertTrue(entradasEncontradas.contains("fotos/perfil_chave_content.jpg"))
        } finally {
            unmockkStatic(Uri::class)
        }
    }

    @Test
    fun `criarZipBackup deve sobrescrever arquivo zip antigo se ja existir no cacheDir`() = runBlocking {
        val zipAntigo = File(cacheDir, "backup_geral_chama.zip").apply {
            writeText("conteudo_antigo_invalido")
        }
        assertTrue(zipAntigo.exists())

        val zipNovo = ZipBackupUtils.criarZipBackup(mockContext, "Nome\nTeste", emptyList())

        assertTrue(zipNovo.exists())
        val entradasEncontradas = mutableListOf<String>()
        ZipInputStream(FileInputStream(zipNovo)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entradasEncontradas.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        assertTrue(entradasEncontradas.contains("dados.csv"))
    }

    @Test
    fun `criarZipBackup deve ignorar foto com URL http inacessivel sem lancar excecao`() = runBlocking {
        val httpUrl = "http://127.0.0.1:65535/foto.jpg"
        val crismandos = listOf(
            Triple("chave_http", "HttpUser", httpUrl)
        )

        val zipGerado = ZipBackupUtils.criarZipBackup(mockContext, "Nome\nHttpUser", crismandos)

        assertTrue(zipGerado.exists())
        val entradasEncontradas = mutableListOf<String>()
        ZipInputStream(FileInputStream(zipGerado)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entradasEncontradas.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        assertTrue(entradasEncontradas.contains("dados.csv"))
        assertFalse(entradasEncontradas.contains("fotos/perfil_chave_http.jpg"))
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

    @Test
    fun `descompactarZipBackup deve ignorar diretorios, arquivos irrelevantes e chaves em branco`() = runBlocking {
        val zipCompleto = tempFolder.newFile("zip_test.zip")
        ZipOutputStream(zipCompleto.outputStream()).use { zos ->
            // dados.csv
            zos.putNextEntry(ZipEntry("dados.csv"))
            zos.write("Nome\nCarlos".toByteArray())
            zos.closeEntry()

            // entrada que é um diretório fotos/
            zos.putNextEntry(ZipEntry("fotos/"))
            zos.closeEntry()

            // foto válida
            zos.putNextEntry(ZipEntry("fotos/perfil_k123.jpg"))
            zos.write("foto_bytes".toByteArray())
            zos.closeEntry()

            // foto com chave em branco (ex: perfil_.jpg)
            zos.putNextEntry(ZipEntry("fotos/perfil_.jpg"))
            zos.write("foto_sem_chave".toByteArray())
            zos.closeEntry()

            // outro arquivo irrelevante na raiz
            zos.putNextEntry(ZipEntry("relatorio.pdf"))
            zos.write("pdf_bytes".toByteArray())
            zos.closeEntry()
        }

        val mockUri = mockk<Uri>()
        every { mockContentResolver.openInputStream(mockUri) } answers { FileInputStream(zipCompleto) }

        val (csvFile, mapaFotos) = ZipBackupUtils.descompactarZipBackup(mockContext, mockUri)

        assertTrue(csvFile.exists())
        assertEquals("Nome\nCarlos", csvFile.readText())

        assertEquals(1, mapaFotos.size)
        assertTrue(mapaFotos.containsKey("k123"))
        assertFalse(mapaFotos.containsKey(""))
    }

    @Test(expected = IllegalStateException::class)
    fun `descompactarZipBackup deve lancar excecao se o arquivo dados csv nao existir no zip`() {
        // Cria um ZIP corrompido/incompleto sem dados.csv
        val zipInvalido = tempFolder.newFile("corrompido.zip")
        ZipOutputStream(zipInvalido.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("outra_coisa.txt"))
            zos.write("sem_csv".toByteArray())
            zos.closeEntry()
        }

        val mockUri = mockk<Uri>()
        every { mockContentResolver.openInputStream(mockUri) } answers { FileInputStream(zipInvalido) }

        ZipBackupUtils.descompactarZipBackup(mockContext, mockUri)
    }

    @Test(expected = IllegalStateException::class)
    fun `descompactarZipBackup deve lancar excecao quando openInputStream retornar nulo`() {
        val mockUri = mockk<Uri>()
        every { mockContentResolver.openInputStream(mockUri) } returns null

        ZipBackupUtils.descompactarZipBackup(mockContext, mockUri)
    }
}