package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Patryk on 07.11.2017.
 */

public class Pruefung implements Parcelable {

    private ArrayList<ContentValues> kriterien, values;
    private String bemerkungen, geraeteName, herstellerName, headerText, footerText, barcode;

    public Pruefung(ArrayList<ContentValues> contentValuesArray) throws IllegalArgumentException {
        if(!(contentValuesArray.get(0).containsKey("geraetename") &&
                contentValuesArray.get(0).containsKey("herstellername") &&
                contentValuesArray.get(0).containsKey("headertext") &&
                contentValuesArray.get(0).containsKey("footertext") &&
                contentValuesArray.get(0).containsKey("geraete_barcode") &&
                contentValuesArray.get(0).containsKey("idkriterium") &&
                contentValuesArray.get(0).containsKey("text") &&
                contentValuesArray.get(0).containsKey("anzeigeart")))
            throw new IllegalArgumentException("Die Inhalte der übergebenen Liste hatten das falsche Format");
        this.bemerkungen = contentValuesArray.get(0).containsKey("bemerkungen") ? contentValuesArray.get(0).getAsString("bemerkungen") : "";
        this.geraeteName = contentValuesArray.get(0).getAsString("geraeteName");
        this.herstellerName = contentValuesArray.get(0).getAsString("herstellerName");
        this.headerText = contentValuesArray.get(0).getAsString("headerText");
        this.footerText = contentValuesArray.get(0).getAsString("footerText");
        this.barcode = contentValuesArray.get(0).getAsString("geraete_barcode");
        this.kriterien = new ArrayList<>();
        this.values = new ArrayList<>();
        ContentValues buffer;
        if (contentValuesArray.get(0).containsKey("messwert")) {
            for (ContentValues contentValues : contentValuesArray) {
                buffer = new ContentValues();
                buffer.put("idkriterium", contentValues.getAsString("idkriterium"));
                buffer.put("text", contentValues.getAsString("text"));
                buffer.put("anzeigeart", contentValues.getAsString("anzeigeart"));
                kriterien.add(buffer);
                buffer = new ContentValues();
                buffer.put("idkriterium", contentValues.getAsString("idkriterium"));
                buffer.put("messwert", contentValues.getAsString("messwert"));
                values.add(buffer);
            }

        } else {
            for (ContentValues contentValues : contentValuesArray) {
                buffer = new ContentValues();
                buffer.put("idkriterium", contentValues.getAsString("idkriterium"));
                buffer.put("text", contentValues.getAsString("text"));
                buffer.put("anzeigeart", contentValues.getAsString("anzeigeart"));
                kriterien.add(buffer);
            }
            this.values = getDefaultValues(this.kriterien);
        }
    }

    public Pruefung(ArrayList<ContentValues> kriterien, ArrayList<ContentValues> values, String bemerkungen, String geraeteName, String herstellerName, String headerText, String footerText, String barcode) throws IllegalArgumentException {
        if(!(kriterien.get(0).containsKey("bemerkungen") &&
                kriterien.get(0).containsKey("geraeteName") &&
                kriterien.get(0).containsKey("herstellerName") &&
                kriterien.get(0).containsKey("headerText") &&
                kriterien.get(0).containsKey("footerText") &&
                kriterien.get(0).containsKey("geraete_barcode") &&
                kriterien.get(0).containsKey("idkriterium") &&
                kriterien.get(0).containsKey("text") &&
                kriterien.get(0).containsKey("anzeigeart") &&
                kriterien.size() == values.size()))
            throw new IllegalArgumentException("Die Inhalte der übergebenen Liste hatten das falsche Format");
        this.kriterien = kriterien;
        this.values = values;
        this.bemerkungen = bemerkungen;
        this.geraeteName = geraeteName;
        this.herstellerName = herstellerName;
        this.headerText = headerText;
        this.footerText = footerText;
        this.barcode = barcode;
    }

    private ArrayList<ContentValues> getDefaultValues(ArrayList<ContentValues> contentValuesArray) throws IllegalArgumentException {
        if(!(contentValuesArray.get(0).containsKey("idkriterium") &&
                contentValuesArray.get(0).containsKey("text") &&
                contentValuesArray.get(0).containsKey("anzeigeart")))
            throw new IllegalArgumentException("Die Inhalte der übergebenen Liste hatten das falsche Format");
        ArrayList<ContentValues> values = new ArrayList<>();
        ContentValues buffer;
        for (ContentValues contentValues : contentValuesArray)
        {
            buffer = new ContentValues();
            buffer.put("idkriterium", contentValues.getAsInteger("idkriterium"));
            switch (contentValues.getAsString("anzeigeart"))
            {
                case "b":
                    buffer.put("messwert", false);
                    break;
                default:
                    buffer.put("messwert", "");
                    break;
            }
            values.add(buffer);
        }
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(kriterien);
        parcel.writeTypedList(values);
        parcel.writeString(bemerkungen);
        parcel.writeString(geraeteName);
        parcel.writeString(herstellerName);
        parcel.writeString(headerText);
        parcel.writeString(footerText);
        parcel.writeString(barcode);
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
        this.kriterien = new ArrayList<>();
        this.values = new ArrayList<>();
        in.readTypedList(kriterien, ContentValues.CREATOR);
        in.readTypedList(values, ContentValues.CREATOR);
        bemerkungen = in.readString();
        geraeteName = in.readString();
        herstellerName = in.readString();
        headerText = in.readString();
        footerText = in.readString();
        barcode = in.readString();
    }

}
