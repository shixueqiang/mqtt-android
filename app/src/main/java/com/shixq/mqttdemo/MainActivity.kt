package com.shixq.mqttdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.shixq.mqtt.Mqtt
import com.shixq.mqtt.model.Config
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var tvMessage: TextView
    private lateinit var mqtt: Mqtt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMessage = findViewById(R.id.message)
        mqtt = Mqtt.init(this)
        val config = Config.Builder("192.168.0.114")
                .port(1883)
                .debug(true)
                .build()
        mqtt.config(config)
        mqtt.setmMessageCallback { msg ->
            runOnUiThread {
                val stringBuffer = StringBuffer()
                stringBuffer.append(tvMessage.text)
                stringBuffer.append("\n")
                stringBuffer.append(String(msg.payload, Charset.forName("utf-8")))
                tvMessage.text = stringBuffer.toString()
            }
        }
        mqtt.start()
        mqtt.subscribe("test/shixq", 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        mqtt.onDestroy()
    }
}
