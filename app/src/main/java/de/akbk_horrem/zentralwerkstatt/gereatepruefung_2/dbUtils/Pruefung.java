package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Patryk on 07.11.2017.
 * <p>
 * Die Klasse stellt eine Geräteprüfung dar
 */
public class Pruefung implements Parcelable {

    //Variablen für das Formatieren des Datums
    private static final SimpleDateFormat DATE_FORMAT_IN = new SimpleDateFormat("yyyy-MM-dd"),
            DATE_FORMAT_OUT = new SimpleDateFormat("dd.MM.yyyy");

    //ArrayLists für die Kriterien und deren Werte
    private ArrayList<ContentValues> kriterien, values;

    private String bemerkungen, geraeteName, herstellerName, headerText, footerText, barcode, seriennummer;

    private Date datum;

    /**
     * Erzeugt ein Pruefung Objekt
     * @param contentValuesArray Eine ContentValues-Liste die in der ersten Zeile folgende Schlüsseln enthalten muss: geraetename, herstellername, headertext, footertext, geraete_barcode, idkriterium, text, anzeigeart, seriennummer. Optional sind: bemerkungen, messwert und datum (falls es sich um eine alte Prüfung handelt).
     * @throws IllegalArgumentException Wenn die Liste keinen gültigen Format hat
     */
    public Pruefung(ArrayList<ContentValues> contentValuesArray) throws IllegalArgumentException {
        if(!(contentValuesArray.get(0).containsKey("geraetename") &&
                contentValuesArray.get(0).containsKey("herstellername") &&
                contentValuesArray.get(0).containsKey("headertext") &&
                contentValuesArray.get(0).containsKey("footertext") &&
                contentValuesArray.get(0).containsKey("geraete_barcode") &&
                contentValuesArray.get(0).containsKey("idkriterium") &&
                contentValuesArray.get(0).containsKey("text") &&
                contentValuesArray.get(0).containsKey("anzeigeart") &&
                contentValuesArray.get(0).containsKey("seriennummer")))
            throw new IllegalArgumentException("Die Inhalte der übergebenen Liste hatten das falsche Format");
        this.bemerkungen = contentValuesArray.get(0).containsKey("bemerkungen") ? contentValuesArray.get(0).getAsString("bemerkungen") : "";
        try {
            this.datum = contentValuesArray.get(0).containsKey("datum") ? DATE_FORMAT_IN.parse(contentValuesArray.get(0).getAsString("datum")) : new Date(Calendar.getInstance().getTimeInMillis());
        }catch(ParseException e) {
            throw new IllegalArgumentException("Das Datum in der übergebenen Liste hatte das falsche Format");
        }
        this.geraeteName = contentValuesArray.get(0).getAsString("geraetename");
        this.herstellerName = contentValuesArray.get(0).getAsString("herstellername");
        this.headerText = contentValuesArray.get(0).getAsString("headertext");
        this.footerText = contentValuesArray.get(0).getAsString("footertext");
        this.barcode = contentValuesArray.get(0).getAsString("geraete_barcode");
        this.seriennummer = contentValuesArray.get(0).getAsString("seriennummer");
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

    /**
     * Erzeugt ein Pruefung Objekt
     * @param contentValuesArray Eine ContentValues-Liste die in der ersten Zeile folgende Schlüsseln enthalten muss: idkriterium, text, anzeigeart, seriennummer. Optional ist messwert (falls es sich um eine alte Prüfung handelt).
     * @param bemerkungen Die Bemerkungen zu der Prüfung. null wenn nicht vorhanden
     * @param geraeteName Die Bezeichnung des Geräts
     * @param herstellerName Die Bezeichnung des Herstellers
     * @param headerText Der Headertext
     * @param footerText Der Footertext
     * @param barcode Der Barcode des Geräts
     * @param seriennummer Die Seriennummer des Geräts
     * @param datum Ein String der das Datum im Format Jahren-Monaten-Tagen darstellt
     * @throws IllegalArgumentException Wenn die Liste keinen gültigen Format hat
     * @throws ParseException Falls das Datum nicht gültig war
     */
    public Pruefung(ArrayList<ContentValues> contentValuesArray, @Nullable String bemerkungen, String geraeteName, String herstellerName, String headerText, String footerText, String barcode, String seriennummer, @Nullable String datum) throws IllegalArgumentException, ParseException {
        this(contentValuesArray, bemerkungen, geraeteName, herstellerName, headerText, footerText, barcode, seriennummer, DATE_FORMAT_IN.parse(datum));
    }

    /**
     * Erzeugt ein Pruefung Objekt
     * @param contentValuesArray Eine ContentValues-Liste die in der ersten Zeile folgende Schlüsseln enthalten muss: idkriterium, text, anzeigeart, seriennummer. Optional ist messwert (falls es sich um eine alte Prüfung handelt).
     * @param bemerkungen Die Bemerkungen zu der Prüfung. null wenn nicht vorhanden
     * @param geraeteName Die Bezeichnung des Geräts
     * @param herstellerName Die Bezeichnung des Herstellers
     * @param headerText Der Headertext
     * @param footerText Der Footertext
     * @param barcode Der Barcode des Geräts
     * @param seriennummer Die Seriennummer des Geräts
     * @param datum Das Datum der Prüfung. null wenn nicht vorhanden
     * @throws IllegalArgumentException Wenn die Liste keinen gültigen Format hat
     */
    public Pruefung(ArrayList<ContentValues> contentValuesArray, @Nullable String bemerkungen, String geraeteName, String herstellerName, String headerText, String footerText, String barcode, String seriennummer, @Nullable Date datum) throws IllegalArgumentException {
        if(!(contentValuesArray.get(0).containsKey("idkriterium") &&
                contentValuesArray.get(0).containsKey("text") &&
                contentValuesArray.get(0).containsKey("anzeigeart")))
            throw new IllegalArgumentException("Die Inhalte der übergebenen Liste hatten das falsche Format");

        this.bemerkungen = bemerkungen != null ? bemerkungen : "";
        this.geraeteName = geraeteName;
        this.herstellerName = herstellerName;
        this.headerText = headerText;
        this.footerText = footerText;
        this.barcode = barcode;
        this.seriennummer = seriennummer;
        this.datum = datum != null ? datum : new Date(Calendar.getInstance().getTimeInMillis());

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

    /**
     * Erzeugt die Standartwerte aus einer Liste der Kriterien
     * @param contentValuesArray Eine Liste die Kriteriendaten enthält
     * @return Liefert eine Liste der Werten der Kriterien zurück
     * @throws IllegalArgumentException
     */
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
            //Je nach anzeigeart ausfüllen
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
        parcel.writeString(seriennummer);
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

    /**
     * Stellt die Instanz aus einem Parcel wieder her
     * @param in Parcel
     * @see Parcelable
     */
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
        seriennummer = in.readString();
        datum = (Date) in.readValue(Date.class.getClassLoader());
    }

    /**
     * Gibt die Liste der Kriterien an
     * @return Eine Liste der Kriterien in ContentValues gefasst
     */
    public ArrayList<ContentValues> getKriterien() {
        return kriterien;
    }

    /**
     * Gibt die Liste der Werten an
     * @return Eine Liste der Werten in ContentValues gefasst
     */
    public ArrayList<ContentValues> getValues() {
        return values;
    }

    /**
     * Gibt Werte aus der Prüfliste an der bestimmten Position wieder
     * @param position Position der Werten
     * @return Liefert die Werte an der bestimmten Position zurück
     */
    public ContentValues getKriterienAtPosition(int position){
        return kriterien.get(position);
    }

    /**
     * Gibt die tatsächlichen Werte der Kriterien aus der Prüfliste an der bestimmten Position wieder
     * @param position Position der Werten
     * @return Liefert die Werte an der bestimmten Position zurück
     */
    public ContentValues getValuesAtPosition(int position){
        return values.get(position);
    }

    /**
     * Gibt den Text des Kriteriums an der bestimmten Position an
     * @param position Position des Kriteriums
     * @return Liefert den Text des Kriteriums zurück
     */
    public String getKriteriumAtPosition(int position){
        return getKriterienAtPosition(position).getAsString("text");
    }

    /**
     * Gibt die Anzeigeart des Kriteriums an der bestimmten Position an
     * @param position Position des Kriteriums
     * @return Liefert die Anzeigeart als String kodiert ("h" für Header, "b" für ein Ja/Nein Feld, oder die Einheit wenn es sich um ein Zahlenwert handelt) zurück
     */
    public String getAnzeigeartAtPosition(int position){
        return getKriterienAtPosition(position).getAsString("anzeigeart");
    }

    /**
     * Setzt einen Messwert an der bestimmten Position
     * @param position Die Position des Messwertes in der Prüfliste
     * @param messwert Der zu setzende Messwert
     */
    public void setValueAtPosition(int position, boolean messwert) {
        values.get(position).put("messwert", messwert);
    }

    /**
     * Setzt einen Messwert an der bestimmten Position
     * @param position Die Position des Messwertes in der Prüfliste
     * @param messwert Der zu setzende Messwert
     */
    public void setValueAtPosition(int position, String messwert) {
        values.get(position).put("messwert", messwert);
    }

    /**
     * @return Liefert die Bemerkungen zurück
     */
    public String getBemerkungen() {
        return bemerkungen;
    }

    /**
     * @return Liefert den Gerätenamen zurück
     */
    public String getGeraeteName() {
        return geraeteName;
    }

    /**
     * @return Liefert den Herstellernamen des Geräts zurück
     */
    public String getHerstellerName() {
        return herstellerName;
    }

    /**
     * @return Liefert den Header-Text zurück
     */
    public String getHeaderText() {
        return headerText;
    }

    /**
     * @return Liefert den Footer-Text zurück
     */
    public String getFooterText() {
        return footerText;
    }

    /**
     * @return Liefert den Barcode zurück
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * @return Liefert die Seriennummer des Geräts zurück
     */
    public String getSeriennummer() {
        return seriennummer;
    }

    /**
     * @return Liefert das Datum der Prüfung zurück
     */
    public Date getDatum() {
        return datum;
    }

    /**
     * Formatiert das Datum als Tagen.Monaten.Jahren
     * @return Ein formatiertes Datum
     */
    public String getFormatDatum() {
        return DATE_FORMAT_OUT.format(this.datum);
    }

    /**
     * Berechnet die Länge der Prüfliste für diese Prüfung
     * @return Länge der Prüfliste
     */
    public int getCount(){
        return this.kriterien.size();
    }

    /**
     * Setzt die Bemerkungen zu der Prüfung
     * @param bemerkungen Eine Zeichenkette mit Bemerkungen
     */
    public void setBemerkungen(String bemerkungen) {
        this.bemerkungen = bemerkungen;
    }


}
