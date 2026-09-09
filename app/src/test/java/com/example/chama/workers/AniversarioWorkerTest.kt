package com.example.chama.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.chama.BuildConfig
import com.example.chama.data.AppDatabase
import com.example.chama.data.dao.CrismandoDao
import com.example.chama.data.entity.Crismando
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class AniversarioWorkerTest {

    private val notificationManager: NotificationManager = mockk(relaxed = true)
    private val pendingIntentMock: PendingIntent = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val crismandoDao: CrismandoDao = mockk(relaxed = true)
    private val database: AppDatabase = mockk(relaxed = true)

    private val dataReferencia: LocalDate = if (BuildConfig.DATA_CORTE_MOCK.isNotBlank()) {
        runCatching { LocalDate.parse(BuildConfig.DATA_CORTE_MOCK) }.getOrDefault(LocalDate.now())
    } else {
        LocalDate.now()
    }

    @Before
    fun setUp() {
        mockkObject(AppDatabase.Companion)
        every { AppDatabase.getDatabase(any()) } returns database
        every { database.crismandoDao() } returns crismandoDao

        mockkStatic(PendingIntent::class)
        mockkStatic(NotificationManagerCompat::class)
        // Evita RuntimeException de stub nativo ao instanciar classes do Android na JVM pura
        mockkConstructor(NotificationChannel::class)
        every { anyConstructed<NotificationChannel>().description = any() } just runs

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setFlags(any()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Boolean>()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Long>()) } returns mockk(relaxed = true)

        mockkConstructor(NotificationCompat.Builder::class)
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setColor(any()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setContentTitle(any()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setContentText(any()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setContentIntent(any()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setPriority(any()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().setAutoCancel(any()) } returns mockk(relaxed = true)
        every { anyConstructed<NotificationCompat.Builder>().build() } returns mockk(relaxed = true)

        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns pendingIntentMock

        every { context.applicationContext } returns context
        every { context.packageName } returns "com.example.chama"
        every { context.applicationInfo } returns ApplicationInfo().apply { icon = 0 }
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { notificationManager.createNotificationChannel(any<NotificationChannel>()) } just runs
        every { notificationManager.notify(any<Int>(), any()) } just runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDoWorkSemAniversariantes() = runBlocking {
        val outroDia = dataReferencia.plusDays(1)
        val crismandos = listOf(
            Crismando(
                crismandoId = 1L,
                nome = "Lucas",
                dataNascimento = "2010-${String.format(Locale.US, "%02d", outroDia.monthValue)}-${String.format(Locale.US, "%02d", outroDia.dayOfMonth)}"
            ),
            Crismando(crismandoId = 2L, nome = "Sem Data", dataNascimento = null),
            Crismando(crismandoId = 3L, nome = "Data Invalida", dataNascimento = "data-errada")
        )
        every { crismandoDao.getAllCrismandosStatic() } returns crismandos

        val worker = TestListenableWorkerBuilder<AniversarioWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun testDoWorkNotificacaoUmAniversariante() = runBlocking {
        val mesDia = "${String.format(Locale.US, "%02d", dataReferencia.monthValue)}-${String.format(Locale.US, "%02d", dataReferencia.dayOfMonth)}"
        val crismando = Crismando(
            crismandoId = 10L,
            nome = "Maria Clara",
            dataNascimento = "2009-$mesDia"
        )
        every { crismandoDao.getAllCrismandosStatic() } returns listOf(crismando)

        val worker = TestListenableWorkerBuilder<AniversarioWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun testDoWorkNotificacaoDoisAniversariantes() = runBlocking {
        val mesDia = "${String.format(Locale.US, "%02d", dataReferencia.monthValue)}-${String.format(Locale.US, "%02d", dataReferencia.dayOfMonth)}"
        val crismandos = listOf(
            Crismando(crismandoId = 20L, nome = "Lucas", dataNascimento = "2008-$mesDia"),
            Crismando(crismandoId = 21L, nome = "Ana", dataNascimento = "2009-$mesDia")
        )
        every { crismandoDao.getAllCrismandosStatic() } returns crismandos

        val worker = TestListenableWorkerBuilder<AniversarioWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun testDoWorkNotificacaoMultiplosAniversariantes() = runBlocking {
        val mesDia = "${String.format(Locale.US, "%02d", dataReferencia.monthValue)}-${String.format(Locale.US, "%02d", dataReferencia.dayOfMonth)}"
        val crismandos = listOf(
            Crismando(crismandoId = 30L, nome = "Lucas", dataNascimento = "2008-$mesDia"),
            Crismando(crismandoId = 31L, nome = "Ana", dataNascimento = "2009-$mesDia"),
            Crismando(crismandoId = 32L, nome = "Pedro", dataNascimento = "2007-$mesDia")
        )
        every { crismandoDao.getAllCrismandosStatic() } returns crismandos

        val worker = TestListenableWorkerBuilder<AniversarioWorker>(context).build()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}