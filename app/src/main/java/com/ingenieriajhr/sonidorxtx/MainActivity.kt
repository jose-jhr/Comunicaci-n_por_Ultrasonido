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