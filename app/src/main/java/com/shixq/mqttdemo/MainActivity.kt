package com.shixq.mqttdemo

import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.shixq.mqtt.Mqtt
import com.shixq.mqtt.model.Config
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "MainActivity"
    private lateinit var tvMessage: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var mqtt: Mqtt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMessage = findViewById(R.id.tv_message)
        etMessage = findViewById(R.id.et_message)
        btnSend = findViewById(R.id.btn_send)
        btnSend.setOnClickListener(this)
        mqtt = Mqtt.init(this)
        val ANDROID_ID = Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.e(TAG, "ANDROID_ID:$ANDROID_ID")
        val config = Config.Builder("192.168.0.114")
                .id(ANDROID_ID)
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_send -> {
                var message = etMessage.text.toString()
                if (!TextUtils.isEmpty(message)) {
                    val lastChar = message.last()
                    if (lastChar == ' ') {
                        message = message.substring(0, message.lastIndex - 1)
                    }
                    mqtt.publish("test/topic", message.toByteArray(), 1)
                    val stringBuffer = StringBuffer()
                    stringBuffer.append(tvMessage.text)
                    stringBuffer.append("\n")
                    stringBuffer.append(message)
                    tvMessage.text = stringBuffer.toString()
                    etMessage.setText("")
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mqtt.onDestroy()
    }
}
