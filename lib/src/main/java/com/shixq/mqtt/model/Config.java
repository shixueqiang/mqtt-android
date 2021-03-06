package com.shixq.mqtt.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created with author.
 * Description:
 * Date: 2018-10-26
 * Time: 下午4:58
 */
public class Config implements Parcelable {
    private String id;
    private String host;
    private int port = 1883;
    private String username;
    private String password;
    private String cafile;
    private String certfile;
    private String keyfile;
    private int keepalive = 60;
    private int protocolVersion = MQTT_PROTOCOL_V311;
    private boolean debug;
    //是否接收离线消息
    private boolean disableCleanSession;
    public static final int MQTT_PROTOCOL_V31 = 3;
    public static final int MQTT_PROTOCOL_V311 = 4;

    protected Config(Parcel in) {
        id = in.readString();
        host = in.readString();
        port = in.readInt();
        username = in.readString();
        password = in.readString();
        cafile = in.readString();
        certfile = in.readString();
        keyfile = in.readString();
        keepalive = in.readInt();
        protocolVersion = in.readInt();
        debug = in.readByte() != 0;
        disableCleanSession = in.readByte() != 0;
    }

    private Config(Builder builder) {
        id = builder.id;
        host = builder.host;
        port = builder.port;
        username = builder.username;
        password = builder.password;
        cafile = builder.cafile;
        certfile = builder.certfile;
        keyfile = builder.keyfile;
        keepalive = builder.keepalive;
        protocolVersion = builder.protocolVersion;
        debug = builder.debug;
        disableCleanSession = builder.disableCleanSession;
    }

    public static class Builder {
        private String id;
        private String host;
        private int port;
        private String username;
        private String password;
        private String cafile;
        private String certfile;
        private String keyfile;
        private int keepalive;
        private int protocolVersion;
        private boolean debug;
        private boolean disableCleanSession;

        public Builder(String host) {
            this.host = host;
        }

        public Builder id(String val) {
            id = val;
            return this;
        }

        public Builder port(int val) {
            port = val;
            return this;
        }

        public Builder username(String val) {
            username = val;
            return this;
        }

        public Builder password(String val) {
            password = val;
            return this;
        }

        public Builder cafile(String val) {
            cafile = val;
            return this;
        }

        public Builder certfile(String val) {
            certfile = val;
            return this;
        }

        public Builder keyfile(String val) {
            keyfile = val;
            return this;
        }

        public Builder keepalive(int val) {
            keepalive = val;
            return this;
        }

        public Builder protocolVersion(int val) {
            protocolVersion = val;
            return this;
        }

        public Builder debug(boolean val) {
            debug = val;
            return this;
        }

        public Builder disableCleanSession(boolean val) {
            disableCleanSession = val;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCafile() {
        return cafile;
    }

    public void setCafile(String cafile) {
        this.cafile = cafile;
    }

    public String getCertfile() {
        return certfile;
    }

    public void setCertfile(String certfile) {
        this.certfile = certfile;
    }

    public String getKeyfile() {
        return keyfile;
    }

    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    public int getKeepalive() {
        return keepalive;
    }

    public void setKeepalive(int keepalive) {
        this.keepalive = keepalive;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDisableCleanSession() {
        return disableCleanSession;
    }

    public void setDisableCleanSession(boolean disableCleanSession) {
        this.disableCleanSession = disableCleanSession;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(host);
        parcel.writeInt(port);
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(cafile);
        parcel.writeString(certfile);
        parcel.writeString(keyfile);
        parcel.writeInt(keepalive);
        parcel.writeInt(protocolVersion);
        parcel.writeByte((byte) (debug ? 1 : 0));
        parcel.writeByte((byte) (disableCleanSession ? 1 : 0));
    }
}
