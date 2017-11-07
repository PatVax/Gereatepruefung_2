package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class PruefergebnisseDBHelper extends DBHelper {
    public PruefergebnisseDBHelper(Context applicationContext) {
        super(applicationContext, "Pruefergebnisse", 3);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (IDPruefung INTEGER, IDKriterium INTEGER, Messwert INTEGER)");
    }

    public void insertRow(int idErgebniss, int idPruefung, int idKriterium, int messwert) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("IDPruefung", Integer.valueOf(idPruefung));
        values.put("IDKriterium", Integer.valueOf(idKriterium));
        values.put("Messwert", Integer.valueOf(messwert));
        db.insert(this.TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContentValues> getRowsByID(int idPruefung) {
        return getContentValuesArrayFromCursor(getReadableDatabase().rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE IDPruefung = '" + idPruefung + "'", null));
    }

    public void deleteRowByIDPruefung (int idPruefung)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE * FROM " + this.TABLE_NAME + " WHERE IDPruefung = '" + idPruefung + "'");
        db.close();
    }
}
