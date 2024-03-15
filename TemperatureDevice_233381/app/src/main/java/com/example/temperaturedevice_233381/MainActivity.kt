package com.example.temperaturedevice_233381

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity(), SensorEventListener {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var mediaPlayer: MediaPlayer? = null
    var sensor: Sensor? = null
    var sensorManager: SensorManager? = null
    lateinit var image: ImageView
    lateinit var background: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        mediaPlayer = MediaPlayer.create(this, R.raw.sound1)
        image = findViewById(R.id.imageV)
        background = findViewById(R.id.back)
        image.visibility = View.INVISIBLE

        //Inicialización del sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    }

    private fun guardarDatos(temperatura: Float) {
        val fechaHora = dateFormat.format(Date())
        val jsonObject = JSONObject()
        jsonObject.put("fechaHora", fechaHora)
        jsonObject.put("TemperaturaC", temperatura)

        val jsonString = jsonObject.toString()

        try {
            val externalDir = getExternalFilesDir(null)
            if (externalDir != null) {
                val file = File(externalDir, "datos_temperatura.json")
                val fileOutputStream = FileOutputStream(file, true)
                fileOutputStream.write(jsonString.toByteArray())
                fileOutputStream.write("\n".toByteArray())
                fileOutputStream.close()
            } else {
                Log.e(TAG, "Directorio externo no disponible")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al escribir en el archivo: ${e.message}")
        }
    }

    fun playSound1() {
        mediaPlayer?.start()
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        try {
            if (event != null) {
                Log.d(TAG, "onSensorChanged:" + event.values[0])

                val temperatura = event.values[0]

                if (temperatura < 10) {
                    image.visibility = View.INVISIBLE
                    background.setBackgroundColor(resources.getColor(R.color.azulBajito))
                } else if (temperatura < 30) {
                    image.visibility = View.INVISIBLE
                    background.setBackgroundColor(resources.getColor(R.color.amarilloCalido))
                } else if (temperatura < 50) {
                    image.visibility = View.INVISIBLE
                    background.setBackgroundColor(resources.getColor(R.color.rojoCalor))
                } else {
                    image.visibility = View.VISIBLE
                    image.setImageResource(R.drawable.skeletonburning)
                    background.setBackgroundColor(resources.getColor(R.color.rojoHirviendo))
                    playSound1()
                }
                guardarDatos(temperatura)
            }
        } catch (e: IOException) {
            Log.d(TAG, "onSensorChanged: +${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }
}