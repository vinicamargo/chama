package com.example.chama.utils

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DataVisualTransformationTest {

    private lateinit var transformation: DataVisualTransformation

    @Before
    fun setUp() {
        transformation = DataVisualTransformation()
    }

    @Test
    fun `deve formatar texto com 8 digitos adicionando barras corretamente`() {
        val entrada = AnnotatedString("15032011")
        val resultado = transformation.filter(entrada)

        assertEquals("15/03/2011", resultado.text.text)
    }

    @Test
    fun `deve formatar textos parciais durante a digitacao`() {
        assertEquals("", transformation.filter(AnnotatedString("")).text.text)
        assertEquals("1", transformation.filter(AnnotatedString("1")).text.text)
        assertEquals("15/", transformation.filter(AnnotatedString("15")).text.text)
        assertEquals("15/0", transformation.filter(AnnotatedString("150")).text.text)
        assertEquals("15/03/", transformation.filter(AnnotatedString("1503")).text.text)
        assertEquals("15/03/20", transformation.filter(AnnotatedString("150320")).text.text)
    }

    @Test
    fun `deve truncar quando o texto tiver mais de 8 digitos`() {
        val entrada = AnnotatedString("1503201199999")
        val resultado = transformation.filter(entrada)

        assertEquals("15/03/2011", resultado.text.text)
    }

    @Test
    fun `deve mapear offset original para transformado corretamente`() {
        val resultado = transformation.filter(AnnotatedString("15032011"))
        val mapper = resultado.offsetMapping

        // Índices de 0 a 1 (dia antes da barra): mapeia 1 para 1
        assertEquals(0, mapper.originalToTransformed(0))
        assertEquals(1, mapper.originalToTransformed(1))

        // Índices 2 e 3 (mês após a primeira barra): soma +1
        assertEquals(3, mapper.originalToTransformed(2))
        assertEquals(4, mapper.originalToTransformed(3))

        // Índices 4 a 8 (ano após a segunda barra): soma +2
        assertEquals(6, mapper.originalToTransformed(4))
        assertEquals(10, mapper.originalToTransformed(8))

        // Offsets maiores que 8 travam no final da máscara (10)
        assertEquals(10, mapper.originalToTransformed(9))
        assertEquals(10, mapper.originalToTransformed(99))
    }

    @Test
    fun `deve mapear offset transformado para original corretamente`() {
        val resultado = transformation.filter(AnnotatedString("15032011"))
        val mapper = resultado.offsetMapping

        // Até o índice 2 (incluindo primeira barra): offset original é mantido
        assertEquals(0, mapper.transformedToOriginal(0))
        assertEquals(2, mapper.transformedToOriginal(2))

        // Índices de 3 a 5 (mês + segunda barra): subtrai 1
        assertEquals(2, mapper.transformedToOriginal(3))
        assertEquals(4, mapper.transformedToOriginal(5))

        // Índices de 6 a 10 (ano): subtrai 2
        assertEquals(4, mapper.transformedToOriginal(6))
        assertEquals(8, mapper.transformedToOriginal(10))

        // Offsets maiores que 10 travam no comprimento máximo do texto original (8)
        assertEquals(8, mapper.transformedToOriginal(11))
        assertEquals(8, mapper.transformedToOriginal(50))
    }
}