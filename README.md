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
import android.media.AudioRecord
import android.util.Log
import kotlinx.coroutines.Runnable
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.task.audio.classifier.Classifications
import org.tensorflow.lite.task.core.BaseOptions

import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.thread

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

```


**4) Configuramos los permisos en el Manifest.**

```kotlin 
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
```

**5) Configuramos la clase principal y xml.**

```XML
    <?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <EditText
        android:id="@+id/edtTextEnviar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="10dp"
        android:layout_marginTop="20dp"
        android:layout_marginEnd="10dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"

        ></EditText>

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/edtTextEnviar"
        android:layout_marginTop="10dp"
        android:text="@string/enviar"
        android:id="@+id/btnEnviarMensaje"
        ></Button>


    <TextView
        android:id="@+id/txtEnviar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/btnEnviarMensaje"
        android:textColor="@color/white"
        android:layout_marginStart="10dp"
        android:layout_marginEnd="10dp"
        ></TextView>


    <Button
        android:id="@+id/btnEscuchar"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_marginTop="20dp"
        android:background="@drawable/baseline_mic_24"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/txtEnviar"></Button>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginStart="10dp"
        android:layout_marginEnd="10dp"
        android:background="@color/black"
        android:padding="10dp"
        android:text="consola"
        android:textColor="@color/white"
        android:textSize="20sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/btnEscuchar"
        android:layout_marginBottom="10dp"
        android:id="@+id/txtConsolaRx"
        ></TextView>
</androidx.constraintlayout.widget.ConstraintLayout>
```



**5.1) Solicitamos los permisos de usar el microfono.**

declaramos una variable que nos almacenara el estado del permiso del microfono.

```kotlin 
    //tiene permisos de microfono
    private var isMicPermiso = false
```

Llamamos a la funcion checkPermisoMic, que nos indicara si tiene permisos de usar el microfono 


```kotlin 
     //verifica permiso de microfono
        isMicPermiso = checkPermisoMic()
```
```kotlin 
       /**
     * Chekea si tiene permisos de microfono
     */
    private fun checkPermisoMic(): Boolean {
        val permiso = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
        return permiso == PackageManager.PERMISSION_GRANTED
    }
```

```kotlin 
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_RECORD_PERMISSION->{
                isMicPermiso = grantResults[0] == PackageManager.PERMISSION_GRANTED
                var mensaje = ""
                mensaje = if (isMicPermiso) "Permiso asignado" else "Permiso denegado"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }
```

```kotlin 
      /**
     * Permisos de microfono
     */
    private fun permisosMic() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_PERMISSION
        )
    }
```

**5.2) Inicializamos la clase TensorAudio**

```kotlin 
     //init tensorAudio
        tensorAudio = TensorAudio(this)
```





**5.3) Configuramos la clase que contendra las acciones de las vistas. **
```kotlin 
     //Views
        viewStart()
```

```kotlin 
     /**
     * Configuraciones de vistas
     */
    private fun viewStart() {
        //TODO Boton escuchar
       vb.btnEscuchar.setOnClickListener {
           if(isMicPermiso){
                tensorAudio.initialize()
                //datos recibidos
                var datosRx = ""
                var mensajeCompleto = ""
                //no duplicar
                var isDuplicate = false
                //escuchar respuesta de tensorAudio
               tensorAudio.setResultListener(object :TensorResultListener{
                   override fun onTensorResult(results: List<String>) {
                       Log.d("outputModel",results[0])
                       when(results[0][0]){
                           '0'->{
                               if (!isDuplicate){
                                   datosRx+="0"
                                   isDuplicate = true
                               }
                           }
                           '1'->{
                               if (!isDuplicate){
                                   datosRx+='1'
                                   isDuplicate = true
                               }
                           }
                           '2'->isDuplicate = false
                       }

                       if (datosRx.length==8){
                           val charValue = datosRx.toInt(2).toChar()
                           mensajeCompleto+=charValue
                           //set mensaje consola
                           vb.txtConsolaRx.text = mensajeCompleto
                           //resetear datos Rx
                           datosRx = ""
                       }
                   }

               })

           }else{
               permisosMic()
           }
       }

        //TODO Boton enviar binarios
        vb.btnEnviarMensaje.setOnClickListener {
            if (vb.edtTextEnviar.text.isNotEmpty()){
                //texto a enviar
                val txtBinario = vb.edtTextEnviar.text.toString().toBinary()
                vb.txtEnviar.text = txtBinario
                //enviar texto con sonido
                thread(start = true){
                    for (binario in txtBinario){
                        when(binario){
                            '0'->playSound(R.raw.dos250)
                            '1'->playSound(R.raw.cuatro400)
                            else->{
                                Thread.sleep(tiempoPausa)
                            }
                        }
                    }
                }
            }
        }
    }
```

Código completo MainActivity.

```Kotlin
package com.ingenieriajhr.sonidorxtx

import TensorAudio
import TensorResultListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ingenieriajhr.sonidorxtx.databinding.ActivityMainBinding
import org.w3c.dom.Text
import kotlin.concurrent.thread


private lateinit var vb: ActivityMainBinding

private val REQUEST_RECORD_PERMISSION = 100

//tiene permisos de microfono
private var isMicPermiso = false

//lateinit object tensorAudio
private lateinit var tensorAudio: TensorAudio

//Tiempo de pausa
private var tiempoPausa =1500L
private var tiempoSonido =500L

private var isThread = true



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        vb = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(vb.root)

        //verifica permiso de microfono
        isMicPermiso = checkPermisoMic()
        //Views
        viewStart()
        //init tensorAudio
        tensorAudio = TensorAudio(this)
    }

    /**
     * Chekea si tiene permisos de microfono
     */
    private fun checkPermisoMic(): Boolean {
        val permiso = ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
        return permiso == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Configuraciones de vistas
     */
    private fun viewStart() {
        //TODO Boton escuchar
       vb.btnEscuchar.setOnClickListener {
           if(isMicPermiso){
               Toast.makeText(this, "Inicie a escuchar", Toast.LENGTH_SHORT).show()
               //vb txt consola rx
                tensorAudio.initialize()
                //datos recibidos
                var datosRx = ""
                var mensajeCompleto = "Escuchando \n"
               //set mensaje consola
               vb.txtConsolaRx.text = mensajeCompleto
                //no duplicar
                var isDuplicate = false
                //escuchar respuesta de tensorAudio
               tensorAudio.setResListener(object :TensorResultListener{
                   override fun onTensorResult(results: List<String>) {
                       Log.d("outputModel",results[0])
                       when(results[0][0]){
                           '0'->{
                               if (!isDuplicate){
                                   datosRx+="0"
                                   isDuplicate = true
                               }
                           }
                           '1'->{
                               if (!isDuplicate){
                                   datosRx+='1'
                                   isDuplicate = true
                               }
                           }
                           '2'->isDuplicate = false
                       }
                       vb.txtEnviar.text = datosRx
                       if (datosRx.length==8){

                           val charValue = datosRx.toInt(2).toChar()
                           mensajeCompleto+=charValue
                           //set mensaje consola
                           vb.txtConsolaRx.text = mensajeCompleto
                           //resetear datos Rx
                           datosRx = ""
                       }
                   }

               })

           }else{
               permisosMic()
           }
       }

        vb.btnEscuchar.setOnLongClickListener {
            Toast.makeText(this, "Dejando de escuchar", Toast.LENGTH_SHORT).show()
            tensorAudio.stop()
            true
        }

        //TODO Boton enviar binarios
        vb.btnEnviarMensaje.setOnClickListener {
            if (vb.edtTextEnviar.text.isNotEmpty()){
                //texto a enviar
                val txtBinario = vb.edtTextEnviar.text.toString().toBinary()
                vb.txtEnviar.text = txtBinario
                //enviar texto con sonido
                thread(start = true){
                    for (binario in txtBinario){
                        if (!isThread) break
                        when(binario){
                            '0'->playSound(R.raw.dos250)
                            '1'->playSound(R.raw.cuatro400)
                            else->{
                                //Thread.sleep(tiempoPausa)
                            }
                        }
                    }
                    isThread = true
                }
            }
        }

        vb.btnEnviarMensaje.setOnLongClickListener {
            isThread = false
            Toast.makeText(this, "Envio detenido", Toast.LENGTH_SHORT).show()
            true
        }
    }


    /**
     * Emitir sonido
     */
    fun playSound(sonido:Int){
        var mediaPlayer = MediaPlayer.create(this,sonido)
        mediaPlayer.start()
        Thread.sleep(tiempoSonido)
        mediaPlayer.stop()
        Thread.sleep(tiempoPausa)
        mediaPlayer.release()
    }


    /**
     * String a binario
     */
    fun String.toBinary(): String {
        return this.toByteArray(Charsets.UTF_8).joinToString(separator = " "){
            String.format("%8s", it.toString(2)).replace(' ', '0')
        }
    }




    /**
     * Permisos de microfono
     */
    private fun permisosMic() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_PERMISSION
        )
    }

    /**
     * Resultado de la solicitud de permiso
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_RECORD_PERMISSION->{
                isMicPermiso = grantResults[0] == PackageManager.PERMISSION_GRANTED
                var mensaje = ""
                mensaje = if (isMicPermiso) "Permiso asignado" else "Permiso denegado"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

}

```


**5.4) Insertamos las frecuencias con las que fueron entrenados los modelos. **




**Frecuencia de 400hz:** https://cienciayculturacreativa.com/2024/comunicacion_ultrasonido/cuatro400.wav

**Frecuencia de 250hz:** https://cienciayculturacreativa.com/2024/comunicacion_ultrasonido/dos250.wav

en la carpeta raw

<a href="https://youtu.be/gwEq0-ACUr8?si=C6XIkI40u1sTYTNc&t=730">
  <img src="https://github.com/jose-jhr/Comunicacion_por_Ultrasonido/assets/66834393/246bdcaa-d87d-4f06-88d6-02f2860db04f" width="450">
</a>







