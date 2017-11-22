package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public abstract class DBHelper extends SQLiteOpenHelper {
    protected final int COLUMN_COUNT;
    protected final String TABLE_NAME;

    public DBHelper(Context applicationContext, String name, int columnCount) {
        super(applicationContext, name, null, 1);
        this.TABLE_NAME = name;
        this.COLUMN_COUNT = columnCount;
    }

    public void onCreate(SQLiteDatabase db) {
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void insertRow(ContentValues contents) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(this.TABLE_NAME, null, contents);
        db.close();
    }

    public ContentValues getRow(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE ID" + this.TABLE_NAME + " = '" + id + "'", null);
        db.close();
        return getContentValuesFromCursor(cursor);
    }

    public ArrayList<ContentValues> getRows() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + this.TABLE_NAME, null);
        db.close();
        return getContentValuesArrayFromCursor(cursor);
    }

    public void deleteFromTable(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.rawQuery("Delete FROM " + this.TABLE_NAME + " WHERE ID" + this.TABLE_NAME + " = '" + id + "'", null);
        db.close();
    }

    public void deleteAllFromTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(this.TABLE_NAME, null, null);
        //db.rawQuery("DELETE FROM " + this.TABLE_NAME, null);
        db.close();
    }

    protected ArrayList<ContentValues> getContentValuesArrayFromCursor(Cursor cursor) {
        ArrayList<ContentValues> contents = new ArrayList();
        if (cursor.moveToFirst()) {
            do {
                ContentValues content = new ContentValues();
                for (int i = 0; i < this.COLUMN_COUNT; i++) {
                    switch (cursor.getType(i)) {
                        case 0:
                            content.putNull(cursor.getColumnName(i));
                            break;
                        case 1:
                            content.put(cursor.getColumnName(i), Integer.valueOf(cursor.getInt(i)));
                            break;
                        case 2:
                            content.put(cursor.getColumnName(i), Float.valueOf(cursor.getFloat(i)));
                            break;
                        case 3:
                            content.put(cursor.getColumnName(i), cursor.getString(i));
                            break;
                        case 4:
                            content.put(cursor.getColumnName(i), cursor.getBlob(i));
                            break;
                        default:
                            break;
                    }
                }
                contents.add(content);
            } while (cursor.moveToNext());
        }
        return contents;
    }

    protected ContentValues getContentValuesFromCursor(Cursor cursor) {
        ContentValues content = new ContentValues();
        if (cursor.moveToFirst()) {
            for (int i = 0; i < this.COLUMN_COUNT; i++) {
                switch (cursor.getType(i)) {
                    case 0:
                        content.putNull(cursor.getColumnName(i));
                        break;
                    case 1:
                        content.put(cursor.getColumnName(i), Integer.valueOf(cursor.getInt(i)));
                        break;
                    case 2:
                        content.put(cursor.getColumnName(i), Float.valueOf(cursor.getFloat(i)));
                        break;
                    case 3:
                        content.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                    case 4:
                        content.put(cursor.getColumnName(i), cursor.getBlob(i));
                        break;
                    default:
                        break;
                }
            }
        }
        return content;
    }
}
