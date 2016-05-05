package com.zerogerc.photoframe.slideshow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ZeRoGerc on 05/05/16.
 */
public class Image implements Parcelable {
    private byte[] data;

    public Image(byte[] data) {
        this.data = data;
    }

    private Image(Parcel source) {
        data = new byte[source.readInt()];
        source.readByteArray(data);
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data.length);
        dest.writeByteArray(data);
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
