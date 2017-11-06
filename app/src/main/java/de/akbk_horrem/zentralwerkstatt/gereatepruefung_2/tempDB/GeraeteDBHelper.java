package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class GeraeteDBHelper extends DBHelper {
    public GeraeteDBHelper(Context applicationContext) {
        super(applicationContext, "Geraete", 4);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (Geraete_Barcode TEXT PRIMARY KEY, IDGeraetetyp INTEGER, Anschaffungsdatum TEXT, Seriennummer TEXT)");
    }

    public void insertRow(String geraeteBarcode, int idGeraetetyp, String anschaffungsdatum, String seriennummer) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Geraete_Barcode", geraeteBarcode);
        values.put("IDGeraetetyp", Integer.valueOf(idGeraetetyp));
        values.put("Anschaffungsdatum", anschaffungsdatum);
        values.put("Seriennummer", seriennummer);
        db.insert(this.TABLE_NAME, null, values);
        db.close();
    }

    public ContentValues getRowByBarcode(String barcode) {
        return getContentValuesFromCursor(getReadableDatabase().rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE Geraete_Barcode = '" + barcode + "'", null));
    }
}
