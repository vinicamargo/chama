package com.example.chama.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.chama.data.entity.Crismando
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfFichasGenerator {

    private const val PAGE_WIDTH = 595 // A4 Retrato (72 DPI)
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 20f

    fun gerarPdfFichas(
        context: Context,
        crismandos: List<Crismando>,
        dataGeracao: LocalDate = LocalDate.now()
    ): File {
        val pdfDocument = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 2 fichas por página A4
        val paginas = crismandos.sortedBy { it.nome }.chunked(2)
        val cardWidth = PAGE_WIDTH - (MARGIN * 2f)
        val cardHeight = 394f
        val gapEntreCards = 14f

        paginas.forEachIndexed { pageIndex, parCrismandos ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Ficha 1 (Superior)
            desenharFichaCard(
                canvas = canvas,
                paint = paint,
                textPaint = textPaint,
                x = MARGIN,
                y = MARGIN,
                w = cardWidth,
                h = cardHeight,
                c = parCrismandos[0],
                dataGeracao = dataGeracao
            )

            // Divisor e Ficha 2 (Inferior)
            if (parCrismandos.size > 1) {
                val separadorY = MARGIN + cardHeight + (gapEntreCards / 2f)

                paint.apply {
                    color = Color.parseColor("#D5D8DC")
                    strokeWidth = 0.8f
                    style = Paint.Style.STROKE
                }
                canvas.drawLine(MARGIN + 20f, separadorY, PAGE_WIDTH - MARGIN - 20f, separadorY, paint)

                val ficha2Y = MARGIN + cardHeight + gapEntreCards
                desenharFichaCard(
                    canvas = canvas,
                    paint = paint,
                    textPaint = textPaint,
                    x = MARGIN,
                    y = ficha2Y,
                    w = cardWidth,
                    h = cardHeight,
                    c = parCrismandos[1],
                    dataGeracao = dataGeracao
                )
            }

            pdfDocument.finishPage(page)
        }

        context.cacheDir.listFiles { _, name -> name.startsWith("fichas_crismandos_") }?.forEach { it.delete() }

        val file = File(context.cacheDir, "fichas_crismandos_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
            out.flush()
        }
        pdfDocument.close()

        return file
    }

    private fun desenharFichaCard(
        canvas: Canvas,
        paint: Paint,
        textPaint: Paint,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        c: Crismando,
        dataGeracao: LocalDate
    ) {
        val rectCard = RectF(x, y, x + w, y + h)
        val paddingHorizontal = 22f
        val col2X = x + 310f

        // Fundo e borda
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawRoundRect(rectCard, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#5B0000")
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(rectCard, 8f, 8f, paint)

        var curY = y + 20f

        // Cabeçalho Principal: Nome do crismando em evidência
        textPaint.apply {
            color = Color.parseColor("#5B0000")
            textSize = 15f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("NOME: " + c.nome.uppercase(), x + paddingHorizontal, curY, textPaint)

        // "Gerado em:" discreto no canto superior direito
        val dataHojeFormatada = dataGeracao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        textPaint.apply {
            color = Color.parseColor("#8E8E93")
            textSize = 7.5f
            isFakeBoldText = false
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Gerado em: $dataHojeFormatada", x + w - paddingHorizontal, curY, textPaint)
        textPaint.textAlign = Paint.Align.LEFT

        curY += 14f
        textPaint.apply {
            color = Color.parseColor("#616161")
            textSize = 9f
            isFakeBoldText = false
        }
        canvas.drawText("CATEQUESE DE CRISMA - FICHA CADASTRAL", x + paddingHorizontal, curY, textPaint)

        curY += 9f
        paint.apply {
            color = Color.parseColor("#5B0000")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(x + paddingHorizontal, curY, x + w - paddingHorizontal, curY, paint)

        curY += 12f

        // ========================================================
        // 1. DADOS CIVIS E PESSOAIS
        // ========================================================
        desenharFaixaSecao(canvas, paint, textPaint, x + paddingHorizontal, curY, w - (paddingHorizontal * 2f), "1. Dados Civis e Pessoais")
        curY += 40f

        val dataNascFormatada = runCatching {
            c.dataNascimento?.let {
                LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
        }.getOrNull() ?: c.dataNascimento ?: "-"

        val idadeTxt = c.idade?.let { " ($it anos)" } ?: ""
        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Nascimento:", "$dataNascFormatada$idadeTxt")
        val cpfFormatado = if (c.cpf?.length == 11) {
            "${c.cpf.substring(0, 3)}.${c.cpf.substring(3, 6)}.${c.cpf.substring(6, 9)}-${c.cpf.substring(9)}"
        } else {
            c.cpf ?: "Não informado"
        }
        desenharCampo(canvas, textPaint, col2X, curY, "CPF:", cpfFormatado)
        curY += 22f

        val naturalidadeTxt = if (!c.cidadeNascimento.isNullOrBlank() || !c.estadoNascimento.isNullOrBlank()) {
            "${c.cidadeNascimento ?: ""} - ${c.estadoNascimento ?: ""}"
        } else {
            "Não informada"
        }
        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Naturalidade:", naturalidadeTxt)
        desenharCampo(canvas, textPaint, col2X, curY, "Celular:", c.celular ?: "Não informado")
        curY += 22f

        // ========================================================
        // 2. FILIAÇÃO E RESIDÊNCIA
        // ========================================================
        desenharFaixaSecao(canvas, paint, textPaint, x + paddingHorizontal, curY, w - (paddingHorizontal * 2f), "2. Filiação e Residência")
        curY += 40f

        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Pai:", c.nomePai ?: "Não informado")
        curY += 22f
        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Mãe:", c.nomeMae ?: "Não informado")
        curY += 22f

        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Endereço:", c.endereco ?: "Não informado")
        curY += 22f
        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "CEP:", c.cep ?: "-")
        desenharCampo(canvas, textPaint, col2X, curY, "Cidade Atual:", c.cidadeAtual ?: "Santo André")
        curY += 22f

        // ========================================================
        // 3. INFORMAÇÕES SACRAMENTAIS
        // ========================================================
        desenharFaixaSecao(canvas, paint, textPaint, x + paddingHorizontal, curY, w - (paddingHorizontal * 2f), "3. Informações Sacramentais (Paroquiais)")
        curY += 40f

        val batBox = if (c.isBatizado) "[X]" else "[   ]"
        val dioBox = if (c.batizadoNaDiocese) "[X]" else "[   ]"
        val eucBox = if (c.temPrimeiraComunhao) "[X]" else "[   ]"

        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Batizado?:", batBox)
        desenharCampo(canvas, textPaint, x + 200f, curY, "Diocese de Santo André?:", dioBox)
        desenharCampo(canvas, textPaint, x + 380f, curY, "1ª Eucaristia?:", eucBox)
        curY += 22f

        val paroquiaTxt = "${c.paroquiaBatismo ?: "Não informada"}"
        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Paróquia Batismo:", paroquiaTxt)
        curY += 22f

        val certBox = if (c.certidaoBatismoEntregue) "[X]" else "[   ]"
        desenharCampo(canvas, textPaint, x + paddingHorizontal + 4f, curY, "Certidão Batismo:", certBox)
    }

    private fun desenharFaixaSecao(
        canvas: Canvas,
        paint: Paint,
        textPaint: Paint,
        x: Float,
        y: Float,
        w: Float,
        titulo: String
    ) {
        val rectFaixa = RectF(x, y, x + w, y + 17f)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F6EEEE")
        canvas.drawRoundRect(rectFaixa, 4f, 4f, paint)

        textPaint.apply {
            color = Color.parseColor("#5B0000")
            textSize = 11f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(titulo.uppercase(), x + 8f, y + 12f, textPaint)
    }

    private fun desenharCampo(
        canvas: Canvas,
        textPaint: Paint,
        x: Float,
        y: Float,
        rotulo: String,
        valor: String
    ) {
        textPaint.apply {
            color = Color.parseColor("#212121")
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(rotulo, x, y, textPaint)

        val larguraRotulo = textPaint.measureText("$rotulo ")

        textPaint.apply {
            color = Color.parseColor("#37474F")
            isFakeBoldText = false
        }
        canvas.drawText(valor, x + larguraRotulo, y, textPaint)
    }
}