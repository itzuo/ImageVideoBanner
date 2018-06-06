package com.zxj.imagevideobanner.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by jay on 2018/5/30.
 */

public class BannerBean implements Serializable,Parcelable {
    private String url;
    private int type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeInt(this.type);
    }

    public BannerBean() {
    }

    protected BannerBean(Parcel in) {
        this.url = in.readString();
        this.type = in.readInt();
    }

    public static final Creator<BannerBean> CREATOR = new Creator<BannerBean>() {
        @Override
        public BannerBean createFromParcel(Parcel source) {
            return new BannerBean(source);
        }

        @Override
        public BannerBean[] newArray(int size) {
            return new BannerBean[size];
        }
    };

    @Override
    public String toString() {
        return "BannerBean{" +
                "url='" + url + '\'' +
                ", type=" + type +
                '}';
    }
}
