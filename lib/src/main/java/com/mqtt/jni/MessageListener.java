package com.mqtt.jni;

public interface MessageListener {
    void onMessage(ReceiveMessage message);

    void onConnect();

    void onDebugLog(String log);
}