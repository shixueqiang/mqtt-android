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
import com.mqtt.jni.ReceiveMessage
import com.shixq.mqtt.Mqtt
import com.shixq.mqtt.model.Config
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TAG = "MainActivity"
    private lateinit var tvMessage: TextView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var mqtt: Mqtt
    private val cafile = "ca.crt"
    private val certfile = "client.pem"
    private val keyfile = "client.key.unsecure"

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
        val builder = Config.Builder("192.168.0.114")
                .id(ANDROID_ID)
                .port(1883)
                .debug(true)
        if (!BuildConfig.DEBUG) {
            copyCerts(cafile, filesDir.absolutePath.plus(File.separator).plus(cafile))
            copyCerts(certfile, filesDir.absolutePath.plus(File.separator).plus(certfile))
            copyCerts(keyfile, filesDir.absolutePath.plus(File.separator).plus(keyfile))
            builder.cafile(filesDir.absolutePath.plus(File.separator).plus(cafile))
                    .certfile(filesDir.absolutePath.plus(File.separator).plus(certfile))
                    .keyfile(filesDir.absolutePath.plus(File.separator).plus(keyfile))
        }
        mqtt.config(builder.build())
        mqtt.setMessageCallback(object : Mqtt.MessageCallback {
            override fun onConnect() {
                mqtt.subscribe("test/topic", 1)
            }

            override fun onMessage(message: ReceiveMessage) {
                runOnUiThread {
                    Log.e(TAG, message.toString())
                    val stringBuffer = StringBuffer()
                    stringBuffer.append(tvMessage.text)
                    stringBuffer.append("\n")
                    stringBuffer.append(message.msgPayload)
                    tvMessage.text = stringBuffer.toString()
                }
            }

        })
        mqtt.start()
    }

    fun copyCerts(asset: String, outputFile: String) {
        val file = File(outputFile)
        if (file.exists()) {
            return
        }
        val myOutput = FileOutputStream(outputFile)
        val myInput = assets.open(asset)
        val buffer = ByteArray(1024)
        var length = myInput.read(buffer)
        while (length > 0) {
            myOutput.write(buffer, 0, length)
            length = myInput.read(buffer)
        }
        myOutput.flush()
        myInput.close()
        myOutput.close()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_send -> {
                var message = etMessage.text.toString()
                if (!TextUtils.isEmpty(message)) {
                    mqtt.publish("test/shixq", message, 1)
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
