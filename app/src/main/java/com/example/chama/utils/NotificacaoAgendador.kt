package com.example.chama.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.chama.workers.AniversarioWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object NotificacaoAgendador {

    fun agendarNotificacaoDiaria(context: Context) {
        val agora = LocalDateTime.now()
        val horarioAlvo = LocalTime.of(23, 18)
        var proximaExecucao = agora.toLocalDate().atTime(horarioAlvo)

        if (agora.isAfter(proximaExecucao)) {
            proximaExecucao = proximaExecucao.plusDays(1)
        }

        val delayInicial = Duration.between(agora, proximaExecucao).toMillis()

        val workRequest = PeriodicWorkRequestBuilder<AniversarioWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayInicial, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "verificar_aniversarios_diarios",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}