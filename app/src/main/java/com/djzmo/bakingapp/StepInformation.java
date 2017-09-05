package com.djzmo.bakingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class StepInformation implements Parcelable {

    public String remoteId;
    public String shortDescription;
    public String description;
    public String videoUrl;
    public String thumbnailUrl;

    public StepInformation() {}

    public StepInformation(String remoteId, String shortDescription, String description, String videoUrl, String thumbnailUrl) {
        this.remoteId = remoteId;
        this.shortDescription = shortDescription;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    public StepInformation(Parcel in) {
        this.remoteId = in.readString();
        this.shortDescription = in.readString();
        this.description = in.readString();
        this.videoUrl = in.readString();
        this.thumbnailUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(remoteId);
        parcel.writeString(shortDescription);
        parcel.writeString(description);
        parcel.writeString(videoUrl);
        parcel.writeString(thumbnailUrl);
    }

    @SuppressWarnings("unused")
    public static final Creator<StepInformation> CREATOR = new Creator<StepInformation>() {
        @Override
        public StepInformation createFromParcel(Parcel in) {
            return new StepInformation(in);
        }

        @Override
        public StepInformation[] newArray(int size) {
            return new StepInformation[size];
        }
    };

}
