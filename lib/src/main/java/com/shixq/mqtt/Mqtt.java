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

import com.mqtt.jni.ReceiveMessage;
import com.shixq.mqtt.model.Config;
import com.shixq.mqtt.model.SendMessage;
import com.shixq.mqtt.service.MqttService;

/**
 * Created with shixq.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:24
 */
public class Mqtt {
    private String TAG = "Mqtt";
    private static volatile Mqtt mInstance;
    private Messenger mService;
    private Context mContext;
    private Config mCfg;
    private boolean mIsBound;
    private MessageCallback mMessageCallback;

    public interface MessageCallback {
        void onConnect();

        void onMessage(ReceiveMessage message);
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MqttService.MSG_CONNECT:
                    if (mMessageCallback != null) {
                        mMessageCallback.onConnect();
                    }
                    break;
                case MqttService.MSG_DISPATCH_MESSAGE:
                    if (mMessageCallback != null) {
                        Bundle bundle = msg.getData();
                        bundle.setClassLoader(getClass().getClassLoader());
                        ReceiveMessage message = bundle.getParcelable(MqttService.BUNDLE_MESSAGE);
                        mMessageCallback.onMessage(message);
                    }
                    break;
                default:
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "onServiceConnected");
            mService = new Messenger(iBinder);
            Message message = Message.obtain(null, MqttService.MSG_REGISTER_CLIENT);
            message.replyTo = mMessenger;
            try {
                mService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            doStartService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
            mService = null;
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
        doBindService();
    }

    public void subscribe(String topic, int qos) {
        SendMessage message = new SendMessage();
        message.setMsgType(SendMessage.SUBSCRIBE);
        message.setTopic(topic);
        message.setQos(qos);
        sendMessage(message);
    }

    public void unsubscribe(String topic) {
        SendMessage message = new SendMessage();
        message.setMsgType(SendMessage.UNSUBSCRIBE);
        message.setTopic(topic);
        sendMessage(message);
    }

    public void publish(String topic, String payload, int qos) {
        SendMessage message = new SendMessage();
        message.setMsgType(SendMessage.PUBLISH);
        message.setTopic(topic);
        message.setMsgPayload(payload);
        message.setQos(qos);
        sendMessage(message);
    }

    private void sendMessage(SendMessage mqttMessage) {
        Message message = Message.obtain();
        message.what = MqttService.MSG_RECEIVE_MESSAGE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(MqttService.BUNDLE_MESSAGE, mqttMessage);
        message.setData(bundle);
        try {
            mService.send(message);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void doStartService() {
        Intent service = new Intent(mContext, MqttService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MqttService.MQTT_CONFIG, mCfg);
        service.putExtra(MqttService.BUNDLE_CONFIG, bundle);
        mContext.startService(service);
    }

    void doBindService() {
        Intent service = new Intent(mContext, MqttService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MqttService.MQTT_CONFIG, mCfg);
        service.putExtra(MqttService.BUNDLE_CONFIG, bundle);
        mContext.bindService(service, mConn, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            MqttService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            mContext.unbindService(mConn);
            mIsBound = false;
        }
    }


    public void setMessageCallback(MessageCallback messageCallback) {
        this.mMessageCallback = messageCallback;
    }

    public void onDestroy() {
        doUnbindService();
    }
}
