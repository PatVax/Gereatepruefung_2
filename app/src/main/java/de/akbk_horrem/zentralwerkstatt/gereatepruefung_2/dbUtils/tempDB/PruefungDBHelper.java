package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PruefungDBHelper extends DBHelper {
    public PruefungDBHelper(Context applicationContext) {
        super(applicationContext, "Pruefung", 7);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + this.TABLE_NAME + " (IDPruefung INTEGER PRIMARY KEY, Benutzer TEXT, Geraete_Barcode TEXT, Datum TEXT, Bemerkungen TEXT, Password_Checked INTEGER, Password TEXT)");
    }

    public void insertRow(int idPruefung, int benutzer, String geraeteBarcode, Date date, String bemerkungen, boolean passwordChecked, String password) {
        SQLiteDatabase db = getWritableDatabase();
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        ContentValues values = new ContentValues();
        values.put("IDPruefung", Integer.valueOf(idPruefung));
        values.put("Benutzer", Integer.valueOf(benutzer));
        values.put("Geraete_Barcode", geraeteBarcode);
        values.put("Datum", dateFormat.format(Long.valueOf(date.getTime())));
        values.put("Bemerkungen", bemerkungen);
        values.put("Password_Checked", Integer.valueOf(passwordChecked ? 1 : 0));
        values.put("Password", password);
        db.insert(this.TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<ContentValues> getRowsByBarcode(String barcode) {
        return getContentValuesArrayFromCursor(getReadableDatabase().rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE Geraete_Barcode = '" + barcode + "'", null));
    }

    public ArrayList<ContentValues> getRowsByBenutzer(String benutzer) {
        return getContentValuesArrayFromCursor(getReadableDatabase().rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE Benutzer = '" + benutzer + "'", null));
    }

    public boolean isPasswordChecked(int idPruefung) {
        if (getReadableDatabase().rawQuery("SELECT Password_Checked FROM " + this.TABLE_NAME + " WHERE IDPruefung = '" + idPruefung + "'", null).getInt(0) == 1) {
            return true;
        }
        return false;
    }

    public void deleteRowByIDPruefung(int idPruefung) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE * FROM " + this.TABLE_NAME + " WHERE IDPruefung = '" + idPruefung + "'");
        db.close();
    }
}
