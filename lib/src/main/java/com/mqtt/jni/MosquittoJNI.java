package com.mqtt.jni;

public class MosquittoJNI {
    private static volatile MosquittoJNI mInstance = null;
    private MessageListener messageListener;

    static {
        System.loadLibrary("mosquitto");
        System.loadLibrary("mqtt");
    }

    private MosquittoJNI() {
    }

    public static MosquittoJNI getInstance() {
        if (mInstance == null) {
            synchronized (MosquittoJNI.class) {
                if (mInstance == null) {
                    mInstance = new MosquittoJNI();
                }
            }
        }
        return mInstance;
    }

    public native int nativeSetupJNI();

    public native int nativeRunMain(Object arguments);

    public native int subscribe(String[] topics, int qos);

    public native int unsubscribe(String[] topics);

    public native int publish(String topic, String message, int qos);

    public native void nativeQuit();

    private void onMessage(String topic, byte[] message) {
        if (messageListener != null) {
            messageListener.onMessage(topic, message);
        }
    }

    private void onConnect() {
        if (messageListener != null) {
            messageListener.onConnect();
        }
    }

    private void onDebugLog(String log) {
        if (messageListener != null) {
            messageListener.onDebugLog(log);
        }
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }
}