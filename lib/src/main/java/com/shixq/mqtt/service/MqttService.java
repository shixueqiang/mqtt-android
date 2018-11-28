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
import com.mqtt.jni.ReceiveMessage;
import com.shixq.mqtt.model.Config;
import com.shixq.mqtt.model.SendMessage;

import java.util.ArrayList;
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
    private ArrayList<Messenger> mClients = new ArrayList<>();
    private Config mCfg;
    private boolean isStarted;
    private ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(5);
    public static final int MSG_REGISTER_CLIENT = 0x1;
    public static final int MSG_UNREGISTER_CLIENT = 0x2;
    public static final int MSG_RECEIVE_MESSAGE = 0x3;
    public static final int MSG_DISPATCH_MESSAGE = 0x4;
    public static final int MSG_CONNECT = 0x5;
    public static final String MQTT_CONFIG = "MQTT_CONFIG";
    public static final String BUNDLE_CONFIG = "BUNDLE_CONFIG";
    public static final String BUNDLE_MESSAGE = "BUNDLE_MESSAGE";

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_RECEIVE_MESSAGE:
                    Bundle bundle = msg.getData();
                    bundle.setClassLoader(getClass().getClassLoader());
                    final SendMessage message = bundle.getParcelable(BUNDLE_MESSAGE);
                    switch (message.getMsgType()) {
                        case SendMessage.SUBSCRIBE:
                            mMosquitto.subscribe(new String[]{message.getTopic()}, message.getQos());
                            break;
                        case SendMessage.UNSUBSCRIBE:
                            mMosquitto.unsubscribe(new String[]{message.getTopic()});
                            break;
                        case SendMessage.PUBLISH:
                            mMosquitto.publish(message.getTopic(), message.getMsgPayload(), message.getQos());
                            break;
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        mMosquitto = MosquittoJNI.getInstance();
        mMosquitto.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(ReceiveMessage message) {
                Message msg = Message.obtain();
                msg.what = MSG_DISPATCH_MESSAGE;
                Bundle bundle = new Bundle();
                bundle.putParcelable(BUNDLE_MESSAGE, message);
                msg.setData(bundle);
                dispatchMessage(msg);
            }

            @Override
            public void onConnect() {
                dispatchMessage(Message.obtain(null, MSG_CONNECT));
            }

            @Override
            public void onDebugLog(String log) {

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

    private void dispatchMessage(Message msg) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
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
        if (!TextUtils.isEmpty(cfg.getCafile())) {
            mBuffer.append("--cafile ");
            mBuffer.append(cfg.getCafile() + " ");
        }
        if (!TextUtils.isEmpty(cfg.getCertfile())) {
            mBuffer.append("--cert ");
            mBuffer.append(cfg.getCertfile() + " ");
        }
        if (!TextUtils.isEmpty(cfg.getKeyfile())) {
            mBuffer.append("--key ");
            mBuffer.append(cfg.getKeyfile() + " ");
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
                if (mMosquitto.nativeRunMain(argv) != 0) {
                    //启动失败
                    stopSelf();
                }
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
