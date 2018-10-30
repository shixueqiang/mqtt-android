package com.shixq.mqtt.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
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
import com.shixq.mqtt.model.MqttMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private boolean isConnected;
    private boolean isStarted;
    private ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(5);
    public static final int MSG_CLIENT_MESSENGER = 0x1;
    public static final int MSG_MESSAGE = 0x2;
    public static final int MSG_MESSAGE_CALLBACK = 0x3;
    public static final int MSG_CONNECT = 0x4;
    public static final String MQTT_CONFIG = "MQTT_CONFIG";
    public static final String BUNDLE_CONFIG = "BUNDLE_CONFIG";
    public static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";

    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CLIENT_MESSENGER:
                    mClientMessenger = msg.replyTo;
                    if (isConnected) {
                        try {
                            mClientMessenger.send(Message.obtain(null, MSG_CONNECT));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case MSG_MESSAGE:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(getClass().getClassLoader());
                    MqttMessage message = bundle.getParcelable(BUNDLE_MESSAGE);
                    switch (message.getMsgType()) {
                        case MqttMessage.SUBSCRIBE:
                            mMosquitto.subscribe(new String[]{message.getTopic()}, message.getQos());
                            break;
                        case MqttMessage.UNSUBSCRIBE:
                            mMosquitto.unsubscribe(new String[]{message.getTopic()});
                            break;
                        case MqttMessage.PUBLISH:
                            final StringBuffer stringBuffer = new StringBuffer("mosquitto_pub ");
                            stringBuffer.append("-h ");
                            stringBuffer.append(mCfg.getHost() + " ");
                            stringBuffer.append("-p ");
                            stringBuffer.append(mCfg.getPort() + " ");
                            stringBuffer.append("-i ");
                            stringBuffer.append(mCfg.getId() + "-pub ");
                            stringBuffer.append("-t ");
                            stringBuffer.append(message.getTopic() + " ");
                            stringBuffer.append("-m ");
                            stringBuffer.append(message.payloadToString() + " ");
                            stringBuffer.append("-q ");
                            stringBuffer.append(1 + " ");
                            stringBuffer.append("-d");
                            mFixedThreadPool.submit(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, stringBuffer.toString());
                                    mMosquitto.publish(stringBuffer.toString().split(" "));
                                }
                            });
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
                MqttMessage m = new MqttMessage();
                m.setTopic(topic);
                m.setPayload(message);
                Bundle bundle = new Bundle();
                bundle.putParcelable(BUNDLE_MESSAGE, m);
                msg.setData(bundle);
                try {
                    mClientMessenger.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnect() {
                try {
                    Log.e(TAG, "onConnect");
                    isConnected = true;
                    if (mClientMessenger != null) {
                        mClientMessenger.send(Message.obtain(null, MSG_CONNECT));
                    } else {
                        Log.e(TAG, "onConnect mClientMessenger is null");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "RemoteException", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onDebugLog(String log) {
                Log.e(TAG, log);
            }

            @Override
            public void onPublishEnd(String topic) {
                Log.e(TAG, "onPublishEnd:" + topic);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        if (!isStarted) {
            Bundle bundle = intent.getBundleExtra(BUNDLE_CONFIG);
            mCfg = bundle.getParcelable(MQTT_CONFIG);
            mMosquitto.nativeSetupJNI();
            nativeRun(mCfg);
            isStarted = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void nativeRun(Config cfg) {
        final StringBuffer mBuffer = new StringBuffer("mosquitto_sub ");
        mBuffer.append("-h ");
        mBuffer.append(cfg.getHost() + " ");
        mBuffer.append("-p ");
        mBuffer.append(cfg.getPort() + " ");
        if (!TextUtils.isEmpty(cfg.getId())) {
            mBuffer.append("-i ");
            mBuffer.append(cfg.getId() + "-sub ");
        }
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
        final String[] argv = mBuffer.toString().split(" ");
        mFixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                mMosquitto.nativeRunMain(argv);
            }
        });
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
