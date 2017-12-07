package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Die Klasse liefert allgemeine Methoden für den Datenbankzugriff. Es soll für jede Datenbanktabelle eine Klasse geben die von dieser Klasse erbt. Der Konstruktor der abgeleiteten Klasse soll den Konstruktor der Basisklasse aufrufen.
 */
public abstract class DBHelper extends SQLiteOpenHelper {
    protected final String TABLE_NAME;

    /**
     * Weist die allgemeinen Eigenschaften zu
     * @param applicationContext Aktueller Context von dem auf die Datenbank zugegriffen wird
     * @param name Name der Tabelle
     */
    protected DBHelper(Context applicationContext, String name) {
        super(applicationContext, name, null, 1);
        this.TABLE_NAME = name;
    }

    /**
     * Fügt ein Datensatz ein
     * @param contents ContenValues-Objekt der die erforderlichen Daten enthält. Die Keys müssen den Namen der Columns entsprechen.
     * @return ID des hinzugefügtes Datensatzes. -1 wenn nicht erfolgreich.
     */
    public long insertRow(ContentValues contents) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(this.TABLE_NAME, null, contents);
        db.close();
        return id;
    }

    /**
     * Fügt Datensätze ein
     * @param contentValuesArrayList Eine Liste die ContentValues-Objekte enthält, die die erforderlichen Daten enthalten. Die Keys müssen den Namen der Columns entsprechen.
     * @return true wenn insert erfolgreich war, false wenn nicht
     */
    public boolean insertAll(ArrayList<ContentValues> contentValuesArrayList){
        ArrayList<Long> ids = new ArrayList<>();
        for(ContentValues contentValues : contentValuesArrayList) {
            long id = this.insertRow(contentValues);
            if (id != -1) ids.add(id);
            else{
                for(long idToDelete : ids){
                    deleteFromTable(idToDelete);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Fragt ein Datensatz ab. Nur für Tabellen wo der Name der ID-Column den Format (id + tabellenname) hat.
     * @param id Die ID des Datensatzes
     * @return ContenValues-Objekt mit den Daten über dem Datensatz
     */
    public ContentValues getRow(long id) {
        SQLiteDatabase db = getReadableDatabase();
        ContentValues result = getContentValuesFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME + " WHERE id" + this.TABLE_NAME + " = " + id, null));
        db.close();
        return result;
    }

    /**
     * Fragt alle Datensätze aus der Tabelle ab
     * @return Alle Datensätze in der Tabelle als eine Liste von ContentValues-Objekten, die die Datensätze darstellen
     */
    public ArrayList<ContentValues> getRows() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<ContentValues> result = getContentValuesArrayFromCursor(db.rawQuery("SELECT * FROM " + this.TABLE_NAME, null));
        db.close();
        return result;
    }

    /**
     * Löscht einen Datensatz aus der Tabelle. Nur für Tabellen wo der Name der ID-Column den Format (id + tabellenname) hat.
     * @param id ID des zu löschenden Datensatzes
     * @return Anzahl der Datensätzen die gelöscht wurden
     */
    public int deleteFromTable(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int count = db.delete(this.TABLE_NAME, "id" + this.TABLE_NAME + " = ?", new String[] {id + ""});
        db.close();
        return count;
    }

    /**
     * Löscht alle Datensätze aus der Tabelle
     * @return Anzahl der Datensätzen die gelöscht wurden
     */
    public int deleteAllFromTable() {
        SQLiteDatabase db = getWritableDatabase();
        int count = db.delete(this.TABLE_NAME, null, null);
        db.close();
        return count;
    }

    /**
     * Fragt die größte vergebene ID in der Tabelle ab
     * @return Die größte vergebene ID in der Tabelle
     */
    protected long getMaxID(){
        SQLiteDatabase db = getReadableDatabase();
        long result;
        try {
            result = getContentValuesFromCursor(db.rawQuery("SELECT MAX(id" + this.TABLE_NAME + ") AS idpruefung FROM " + this.TABLE_NAME, null)).getAsLong("id" + this.TABLE_NAME);
        }catch(NullPointerException e){
            result = -1;
        }
        db.close();
        return result;
    }

    /**
     * Wandelt einen Cursor in eine Liste von ContenValues-Objekten, die die Datensätze darstellen
     * @param cursor Ein Cursor der durch ein SQL-Statement zurückgeliefert wurde
     * @return Die Umgewandelte Liste
     */
    protected ArrayList<ContentValues> getContentValuesArrayFromCursor(Cursor cursor) {
        ArrayList<ContentValues> contents = new ArrayList();
        if (cursor.moveToFirst()) {
            do { //Wiederhole
                ContentValues content = new ContentValues();
                //Für jede Column wird der Datentyp ermittelt und dem ContentValues-Objekt angefügt mit dem Columnname als Schlüssel
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_NULL:
                            content.putNull(cursor.getColumnName(i));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            content.put(cursor.getColumnName(i), cursor.getInt(i));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            content.put(cursor.getColumnName(i), cursor.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            content.put(cursor.getColumnName(i), cursor.getString(i));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            content.put(cursor.getColumnName(i), cursor.getBlob(i));
                            break;
                        default:
                            break;
                    }
                }
                contents.add(content); //ContentValues-Objekt zu der Liste hinzufügen
            } while (cursor.moveToNext()); //Solange Cursor einen weitere Position annehmen kann
        }    return contents;
    }

    /**
     * Wandelt den ersten Eintrag in einem Cursor in ein ContenValues-Objekt, der den Datensatz darstellt
     * @param cursor Ein Cursor der durch ein SQL-Statement zurückgeliefert wurde
     * @return Das Umgewandelte ContentValues-Objekt
     */
    protected ContentValues getContentValuesFromCursor(Cursor cursor) {
        ContentValues content = new ContentValues();
        if (cursor.moveToFirst()) {
            //Für jede Column wird der Datentyp ermittelt und dem ContentValues-Objekt angefügt mit dem Columnname als Schlüssel
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_NULL:
                        content.putNull(cursor.getColumnName(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        content.put(cursor.getColumnName(i), cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        content.put(cursor.getColumnName(i), cursor.getFloat(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        content.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
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
