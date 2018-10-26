package com.shixq.mqtt.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with author.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:58
 */
public class Message implements Parcelable {
    private int msgType;
    private String topic;
    private byte[] payload;
    private int qos;
    public static final int CONNACK = 0x20;
    public static final int PUBLISH = 0x30;
    public static final int SUBSCRIBE = 0x80;
    public static final int UNSUBSCRIBE = 0xA0;

    public Message() {

    }

    protected Message(Parcel in) {
        msgType = in.readInt();
        topic = in.readString();
        payload = in.createByteArray();
        qos = in.readInt();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(msgType);
        parcel.writeString(topic);
        parcel.writeByteArray(payload);
        parcel.writeInt(qos);
    }
}
