package com.zerogerc.photoframe.slideshow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable class for storing loaded image as byte array.
 */
public class Image implements Parcelable {
    /**
     * Byte representation of image.
     */
    private byte[] data;

    /**
     * Create instance of {@link Image} with proper data.
     * @param data byte representation of image.
     */
    public Image(byte[] data) {
        this.data = data;
    }

    private Image(Parcel source) {
        data = new byte[source.readInt()];
        source.readByteArray(data);
    }

    /**
     * @return byte representation of image.
     */
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
