package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Eine Klasse für die Datenbanktabelle geraete
 */
public class GeraeteDBHelper extends DBHelper {

    /**
     * Erzeugt neues GeraeteDBHelper-Objekt
     * @param applicationContext Context das dieses Objekt erzeugt
     */
    public GeraeteDBHelper(Context applicationContext) {
        super(applicationContext, "geraete");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (geraete_barcode TEXT PRIMARY KEY, idgeraetetyp INTEGER, anschaffungsdatum TEXT, seriennummer TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Fügt ein Datensatz in die Datenbank ein
     * @param geraeteBarcode Der Barcode des Geräts
     * @param idGeraetetyp ID des Gerätetyps
     * @param anschaffungsdatum Das Anschaffungsdatum des Geräts
     * @param seriennummer Die Seriennummer
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(String geraeteBarcode, long idGeraetetyp, String anschaffungsdatum, String seriennummer) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("geraete_barcode", geraeteBarcode);
        values.put("idgeraetetyp", idGeraetetyp);
        values.put("anschaffungsdatum", anschaffungsdatum);
        values.put("seriennummer", seriennummer);
        long id = db.insert(this.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /**
     * Fragt ein Datensatz aus der Datenbank ab
     * @param barcode Der Barcode des zu abfragenden Geräts
     * @return ContentValues mit den Daten zum Datensatz
     */
    public ContentValues getRowByBarcode(String barcode) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues result = getContentValuesFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE geraete_barcode = '" + barcode + "'", null));
        db.close();
        return result;
    }
}
