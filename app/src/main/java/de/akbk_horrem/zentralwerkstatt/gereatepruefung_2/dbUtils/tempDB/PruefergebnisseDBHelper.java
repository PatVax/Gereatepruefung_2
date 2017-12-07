package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

/**
 * Eine Klasse für die Datenbanktabelle pruefergebnisse
 */
public class PruefergebnisseDBHelper extends DBHelper {

    /**
     * Erzeugt neues PruefergebnisseDBHelper-Objekt
     * @param applicationContext Context das dieses Objekt erzeugt
     */
    public PruefergebnisseDBHelper(Context applicationContext) {
        super(applicationContext, "pruefergebnisse");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (idpruefung INTEGER, idkriterium INTEGER, messwert TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Fügt ein Datensatz in die Datenbank ein
     * @param idPruefung Die ID der Prüfung
     * @param idKriterium Die des Kriteriums
     * @param messwert Der Wert für das Kriterium in der Prüfung
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(long idPruefung, long idKriterium, String messwert) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idpruefung", idPruefung);
        values.put("idkriterium", idKriterium);
        values.put("messwert", messwert);
        long id = db.insert(this.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /**
     * Fragt die Datensätze für die bestimmte Prüfung ab
     * @param idPruefung ID der Prüfung
     * @return Liste von ContentValues-Objekten, die die Daten der Datensätzen beinhalten
     */
    public ArrayList<ContentValues> getRowsByIDPruefung(long idPruefung) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContentValues> result = getContentValuesArrayFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE idpruefung = '" + idPruefung + "'", null));
        db.close();
        return result;
    }

    /**
     * Löscht alle Datensätze der bestimmten Prüfung
     * @param idPruefung Die ID der Prüfung
     * @return Anzahl der Datensätzen die gelöscht wurden
     */
    public int deleteRowsByIDPruefung(long idPruefung){
        SQLiteDatabase db = getWritableDatabase();
        int count = db.delete(this.TABLE_NAME, "idpruefung = ?", new String[]{idPruefung + ""});
        db.close();
        return count;
    }
}
