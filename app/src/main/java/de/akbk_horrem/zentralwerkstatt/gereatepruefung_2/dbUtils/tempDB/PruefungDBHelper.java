package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Eine Klasse für die Datenbanktabelle pruefung
 */
public class PruefungDBHelper extends DBHelper {

    /**
     * Erzeugt neues PruefungDBHelper-Objekt
     * @param applicationContext Context das dieses Objekt erzeugt
     */
    public PruefungDBHelper(Context applicationContext) {
        super(applicationContext, "pruefung");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (idpruefung INTEGER PRIMARY KEY, benutzer TEXT, geraete_barcode TEXT, datum TEXT, bemerkungen TEXT, passwort TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Fügt ein Datensatz in die Datenbank ein
     * @param idPruefung Die ID der Prüfung
     * @param benutzer Die Benutzername des Prüfers
     * @param geraeteBarcode Der Barcode des Geräts
     * @param date Das Datum der Prüfung
     * @param bemerkungen Die Bemerkungen zu der Prüfung
     * @param password Das Passwort mit dem sich der Prüfer in der Programinstanz eingeloggt hat
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(long idPruefung, String benutzer, String geraeteBarcode, Date date, String bemerkungen, String password) {
        SQLiteDatabase db = getWritableDatabase();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ContentValues values = new ContentValues();
        values.put("idpruefung", idPruefung);
        values.put("benutzer", benutzer);
        values.put("geraete_barcode", geraeteBarcode);
        values.put("datum", dateFormat.format(date));
        values.put("bemerkungen", bemerkungen);
        values.put("passwort", password);
        long id = db.insert(this.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /**
     * Fügt ein Datensatz in die Datenbank ein. Für die ID wird die nächste nicht vergebene ID angewendet.
     * @param benutzer Die Benutzername des Prüfers
     * @param geraeteBarcode Der Barcode des Geräts
     * @param date Das Datum der Prüfung
     * @param bemerkungen Die Bemerkungen zu der Prüfung
     * @param password Das Passwort mit dem sich der Prüfer in der Programinstanz eingeloggt hat
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(String benutzer, String geraeteBarcode, Date date, String bemerkungen, String password) {
        return insertRow(this.getMaxID() + 1, benutzer, geraeteBarcode, date, bemerkungen, password);
    }

    /**
     * Fragt die Datensätze ab, die zu dem Gerät gehören
     * @param barcode Der Barcode des Geräts
     * @return Liste von ContentValues-Objekten, die die Daten der Datensätzen beinhalten
     */
    public ArrayList<ContentValues> getRowsByBarcode(String barcode) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContentValues> result = getContentValuesArrayFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE geraete_barcode = '" + barcode + "'", null));
        db.close();
        return result;
    }

    /**
     * Fragt die Datensätze ab, wo die Prüfungen von einem bestimmten Benutzer durchgeführt worden sind
     * @param benutzer Die Benutzername
     * @return Liste von ContentValues-Objekten, die die Daten der Datensätzen beinhalten
     */
    public ArrayList<ContentValues> getRowsByBenutzer(String benutzer) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContentValues> result = getContentValuesArrayFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE benutzer = '" + benutzer + "'", null));
        db.close();
        return result;
    }

    /**
     * Fragt das Passwort ab, das bei der Prüfung benutzt wurde
     * @param idPruefung ID der Prüfung
     * @return Das Passwort der Prüfung
     */
    public String getPasswordByIDPruefung(long idPruefung){
        SQLiteDatabase db = getReadableDatabase();
        String result = getContentValuesFromCursor(db.rawQuery("SELECT passwort FROM " + this.TABLE_NAME + " WHERE idpruefung = '" + idPruefung + "'", null)).getAsString("passwort");
        db.close();
        return result;
    }

    /**
     * Prüft ob das übergebene Passwort dem in der Datenbank gespeichertem Passwort entspricht
     * @param idPruefung ID der Prüfung
     * @param password Das Passwort das geprüft werden soll
     * @return True wenn die Passwörter übereinstimmen
     */
    public boolean isPasswordEqualByIDPruefung(long idPruefung, String password){
        return password.equals(getPasswordByIDPruefung(idPruefung));
    }
}
