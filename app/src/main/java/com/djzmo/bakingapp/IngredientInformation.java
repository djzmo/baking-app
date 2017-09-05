package com.djzmo.bakingapp;

import android.os.Parcel;
import android.os.Parcelable;

public class IngredientInformation implements Parcelable {

    public String name;
    public int quantity;
    public String measure;

    public IngredientInformation() {}

    public IngredientInformation(String name, int quantity, String measure) {
        this.name = name;
        this.quantity = quantity;
        this.measure = measure;
    }

    public IngredientInformation(Parcel in) {
        this.name = in.readString();
        this.quantity = in.readInt();
        this.measure = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(quantity);
        parcel.writeString(measure);
    }

    @SuppressWarnings("unused")
    public static final Creator<IngredientInformation> CREATOR = new Creator<IngredientInformation>() {
        @Override
        public IngredientInformation createFromParcel(Parcel in) {
            return new IngredientInformation(in);
        }

        @Override
        public IngredientInformation[] newArray(int size) {
            return new IngredientInformation[size];
        }
    };

}
