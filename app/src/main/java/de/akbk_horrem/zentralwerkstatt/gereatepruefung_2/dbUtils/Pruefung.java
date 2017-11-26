package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Patryk on 07.11.2017.
 */

public class Pruefung implements Parcelable {

    private final SimpleDateFormat dateFormatIn = new SimpleDateFormat("yyyy-MM-dd"), dateFormatOut = new SimpleDateFormat("dd.MM.yyyy");

    private ArrayList<ContentValues> kriterien, values;

    private String bemerkungen, geraeteName, herstellerName, headerText, footerText, barcode;

    private Date datum;

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
        try {
            this.datum = contentValuesArray.get(0).containsKey("datum") ? dateFormatIn.parse(contentValuesArray.get(0).getAsString("datum")) : new Date(Calendar.getInstance().getTimeInMillis());
        }catch(ParseException e) {
            throw new IllegalArgumentException("Das Datum in der übergebenen Liste hatte das falsche Format");
        }
        this.geraeteName = contentValuesArray.get(0).getAsString("geraetename");
        this.herstellerName = contentValuesArray.get(0).getAsString("herstellername");
        this.headerText = contentValuesArray.get(0).getAsString("headertext");
        this.footerText = contentValuesArray.get(0).getAsString("footertext");
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
        parcel.writeValue(this.datum);
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
        datum = (Date) in.readValue(Date.class.getClassLoader());
    }

    public ArrayList<ContentValues> getKriterien() {
        return kriterien;
    }

    public ArrayList<ContentValues> getValues() {
        return values;
    }

    public String getBemerkungen() {
        return bemerkungen;
    }

    public String getGeraeteName() {
        return geraeteName;
    }

    public String getHerstellerName() {
        return herstellerName;
    }

    public String getHeaderText() {
        return headerText;
    }

    public String getFooterText() {
        return footerText;
    }

    public String getBarcode() {
        return barcode;
    }

    public Date getDatum() {
        return datum;
    }

    public String getFormatDatum() {
        return dateFormatOut.format(this.datum);
    }

    public int getCount(){
        return this.kriterien.size();
    }

    public void setBemerkungen(String bemerkungen) {
        this.bemerkungen = bemerkungen;
    }


}
