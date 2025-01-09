package com.beginners.wavevisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaveVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var barColor: Int = Color.parseColor("#3C3C3C")
    private val paint: Paint = Paint()
    private var data = mutableListOf<Float>()
    var maxAmplitude = 600F
    var barCount = 9
    var barWidth = 10F
    var offSet = 15F
    private val defaultAmp = 30F

    private val bufferSize = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    init {

        paint.color = barColor
        clearData()
    }

    fun startRecorder(audioRecorder: AudioRecord) {
        Thread {
            val buffer = ShortArray(bufferSize)

            try {
                while (audioRecorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val readSize = audioRecorder.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        val amplitude = calculateAmplitude(buffer)
                        CoroutineScope(Dispatchers.Main).launch {
                            addData(amplitude)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

    }

    fun clearData() {
        data.clear()
        repeat(barCount) {
            data.add(barWidth * 2F)
        }

        invalidate()
    }

    fun addData(apm: Float) {
        var value = if (apm > maxAmplitude) maxAmplitude / 10 else apm / 10
//        if (value < defaultAmp) value = defaultAmp
        if (value < data[0] / 1.5F) {
            value = data[0] / 1.5F
            data[1] = data[0] / 1.25F
        }

        data.add(0, value)
        data.removeAt(data.size - 1)

        invalidate()
    }

    private fun calculateAmplitude(buffer: ShortArray): Float {
        var sum = 0f
        for (sample in buffer) {
            sum += Math.abs(sample.toFloat())
        }
        return sum / buffer.size
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2F
        val centerY = height / 2F

        var leftX = centerX - (barWidth / 2F)
        var rightX = leftX + barWidth

        var lIndex = 0
        var rIndex = 0

        for (i in 0 until data.count()) {
            val barHeight = data[i]
            var topY = centerY - barHeight
            var bottomY = centerY + barHeight

            if (i != 0) {
                //right bars
                leftX = centerX + (barWidth / 2F) + ((rIndex + 1) * offSet) + (rIndex * barWidth)
                rightX = leftX + barWidth
                topY =
                    if ((rIndex + 1) * 4F > ((barHeight / 5) * 4F)) centerY - (barWidth / 2F) /*0F*/ else (centerY - barHeight) + ((rIndex + 1) * 4F)
                bottomY =
                    if ((rIndex + 1) * 4F > ((barHeight / 5) * 4F)) centerY + (barWidth / 2F) /*0F*/ else (centerY + barHeight) - ((rIndex + 1) * 4F)

                if (bottomY - topY != 0F && bottomY - topY < barWidth / 2F /*&& i != data.size - 1*/) {
                    topY = centerY - (barWidth / 2F)
                    bottomY = centerY + (barWidth / 2F)
                }
                rIndex++
                canvas.drawRoundRect(RectF(leftX, topY, rightX, bottomY), 30F, 30F, paint)

                //left bars
                rightX = centerX - (barWidth / 2F) - ((lIndex + 1) * offSet) - (lIndex * barWidth)
                leftX = rightX - barWidth
                topY =
                    if ((lIndex + 1) * 4F > ((barHeight / 5) * 4F)) centerY - (barWidth / 2F) /*0F*/ else (centerY - barHeight) + ((lIndex + 1) * 4F)
                bottomY =
                    if ((lIndex + 1) * 4F > ((barHeight / 5) * 4F)) centerY + (barWidth / 2F) /*0F*/ else (centerY + barHeight) - ((lIndex + 1) * 4F)

                if (bottomY - topY != 0F && bottomY - topY < barWidth / 2F /*&& i != data.size - 2*/) {
                    topY = centerY - (barWidth / 2F)
                    bottomY = centerY + (barWidth / 2F)
                }
                lIndex++
                canvas.drawRoundRect(RectF(leftX, topY, rightX, bottomY), 30F, 30F, paint)

            } else {

                canvas.drawRoundRect(RectF(leftX, topY, rightX, bottomY), 30F, 30F, paint)
            }

        }


    }
}