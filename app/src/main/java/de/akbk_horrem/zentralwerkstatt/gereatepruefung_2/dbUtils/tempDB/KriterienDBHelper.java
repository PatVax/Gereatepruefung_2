package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class KriterienDBHelper extends DBHelper {
    public KriterienDBHelper(Context applicationContext) {
        super(applicationContext, "Kriterien", 5);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (IDKriterium INTEGER PRIMARY KEY, IDGeraetetyp INTEGER, Text TEXT, Anzeigeart INTEGER, Status INTEGER)");
    }

    public void insertRow(int idKriterium, int idGeraetetyp, String text, String anzeigeart, int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("IDKriterium", Integer.valueOf(idKriterium));
        values.put("IDGeraetetyp", Integer.valueOf(idGeraetetyp));
        values.put("Text", text);
        values.put("Anzeigeart", anzeigeart);
        values.put("Status", Integer.valueOf(status));
        db.insert(this.TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContentValues> getKriterienByGeraetetyp(int idGeraetetyp) {
        return getContentValuesArrayFromCursor(getReadableDatabase().rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE idGeraetetyp = '" + idGeraetetyp + "' ORDER BY idKriterium ASC", null));
    }
}
