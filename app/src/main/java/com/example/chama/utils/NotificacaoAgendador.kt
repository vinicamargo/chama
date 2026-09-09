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

    // Regra pura: calcula quantos milissegundos faltam para a próxima execução
    fun calcularDelayInicial(
        agora: LocalDateTime,
        horarioAlvo: LocalTime = LocalTime.of(8, 0)
    ): Long {
        var proximaExecucao = agora.toLocalDate().atTime(horarioAlvo)

        if (agora.isAfter(proximaExecucao) || agora.isEqual(proximaExecucao)) {
            proximaExecucao = proximaExecucao.plusDays(1)
        }

        return Duration.between(agora, proximaExecucao).toMillis()
    }

    fun agendarNotificacaoDiaria(context: Context) {
        val delayInicial = calcularDelayInicial(
            agora = LocalDateTime.now(),
            horarioAlvo = LocalTime.of(8, 0)
        )

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