package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class GeraetetypDBHelper extends DBHelper {
    public GeraetetypDBHelper(Context applicationContext) {
        super(applicationContext, "GeraeteDBHelper", 5);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (IDGeraetetyp INTEGER PRIMARY KEY, HerstellerName TEXT, HeaderText TEXT, FooterText TEXT, GeraeteName TEXT)");
    }

    public void insertRow(int idGeraetetyp, String hersteller, String HeaderText, String FooterText, String Bezeichnung) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("IDGeraetetyp", Integer.valueOf(idGeraetetyp));
        values.put("Hersteller", hersteller);
        values.put("HeaderText", HeaderText);
        values.put("FooterText", FooterText);
        values.put("Bezeichnung", Bezeichnung);
        db.insert(this.TABLE_NAME, null, values);
        db.close();
    }

    public ContentValues getGeraetetypByID(int geraetetyp) {
        return getContentValuesFromCursor(getReadableDatabase().rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE IDGeraetetyp = '" + geraetetyp + "'", null));
    }
}
