package com.example.chama.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Presenca
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfPresencaGenerator {

    private const val PAGE_WIDTH = 842
    private const val PAGE_HEIGHT = 595
    private const val MARGIN = 28f

    fun gerarPdfPresencas(
        context: Context,
        crismandos: List<Crismando>,
        diasComChamada: List<String>,
        todasPresencas: List<Presenca>,
        dataLimite: LocalDate = LocalDate.now(),
        tituloSub: String = "Catequese de Crisma (2026/2027)"
    ): File {
        val pdfDocument = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Formatação e ordenação das datas
        val datasIso = diasComChamada.sorted()
        val datasLocalDate = datasIso.mapNotNull {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

        val mapaPresencas = todasPresencas.associate {
            Pair(it.crismandoId, it.data) to it.estaPresente
        }

        // Agrupamento por Mês
        val mesesMap = datasLocalDate.groupBy { YearMonth.from(it) }

        // Configuração de dimensões da tabela (Sem coluna Nº)
        val colNomeWidth = 135f
        val colTotaisWidth = 26f
        val totalColsTotais = 3

        val espacoParaDatas = (PAGE_WIDTH - (MARGIN * 2)) - colNomeWidth - (colTotaisWidth * totalColsTotais)
        val colDataWidth = if (datasLocalDate.isNotEmpty()) espacoParaDatas / datasLocalDate.size else 30f

        val rowHeight = 16f
        val headerHeight = 32f

        // Paginação
        val rowsPerPage = 25
        val crismandosPaginados = crismandos.sortedBy { it.nome }.chunked(rowsPerPage)
        val totalPaginas = crismandosPaginados.size.coerceAtLeast(1)

        crismandosPaginados.forEachIndexed { pageIndex, alunosDaPagina ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = MARGIN

            // 1. Cabeçalho Principal do Relatório
            textPaint.apply {
                color = Color.parseColor("#5B0000")
                textSize = 14f
                isFakeBoldText = true
            }
            canvas.drawText("CHAMA • DIÁRIO DE PRESENÇAS", MARGIN, currentY + 12f, textPaint)

            textPaint.apply {
                color = Color.DKGRAY
                textSize = 8.5f
                isFakeBoldText = false
            }
            canvas.drawText(tituloSub, MARGIN, currentY + 24f, textPaint)

            // Metadados no canto direito
            val dataHojeFormatada = dataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Gerado em: $dataHojeFormatada", PAGE_WIDTH - MARGIN, currentY + 12f, textPaint)
            canvas.drawText("Página ${pageIndex + 1} de $totalPaginas", PAGE_WIDTH - MARGIN, currentY + 24f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            currentY += 34f

            // Linha divisória bordô
            paint.apply {
                color = Color.parseColor("#5B0000")
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, paint)
            currentY += 8f

            // 2. Cabeçalho da Tabela (Nível 1: Nome e Meses)
            val headerTop = currentY
            var currentX = MARGIN

            // Coluna Nome (ocupando o início da linha)
            drawCell(
                canvas, paint, textPaint, currentX, headerTop, colNomeWidth, headerHeight,
                bgColor = Color.parseColor("#4A0000"), textColor = Color.WHITE, text = "Nome do Crismando", isBold = true, textSize = 8f, alignLeft = true
            )
            currentX += colNomeWidth

            // Meses
            val mesFormatter = DateTimeFormatter.ofPattern("MMM/yy", Locale("pt", "BR"))
            mesesMap.forEach { (anoMes, diasDoMes) ->
                val larguraMes = colDataWidth * diasDoMes.size
                val nomeMes = anoMes.format(mesFormatter).replace(".", "").uppercase()
                val tamanhoFonteMes = if (diasDoMes.size <= 2) 5.8f else 6.8f

                drawCell(
                    canvas, paint, textPaint, currentX, headerTop, larguraMes, headerHeight / 2f,
                    bgColor = Color.parseColor("#5B0000"), textColor = Color.WHITE, text = nomeMes, isBold = true, textSize = tamanhoFonteMes
                )
                currentX += larguraMes
            }

            // Totais
            drawCell(
                canvas, paint, textPaint, currentX, headerTop, colTotaisWidth, headerHeight,
                bgColor = Color.parseColor("#5B0000"), textColor = Color.WHITE, text = "Pres.", isBold = true, textSize = 7f
            )
            currentX += colTotaisWidth

            drawCell(
                canvas, paint, textPaint, currentX, headerTop, colTotaisWidth, headerHeight,
                bgColor = Color.parseColor("#5B0000"), textColor = Color.WHITE, text = "Falt.", isBold = true, textSize = 7f
            )
            currentX += colTotaisWidth

            drawCell(
                canvas, paint, textPaint, currentX, headerTop, colTotaisWidth, headerHeight,
                bgColor = Color.parseColor("#5B0000"), textColor = Color.WHITE, text = "%", isBold = true, textSize = 7f
            )

            // Cabeçalho da Tabela (Nível 2: Dias do Domingo)
            currentX = MARGIN + colNomeWidth
            val subHeaderTop = headerTop + (headerHeight / 2f)

            datasLocalDate.forEach { data ->
                val textoDia = String.format("%02d", data.dayOfMonth)
                drawCell(
                    canvas, paint, textPaint, currentX, subHeaderTop, colDataWidth, headerHeight / 2f,
                    bgColor = Color.parseColor("#F5EBEB"), textColor = Color.DKGRAY, text = textoDia, isBold = true, textSize = 6.5f
                )
                currentX += colDataWidth
            }

            currentY += headerHeight

            // 3. Linhas com os Alunos
            alunosDaPagina.forEachIndexed { i, crismando ->
                val rowTop = currentY
                val isZebra = (i % 2 == 1)
                val rowBgColor = if (isZebra) Color.parseColor("#F9F9F9") else Color.WHITE

                var rowX = MARGIN

                // Nome
                drawCell(
                    canvas, paint, textPaint, rowX, rowTop, colNomeWidth, rowHeight,
                    bgColor = rowBgColor, textColor = Color.BLACK, text = crismando.nome, textSize = 7.5f, alignLeft = true
                )
                rowX += colNomeWidth

                var presencasCount = 0
                var faltasCount = 0

                // Células de Presença por Domingo
                datasIso.forEach { dataIso ->
                    val dataEncontro = runCatching { LocalDate.parse(dataIso) }.getOrNull()
                    val cellBg: Int
                    val cellText: String
                    val cellTextColor: Int

                    if (dataEncontro != null && dataEncontro <= dataLimite) {
                        val presente = mapaPresencas[Pair(crismando.crismandoId, dataIso)] ?: false
                        if (presente) {
                            presencasCount++
                            cellBg = Color.parseColor("#E8F5E9")
                            cellText = "•"
                            cellTextColor = Color.parseColor("#1B5E20")
                        } else {
                            faltasCount++
                            cellBg = Color.parseColor("#FFEBEE")
                            cellText = "F"
                            cellTextColor = Color.parseColor("#B71C1C")
                        }
                    } else {
                        // Data futura (em branco para impressão)
                        cellBg = rowBgColor
                        cellText = ""
                        cellTextColor = Color.TRANSPARENT
                    }

                    drawCell(
                        canvas, paint, textPaint, rowX, rowTop, colDataWidth, rowHeight,
                        bgColor = cellBg, textColor = cellTextColor, text = cellText, isBold = true, textSize = 8f
                    )
                    rowX += colDataWidth
                }

                // Totais
                val totalPassado = presencasCount + faltasCount
                val perc = if (totalPassado > 0) ((presencasCount.toFloat() / totalPassado) * 100).toInt() else 100

                drawCell(
                    canvas, paint, textPaint, rowX, rowTop, colTotaisWidth, rowHeight,
                    bgColor = Color.parseColor("#F0F0F0"), textColor = Color.DKGRAY, text = "$presencasCount", textSize = 7.5f, isBold = true
                )
                rowX += colTotaisWidth

                drawCell(
                    canvas, paint, textPaint, rowX, rowTop, colTotaisWidth, rowHeight,
                    bgColor = Color.parseColor("#F0F0F0"), textColor = if (faltasCount > 3) Color.RED else Color.DKGRAY, text = "$faltasCount", textSize = 7.5f, isBold = true
                )
                rowX += colTotaisWidth

                drawCell(
                    canvas, paint, textPaint, rowX, rowTop, colTotaisWidth, rowHeight,
                    bgColor = Color.parseColor("#F0F0F0"), textColor = Color.BLACK, text = "$perc%", textSize = 7f
                )

                currentY += rowHeight
            }

            // 4. Rodapé da Folha (Legenda alinhada à direita sem visto)
            val footerY = PAGE_HEIGHT - 26f
            textPaint.apply {
                color = Color.DKGRAY
                textSize = 7.5f
                isFakeBoldText = false
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Legenda:  [ • ] Presente    [ F ] Falta    [   ] Futuro / Em branco", PAGE_WIDTH - MARGIN, footerY, textPaint)
            textPaint.textAlign = Paint.Align.LEFT

            pdfDocument.finishPage(page)
        }

        val file = File(context.cacheDir, "diario_presencas_${dataLimite}.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    private fun drawCell(
        canvas: Canvas,
        paint: Paint,
        textPaint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        bgColor: Int,
        textColor: Int,
        text: String,
        isBold: Boolean = false,
        textSize: Float = 7.5f,
        alignLeft: Boolean = false
    ) {
        val rect = RectF(x, y, x + width, y + height)

        paint.style = Paint.Style.FILL
        paint.color = bgColor
        canvas.drawRect(rect, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#B0BEC5")
        paint.strokeWidth = 0.6f
        canvas.drawRect(rect, paint)

        if (text.isNotBlank()) {
            textPaint.apply {
                color = textColor
                this.textSize = textSize
                isFakeBoldText = isBold
                textAlign = if (alignLeft) Paint.Align.LEFT else Paint.Align.CENTER
            }

            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val textY = y + (height / 2f) + (textBounds.height() / 2f) - 1.5f

            if (alignLeft) {
                val padding = 5f
                val maxWidth = width - (padding * 2)
                val textoAjustado = if (textPaint.measureText(text) > maxWidth) {
                    var t = text
                    while (t.isNotEmpty() && textPaint.measureText("$t...") > maxWidth) {
                        t = t.dropLast(1)
                    }
                    "$t..."
                } else {
                    text
                }
                canvas.drawText(textoAjustado, x + padding, textY, textPaint)
            } else {
                canvas.drawText(text, x + (width / 2f), textY, textPaint)
            }
        }
    }
}