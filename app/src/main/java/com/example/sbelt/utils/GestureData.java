package com.example.sbelt.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class GestureData implements Parcelable {
    public int amount;
    public Date startDate;
    public Date endDate;

    public GestureData(int amount, Date startDate, Date endDate) {
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public GestureData(){
    }

    protected GestureData(Parcel in) {
        amount = in.readInt();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
    }

    public static final Creator<GestureData> CREATOR = new Creator<GestureData>() {
        @Override
        public GestureData createFromParcel(Parcel in) {
            return new GestureData(in);
        }

        @Override
        public GestureData[] newArray(int size) {
            return new GestureData[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(amount);
        parcel.writeLong(startDate.getTime());
        parcel.writeLong(endDate.getTime());
    }
}
