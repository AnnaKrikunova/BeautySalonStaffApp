package com.example.android.beautysalonstaffapp.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Master implements Parcelable{
    private String name, username, password, masterId;
    private Long rating;

    public Master() {
    }

    public Master(String name, String username, String password, Long rating) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.rating = rating;
    }

    protected Master(Parcel in) {
        name = in.readString();
        username = in.readString();
        password = in.readString();
        masterId = in.readString();
        if (in.readByte() == 0) {
            rating = null;
        } else {
            rating = in.readLong();
        }
    }

    public static final Creator<Master> CREATOR = new Creator<Master>() {
        @Override
        public Master createFromParcel(Parcel in) {
            return new Master(in);
        }

        @Override
        public Master[] newArray(int size) {
            return new Master[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(username);
        parcel.writeString(password);
        parcel.writeString(masterId);
        if (rating == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(rating);
        }
    }
}
