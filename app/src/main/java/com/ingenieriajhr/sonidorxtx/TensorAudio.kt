import android.content.Context
import android.media.AudioRecord
import android.util.Log
import kotlinx.coroutines.Runnable
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.Classifications
import org.tensorflow.lite.task.core.BaseOptions

import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.thread


//fuente 1
interface TensorResultListener {  // Define una interfaz para el listener de resultados del tensor
    fun onTensorResult(results: List<String>)  // Método para recibir resultados del tensor
}

private var timer: Timer? = null  // Timer para la tarea periódica
private var audioClassifier: AudioClassifier? = null
private var audioRecord: AudioRecord? = null

class TensorAudio(val context: Context) {  // Clase TensorAudio que recibe un contexto de Android

    private var modelFileName = "model2.tflite"  // Nombre del archivo del modelo
    private val probabilityThreshold: Float = 0.80f  // Umbral de probabilidad

    lateinit var resultListener: TensorResultListener  // Variable para el listener de resultados

    fun setResListener(listener: TensorResultListener) {  // Método para establecer el listener de resultados
        this.resultListener = listener  // Asigna el listener recibido
    }

    fun initialize() {  // Método para inicializar la funcionalidad del tensor

        val baseOption = BaseOptions.builder()
        baseOption.setNumThreads(8)



        val option = AudioClassifier.AudioClassifierOptions.builder()
            .setScoreThreshold(probabilityThreshold)
            .setMaxResults(3)
            .setBaseOptions(baseOption.build())
            .build()



        // Carga el modelo seleccionado desde el archivo
        audioClassifier = AudioClassifier.createFromFileAndOptions(context, modelFileName,option)


        // Crea el tensor de entrada para audio
        val inputTensor = audioClassifier?.createInputTensorAudio()

        // Crea el grabador de audio
        audioRecord = audioClassifier?.createAudioRecord()

        timer = Timer()

        // Programa una tarea periódica
        timer?.scheduleAtFixedRate(0, 300) {
            // Inicia la grabación
            audioRecord?.startRecording()
            // Carga el tensor con los datos de audio grabados
            inputTensor?.load(audioRecord)

            // Realiza la clasificación
            val classifications = audioClassifier?.classify(inputTensor)

            // Extrae los resultados relevantes
            val output = extractMaxResults(classifications!!)

            // Notifica al listener si hay resultados significativos
            if (output.isNoEmpty()) {
                thread(start = true){
                    resultListener.onTensorResult(output)
                }
                Log.d("outputModel",output[0])
                //release
            }
        }
    }

    fun stop() {
        timer?.cancel()  // Cancela la tarea periódica
        audioRecord?.stop()  // Detiene la grabación de audio si está en curso
        audioClassifier?.close()  // Libera recursos del clasificador
    }

    private fun List<String>.isNoEmpty():Boolean{
        return this[0].isNotEmpty() && this[1].isNotEmpty()
    }
    private fun extractMaxResults(classifications: MutableList<Classifications>): List<String> {
        // Filtra las categorías con puntuación superior al umbral de probabilidad
        val filteredResults = classifications[0].categories.filter {
            it.score > probabilityThreshold
        }
        // Ordena los resultados filtrados por puntuación descendente y los convierte en cadena
        return filteredResults.sortedByDescending { it.score }
            .joinToString(separator = "\n") { "${it.label},${it.score}" }
            .split(",")
    }
}
