package com.ingenieriajhr.sonidorxtx

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