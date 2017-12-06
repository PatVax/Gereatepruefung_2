package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/**
 * Eine Klasse für die Datenbanktabelle kriterien
 */
public class KriterienDBHelper extends DBHelper {

    /**
     * Erzeugt neues KriterienDBHelper-Objekt
     * @param applicationContext Context das dieses Objekt erzeugt
     */
    public KriterienDBHelper(Context applicationContext) {
        super(applicationContext, "kriterien");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (idkriterium INTEGER PRIMARY KEY, idgeraetetyp INTEGER, text TEXT, anzeigeart INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Fügt ein Datensatz in die Datenbank ein
     * @param idKriterium ID des Kriteriums
     * @param idGeraetetyp ID des Gerätetyps
     * @param text Das Kriterium
     * @param anzeigeart Die Art der Eingabe für das Kriterium ("h" für einen Header ohne Eingabe, "b" für eine Checkbox, ansonsten die Einheit der Eingabe
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(long idKriterium, long idGeraetetyp, String text, String anzeigeart) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idkriterium", idKriterium);
        values.put("idgeraetetyp", idGeraetetyp);
        values.put("text", text);
        values.put("anzeigeart", anzeigeart);
        long id = db.insert(this.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /**
     * Fragt die Kriterien ab die für das Gerätetyp gelten
     * @param idGeraetetyp ID des Gerätetyps
     * @return Eine Liste von ContenValues-Objekten, die die Datensätze beinhalten
     */
    public ArrayList<ContentValues> getKriterienRowsByGeraetetyp(long idGeraetetyp) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContentValues> result = getContentValuesArrayFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE idgeraetetyp = '" + idGeraetetyp + "' ORDER BY idkriterium ASC", null));
        db.close();
        return result;
    }
}
