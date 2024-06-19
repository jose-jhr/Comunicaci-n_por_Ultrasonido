**Comunicación por ultrasonido.**

Video de fredy vega explicando la complejidad del virus Stuxnet.

<a href="https://youtu.be/gwEq0-ACUr8?si=C6XIkI40u1sTYTNc&t=730">
  <img src="https://static.platzi.com/blog/uploads/2016/09/freddy-vega.jpg" width="450">
</a>



Aunque en las investigaciones que se hicieron en este canal no se encontro que la forma de comunicación y dispersión del virus
fue el ultrasonido, se encontro que si existio un virus llamado **BadBios**

Para dar solución a esta tarea que es emplear el sonido para la comunicación entre dos dispositivos realizaremos las siguientes tareas.

posible error en Android. 

```
implementation 'androidx.appcompat:appcompat:1.5.0'
```

Implementación en buil.gradle, para hacer referencia a la vistas en xml.
```
 buildFeatures{
        viewBinding = true
    }
```

**1) Implementar la libreria tensorflow task audio**

Esta biblioteca permite utilizar modelos de TensorFlow Lite específicamente diseñados para tareas de procesamiento de audio en aplicaciones móviles.
```
  //tensorflow audio task
  implementation 'org.tensorflow:tensorflow-lite-task-audio:0.2.0'
```
**2) Ahora vamos a implementar el modelo generado, puedes usar mi modelo.**

**Mi modelo:** https://cienciayculturacreativa.com/2024/comunicacion_ultrasonido/modelsound10.tflite

**Etiquetas:** https://cienciayculturacreativa.com/2024/comunicacion_ultrasonido/labels.txt

**Proyecto Teachable Machine Ejemplo:** https://cienciayculturacreativa.com/2024/comunicacion_ultrasonido/project.tm


**3) Ahora vamos a implementar la clase que sera encargada de clasificar las señales de audio.**

```kotlin 

import android.content.Context
import android.provider.MediaStore.Audio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.Classifications
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate


enum class OPTION_MODEL{
    SONIDOS
}

interface InterfaceResultTensor{
    fun resultGeneric(resultsTensor: List<String>)
}

class TensorAudio(val context: Context) {

    /**
     * Patch models
     */
    private var modelTfLite = "modelsound10.tflite"

    // TODO 2.2: defining the minimum threshold
    private val probabilityThreshold: Float = 0.4f

    //lateinit interface usage return result
    lateinit var interfaceResultTensor: InterfaceResultTensor

    fun addInterfaceResultTensor(interfaceResultTensor: InterfaceResultTensor){
        this.interfaceResultTensor = interfaceResultTensor
    }


    fun initConfiguracion(){

        //TODO 1.0 get model, with option model.
        val classifierGeneric = AudioClassifier.createFromFile(context,modelTfLite)

        //TODO 2.0: Creating an audio recorder
        val tensorGeneric = classifierGeneric.createInputTensorAudio()

        //TODO 3.0 CREATING RECORD
        val recordGeneric = classifierGeneric.createAudioRecord()

        //TODO 4.0 START RECORDING
        recordGeneric.startRecording()

        //TODO INIT LISTEN delay is wait separate 1 milisecond and sample rate 500ms
        Timer().scheduleAtFixedRate(1,700){
            //load tensor
            tensorGeneric.load(recordGeneric)

            //add to classifier the tensor with relation
            val outputGeneric = classifierGeneric.classify(tensorGeneric)

            //Listen if change to speech with model generic
            val outputListGeneric = getMaxValue(outputGeneric)

            //TODO 5.0 if output list is no empty and is Speech

            if (outputListGeneric.isNoEmpty()){
                interfaceResultTensor.resultGeneric(outputListGeneric)
            }

        }
    }
    private fun List<String>.isNoEmpty():Boolean{
        return this[0].isNotEmpty() && this[1].isNotEmpty()
    }

    private fun getMaxValue(output: MutableList<Classifications>): List<String> {
        val filteredModelOutput = output[0].categories.filter {
            it.score > probabilityThreshold
        }
        val outputStr =
            filteredModelOutput.sortedBy { -it.score }
                .joinToString(separator = "\n") { "${it.label} , ${it.score.toString()} " }.split(",")

        return outputStr
    }

}

```











