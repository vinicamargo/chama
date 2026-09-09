package com.example.chama.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class NotificacaoAgendadorTest {

    private val horarioAlvo = LocalTime.of(8, 0)

    @Test
    fun `deve agendar para o mesmo dia quando agora for antes do horario alvo`() {
        // Simula 06:00 do mesmo dia
        val agora = LocalDateTime.of(2026, 9, 8, 6, 0, 0)
        val delay = NotificacaoAgendador.calcularDelayInicial(agora, horarioAlvo)

        // De 06:00 até 08:00 são exatamente 2 horas
        val duasHorasEmMs = TimeUnit.HOURS.toMillis(2)
        assertEquals(duasHorasEmMs, delay)
    }

    @Test
    fun `deve agendar para o dia seguinte quando agora for depois do horario alvo`() {
        // Simula 10:00 (já passou das 08:00)
        val agora = LocalDateTime.of(2026, 9, 8, 10, 0, 0)
        val delay = NotificacaoAgendador.calcularDelayInicial(agora, horarioAlvo)

        // De 10:00 até as 08:00 do dia seguinte são 22 horas
        val vinteEDuasHorasEmMs = TimeUnit.HOURS.toMillis(22)
        assertEquals(vinteEDuasHorasEmMs, delay)
    }

    @Test
    fun `deve agendar para o dia seguinte quando agora for exatamente o horario alvo`() {
        // Simula exatamente 08:00:00
        val agora = LocalDateTime.of(2026, 9, 8, 8, 0, 0)
        val delay = NotificacaoAgendador.calcularDelayInicial(agora, horarioAlvo)

        // Como já é o horário exato, deve programar para as 08:00 de amanhã (24h)
        val vinteEQuatroHorasEmMs = TimeUnit.HOURS.toMillis(24)
        assertEquals(vinteEQuatroHorasEmMs, delay)
    }
}