package com.mqtt.jni;

public interface MessageListener {
    void onMessage(String topic, byte[] message);

    void onConnect();

    void onDebugLog(String log);
}