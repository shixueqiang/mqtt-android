package com.shixq.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.shixq.mqtt.model.Config;
import com.shixq.mqtt.service.MqttService;

/**
 * Created with shixq.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:24
 */
public class Mqtt {
    private static volatile Mqtt mInstance;
    private Messenger mRemoteMessenger;
    private Context mContext;
    private Config mCfg;
    private boolean connected;
    private MessageCallback mMessageCallback;

    public interface MessageCallback {
        void onMessage(com.shixq.mqtt.model.Message message);
    }

    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MqttService.MSG_CONNECT:
                    connected = true;
                    break;
                case MqttService.MSG_MESSAGE_CALLBACK:
                    if (mMessageCallback != null) {
                        com.shixq.mqtt.model.Message message = (com.shixq.mqtt.model.Message) msg.obj;
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
            mRemoteMessenger = new Messenger(iBinder);
            Message message = Message.obtain(null, MqttService.MSG_CLIENT_MESSENGER);
            message.replyTo = mMessenger;
            try {
                mRemoteMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

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

    public static Mqtt config(Config cfg) {
        return mInstance.setCfg(cfg);
    }

    private Mqtt setCfg(Config cfg) {
        this.mCfg = cfg;
        return this;
    }

    private void start() {
        if (mCfg == null) {
            throw new NullPointerException("must call config before start");
        }
        Intent service = new Intent(mContext, MqttService.class);
        service.putExtra(MqttService.MQTT_CONFIG, mCfg);
        mContext.bindService(service, mConn, Context.BIND_AUTO_CREATE);
    }

    public void subscribe(String topic, int qos) {
        if(connected) {
            com.shixq.mqtt.model.Message message = new com.shixq.mqtt.model.Message();
            message.setMsgType(com.shixq.mqtt.model.Message.SUBSCRIBE);
            message.setTopic(topic);
            message.setQos(qos);
            Message msg = Message.obtain();
            msg.what = MqttService.MSG_MESSAGE;
            msg.obj = message;
            try {
                mRemoteMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unsubscribe(String topic) {
        com.shixq.mqtt.model.Message message = new com.shixq.mqtt.model.Message();
        message.setMsgType(com.shixq.mqtt.model.Message.UNSUBSCRIBE);
        message.setTopic(topic);
        Message msg = Message.obtain();
        msg.what = MqttService.MSG_MESSAGE;
        msg.obj = message;
        try {
            mRemoteMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setmMessageCallback(MessageCallback mMessageCallback) {
        this.mMessageCallback = mMessageCallback;
    }
}
