package com.shixq.mqttdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.mqtt.jni.MessageListener
import com.mqtt.jni.MosquittoJNI
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var mosquitto: MosquittoJNI
    private lateinit var tvMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMessage = findViewById(R.id.message)
        mosquitto = MosquittoJNI.getInstance()
        mosquitto.setMessageListener(object : MessageListener {
            override fun onDebugLog(log: String?) {
                Log.e(TAG, log)
            }

            override fun onMessage(topic: String, message: ByteArray) {
                Log.e(TAG, "receive $topic message:" + String(message, Charset.forName("utf-8")))
                val mStringBuffer = StringBuffer()
                mStringBuffer.append(tvMessage.text)
                mStringBuffer.append("\n")
                mStringBuffer.append(String(message, Charset.forName("utf-8")))
                runOnUiThread {
                    tvMessage.text = mStringBuffer.toString()
                }
            }

            override fun onConnect() {
                mosquitto.subscribe(arrayOf("test/shixq"), 1)
            }
        })
        mosquitto.nativeSetupJNI()
        Thread(Runnable {
            val status = mosquitto.nativeRunMain("mqtt_main", arrayOf("mosquitto_sub", "-h", "192.168.0.114", "-p", "1883", "-t", "test/topic", "-v", "-d"))
            Log.e(TAG, "nativeRunMain status $status")
        }).start()
    }
}
