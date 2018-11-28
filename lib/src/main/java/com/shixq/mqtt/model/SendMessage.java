package com.shixq.mqtt.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with author.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:58
 */
public class SendMessage implements Parcelable {
    private int msgType;
    private String topic;
    private String msgPayload;
    private int qos;
    public static final int CONNACK = 0x20;
    public static final int PUBLISH = 0x30;
    public static final int SUBSCRIBE = 0x80;
    public static final int UNSUBSCRIBE = 0xA0;

    public SendMessage() {

    }

    protected SendMessage(Parcel in) {
        msgType = in.readInt();
        topic = in.readString();
        msgPayload = in.readString();
        qos = in.readInt();
    }

    public static final Creator<SendMessage> CREATOR = new Creator<SendMessage>() {
        @Override
        public SendMessage createFromParcel(Parcel in) {
            return new SendMessage(in);
        }

        @Override
        public SendMessage[] newArray(int size) {
            return new SendMessage[size];
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(msgType);
        parcel.writeString(topic);
        parcel.writeString(msgPayload);
        parcel.writeInt(qos);
    }
}
