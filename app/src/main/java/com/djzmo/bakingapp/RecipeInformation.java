package com.djzmo.bakingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipeInformation implements Parcelable {

    public String remoteId;
    public String name;
    public int servings;
    public String image;

    public RecipeInformation() {}

    public RecipeInformation(String remoteId, String name, int servings, String image) {
        this.remoteId = remoteId;
        this.name = name;
        this.servings = servings;
        this.image = image;
    }

    public RecipeInformation(Parcel in) {
        remoteId = in.readString();
        name = in.readString();
        servings = in.readInt();
        image = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(remoteId);
        parcel.writeString(name);
        parcel.writeInt(servings);
        parcel.writeString(image);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<RecipeInformation> CREATOR = new Parcelable.Creator<RecipeInformation>() {
        @Override
        public RecipeInformation createFromParcel(Parcel in) {
            return new RecipeInformation(in);
        }

        @Override
        public RecipeInformation[] newArray(int size) {
            return new RecipeInformation[size];
        }
    };

}
