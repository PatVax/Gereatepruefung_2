package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;

/**
 * Eine Klasse für die Datenbanktabelle benutzer
 */
public class BenutzerDBHelper extends DBHelper {

    /**
     * Erzeugt neues BenutzerDBHelper-Objekt
     * @param applicationContext Context das dieses Objekt erzeugt
     */
    public BenutzerDBHelper(Context applicationContext) {
        super(applicationContext, "benutzer");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (benutzername TEXT, passwort TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    /**
     * Fügt ein Datensatz in die Datenbank ein
     * @param benutzername Die Benutzername
     * @param passwort Das Passwort
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(String benutzername, String passwort) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("benutzername", benutzername);
        values.put("passwort", passwort);
        long id = db.insert(this.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /**
     * Fragt ein Datensatz ab
     * @param benutzername Die Benutzername für die der Datensatz ermittelt werden soll
     * @return ContentValues mit dem Datensatz
     */
    public ContentValues getBenutzerRowByBenutzername(String benutzername) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues result = getContentValuesFromCursor(db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE benutzername = '" + benutzername + "'", null));
        db.close();
        return result;
    }

    /**
     * Fragt das Passwort des Benutzers ab
     * @param benutzername Die Benutzername für die das Passwort ermittelt werden soll
     * @return Das Passwort als String
     */
    public String getPasswordByBenutzername(String benutzername){
        SQLiteDatabase db = getReadableDatabase();
        String result = getContentValuesFromCursor(db.rawQuery("SELECT passwort FROM " + TABLE_NAME + " WHERE benutzername = '" + benutzername + "'", null)).getAsString("passwort");
        db.close();
        return result;
    }
}
