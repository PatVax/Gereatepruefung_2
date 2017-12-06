package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Eine Klasse für die Datenbanktabelle geraetetyp
 */
public class GeraetetypDBHelper extends DBHelper {

    /**
     * Erzeugt neues GeraetetypDBHelper-Objekt
     * @param applicationContext Context das dieses Objekt erzeugt
     */
    public GeraetetypDBHelper(Context applicationContext) {
        super(applicationContext, "geraetetyp");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (idgeraetetyp INTEGER PRIMARY KEY, herstellername TEXT, headertext TEXT, footertext TEXT, geraetename TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Fügt ein Datensatz in die Datenbank ein
     * @param idGeraetetyp ID des Gerätetyps
     * @param hersteller Die Herstellername
     * @param headerText Der Kopftext
     * @param footerText Der Fußtext
     * @param bezeichnung Bezeichnung des Gerätetyps
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(long idGeraetetyp, String hersteller, String headerText, String footerText, String bezeichnung) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idgeraetetyp", idGeraetetyp);
        values.put("herstellername", hersteller);
        values.put("headertext", headerText);
        values.put("footertext", footerText);
        values.put("geraetename", bezeichnung);
        long id = db.insert(this.TABLE_NAME, null, values);
        db.close();
        return id;
    }
}
