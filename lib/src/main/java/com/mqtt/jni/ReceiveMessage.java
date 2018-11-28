package com.mqtt.jni;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with author.
 * Description:
 * Date: 2018-11-28
 * Time: 下午12:49
 */
public class ReceiveMessage implements Parcelable {
    private int msgType;
    private long msgTimestamp;
    private String clientId;
    private String topic;
    private String msgId;
    private String msgPayload;
    private int qos;
    public static final int MQTT_MESSAGE_TEXT = 0x1;
    public static final int MQTT_MESSAGE_IMG = 0x2;
    public static final int MQTT_MESSAGE_VOICE = 0x3;
    public static final int MQTT_MESSAGE_VIDEO = 0x4;
    public static final int MQTT_MESSAGE_FILE = 0x5;

    public ReceiveMessage() {
    }

    protected ReceiveMessage(Parcel in) {
        msgType = in.readInt();
        msgTimestamp = in.readLong();
        clientId = in.readString();
        topic = in.readString();
        msgId = in.readString();
        msgPayload = in.readString();
        qos = in.readInt();
    }

    public static final Creator<ReceiveMessage> CREATOR = new Creator<ReceiveMessage>() {
        @Override
        public ReceiveMessage createFromParcel(Parcel in) {
            return new ReceiveMessage(in);
        }

        @Override
        public ReceiveMessage[] newArray(int size) {
            return new ReceiveMessage[size];
        }
    };

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public long getMsgTimestamp() {
        return msgTimestamp;
    }

    public void setMsgTimestamp(long msgTimestamp) {
        this.msgTimestamp = msgTimestamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgPayload() {
        return msgPayload;
    }

    public void setMsgPayload(String msgPayload) {
        this.msgPayload = msgPayload;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(msgType);
        dest.writeLong(msgTimestamp);
        dest.writeString(clientId);
        dest.writeString(topic);
        dest.writeString(msgId);
        dest.writeString(msgPayload);
        dest.writeInt(qos);
    }

    @Override
    public String toString() {
        return "ReceiveMessage{" +
                "msgType=" + msgType +
                ", msgTimestamp=" + msgTimestamp +
                ", clientId='" + clientId + '\'' +
                ", topic='" + topic + '\'' +
                ", msgId='" + msgId + '\'' +
                ", msgPayload='" + msgPayload + '\'' +
                ", qos=" + qos +
                '}';
    }
}
