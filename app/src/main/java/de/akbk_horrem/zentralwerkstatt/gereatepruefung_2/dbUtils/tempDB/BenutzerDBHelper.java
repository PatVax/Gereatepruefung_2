package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BenutzerDBHelper extends DBHelper {
    public BenutzerDBHelper(Context applicationContext) {
        super(applicationContext, "Benutzer", 3);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (Benutzername TEXT, Passwort TEXT, Admin INTEGER)");
    }

    public void insertRow(String benutzername, String passwort, boolean admin) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Benutzername", benutzername);
        values.put("Passwort", passwort);
        values.put("Admin", admin ? "1" : "0");
        db.insert(this.TABLE_NAME, null, values);
        db.close();
    }

    public boolean isAdmin(String benutzername) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Admin FROM " + this.TABLE_NAME + " WHERE Benutzername = '" + benutzername + "'", null);
        cursor.moveToFirst();
        if (cursor.getInt(0) == 1) {
            return true;
        }
        return false;
    }

    public ContentValues getBenutzerRow(String benutzername) {
        return getContentValuesFromCursor(getReadableDatabase().rawQuery("SELECT Benutzername FROM " + this.TABLE_NAME + " WHERE Benutzername = '" + benutzername + "'", null));
    }
}
