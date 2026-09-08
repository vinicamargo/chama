package com.example.chama.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chama.BuildConfig
import com.example.chama.R
import com.example.chama.data.AppDatabase
import java.time.LocalDate

class AniversarioWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val database = AppDatabase.getDatabase(context)
        val crismandoDao = database.crismandoDao()

        val hoje = if (BuildConfig.DATA_CORTE_MOCK.isNotBlank()) {
            runCatching { LocalDate.parse(BuildConfig.DATA_CORTE_MOCK) }.getOrDefault(LocalDate.now())
        } else {
            LocalDate.now()
        }

        val mesHoje = hoje.monthValue
        val diaHoje = hoje.dayOfMonth

        val todosCrismandos = crismandoDao.getAllCrismandosStatic()

        val aniversariantes = todosCrismandos.filter { crismando ->
            crismando.dataNascimento?.let {
                runCatching {
                    val nascimento = LocalDate.parse(it)
                    nascimento.monthValue == mesHoje && nascimento.dayOfMonth == diaHoje
                }.getOrDefault(false)
            } ?: false
        }

        if (aniversariantes.isNotEmpty()) {
            enviarNotificacao(context, aniversariantes.map { it.nome })
        }

        return Result.success()
    }

    private fun enviarNotificacao(context: Context, nomes: List<String>) {
        val channelId = "aniversarios_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val canal = NotificationChannel(
            channelId,
            "Aniversários de Crismandos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos diários de aniversariantes da turma"
        }
        notificationManager.createNotificationChannel(canal)

        val texto = when (nomes.size) {
            1 -> "Hoje é aniversário de ${nomes.first()}! 🎉"
            2 -> "Hoje é aniversário de ${nomes[0]} e ${nomes[1]}! 🎉"
            else -> "Hoje ${nomes.first()} e mais ${nomes.size - 1} crismandos fazem aniversário! 🎉"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_cake)
            .setColor(0xFF5B0000.toInt())
            .setContentTitle("🎂 Aniversariante do Dia!")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}