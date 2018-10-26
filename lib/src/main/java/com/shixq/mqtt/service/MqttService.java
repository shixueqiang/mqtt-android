package com.shixq.mqtt.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.mqtt.jni.MessageListener;
import com.mqtt.jni.MosquittoJNI;
import com.shixq.mqtt.model.Config;

/**
 * Created with shixq.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:01
 */
public class MqttService extends Service {
    private final String TAG = "MqttService";
    private MosquittoJNI mMosquitto;
    private Messenger mClientMessenger;
    private Config mCfg;
    public static final int MSG_CLIENT_MESSENGER = 0x1;
    public static final int MSG_MESSAGE = 0x2;
    public static final int MSG_MESSAGE_CALLBACK = 0x3;
    public static final int MSG_CONNECT = 0x4;
    public static final String MQTT_CONFIG = "MQTT_CONFIG";

    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CLIENT_MESSENGER:
                    mClientMessenger = msg.replyTo;
                    break;
                case MSG_MESSAGE:
                    com.shixq.mqtt.model.Message message = (com.shixq.mqtt.model.Message) msg.obj;
                    switch (message.getMsgType()) {
                        case com.shixq.mqtt.model.Message.SUBSCRIBE:
                            mMosquitto.subscribe(new String[]{message.getTopic()}, message.getQos());
                            break;
                        case com.shixq.mqtt.model.Message.UNSUBSCRIBE:
                            mMosquitto.unsubscribe(new String[]{message.getTopic()});
                            break;
                    }
                    break;
                default:
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
        mMosquitto = MosquittoJNI.getInstance();
        mMosquitto.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(String topic, byte[] message) {
                Message msg = Message.obtain();
                msg.what = MSG_MESSAGE_CALLBACK;
                com.shixq.mqtt.model.Message m = new com.shixq.mqtt.model.Message();
                m.setTopic(topic);
                m.setPayload(message);
                msg.obj = m;
                try {
                    mClientMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnect() {
                try {
                    mClientMessenger.send(Message.obtain(null, MSG_CONNECT));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDebugLog(String log) {
                Log.e(TAG, log);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        mCfg = intent.getParcelableExtra(MQTT_CONFIG);
        nativeRun(mCfg);
        return super.onStartCommand(intent, flags, startId);
    }

    private void nativeRun(Config cfg) {
        StringBuffer mBuffer = new StringBuffer();
        mBuffer.append("mosquitto_sub ");
        mBuffer.append("-h ");
        mBuffer.append(cfg.getHost() + " ");
        mBuffer.append("-p ");
        mBuffer.append(cfg.getPort() + " ");
        if (!TextUtils.isEmpty(cfg.getUsername())) {
            mBuffer.append("-u ");
            mBuffer.append(cfg.getUsername() + " ");
        }
        if (!TextUtils.isEmpty(cfg.getPassword())) {
            mBuffer.append("-P ");
            mBuffer.append(cfg.getPassword() + " ");
        }
        if (cfg.getKeepalive() > 0) {
            mBuffer.append("-k ");
            mBuffer.append(cfg.getKeepalive() + " ");
        }
        if (cfg.isDebug()) {
            mBuffer.append("-d");
        }
        mMosquitto.nativeRunMain("mqtt_main", mBuffer.toString().split(" "));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mMosquitto.nativeQuit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
