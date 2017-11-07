package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Patryk on 07.11.2017.
 */

public class Pruefung implements Parcelable {

    public Pruefung() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static final Parcelable.Creator<Pruefung> CREATOR
            = new Parcelable.Creator<Pruefung>() {
        public Pruefung createFromParcel(Parcel in) {
            return new Pruefung(in);
        }

        public Pruefung[] newArray(int size) {
            return new Pruefung[size];
        }
    };

    private Pruefung(Parcel in) {

    }

}
