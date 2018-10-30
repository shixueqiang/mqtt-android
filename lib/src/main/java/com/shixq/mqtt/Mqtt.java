package com.shixq.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.shixq.mqtt.model.Config;
import com.shixq.mqtt.model.MqttMessage;
import com.shixq.mqtt.service.MqttService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with shixq.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:24
 */
public class Mqtt {
    private String TAG = "Mqtt";
    private static volatile Mqtt mInstance;
    private Messenger mRemoteMessenger;
    private Context mContext;
    private Config mCfg;
    private boolean connected;
    private List<MqttMessage> needSubscribe = new ArrayList<>();
    private List<MqttMessage> needUnsubscribe = new ArrayList<>();
    private MessageCallback mMessageCallback;

    public interface MessageCallback {
        void onMessage(MqttMessage message);
    }

    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MqttService.MSG_CONNECT:
                    connected = true;
                    if (mRemoteMessenger != null) {
                        for (MqttMessage mqttMessage : needSubscribe) {
                            Mqtt.this.sendMessage(mqttMessage);
                        }
                        for (MqttMessage mqttMessage : needUnsubscribe) {
                            Mqtt.this.sendMessage(mqttMessage);
                        }
                    }
                    break;
                case MqttService.MSG_MESSAGE_CALLBACK:
                    if (mMessageCallback != null) {
                        Bundle bundle = msg.getData();
                        bundle.setClassLoader(getClass().getClassLoader());
                        MqttMessage message = bundle.getParcelable(MqttService.BUNDLE_MESSAGE);
                        mMessageCallback.onMessage(message);
                    }
                    break;
                default:
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "onServiceConnected");
            mRemoteMessenger = new Messenger(iBinder);
            Message message = Message.obtain(null, MqttService.MSG_CLIENT_MESSENGER);
            message.replyTo = mMessenger;
            try {
                mRemoteMessenger.send(message);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException", e);
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
        }
    };

    private Mqtt(Context context) {
        mContext = context.getApplicationContext();
    }

    public static Mqtt init(Context context) {
        if (mInstance == null) {
            synchronized (Mqtt.class) {
                if (mInstance == null) {
                    mInstance = new Mqtt(context);
                }
            }
        }
        return mInstance;
    }

    public Mqtt config(Config cfg) {
        this.mCfg = cfg;
        return this;
    }

    public void start() {
        if (mCfg == null) {
            throw new NullPointerException("must call config before start");
        }
        Log.e(TAG, "start");
        Intent service = new Intent(mContext, MqttService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MqttService.MQTT_CONFIG, mCfg);
        service.putExtra(MqttService.BUNDLE_CONFIG, bundle);
        mContext.startService(service);
        if (mRemoteMessenger == null) {
            mContext.bindService(service, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    public void subscribe(String topic, int qos) {
        MqttMessage message = new MqttMessage();
        message.setMsgType(MqttMessage.SUBSCRIBE);
        message.setTopic(topic);
        message.setQos(qos);
        if (connected) {
            sendMessage(message);
        } else {
            needSubscribe.add(message);
        }
    }

    public void unsubscribe(String topic) {
        MqttMessage message = new MqttMessage();
        message.setMsgType(MqttMessage.UNSUBSCRIBE);
        message.setTopic(topic);
        if (connected) {
            sendMessage(message);
        } else {
            needUnsubscribe.add(message);
        }
    }

    public void publish(String topic, byte[] payload, int qos) {
        MqttMessage message = new MqttMessage();
        message.setMsgType(MqttMessage.PUBLISH);
        message.setTopic(topic);
        message.setPayload(payload);
        message.setQos(qos);
        if (connected) {
            sendMessage(message);
        }
    }

    private void sendMessage(MqttMessage mqttMessage) {
        Message message = Message.obtain();
        message.what = MqttService.MSG_MESSAGE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(MqttService.BUNDLE_MESSAGE, mqttMessage);
        message.setData(bundle);
        if (mRemoteMessenger != null) {
            try {
                mRemoteMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setmMessageCallback(MessageCallback mMessageCallback) {
        this.mMessageCallback = mMessageCallback;
    }

    public void onDestroy() {
        mContext.unbindService(mConn);
    }
}
