package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.Toast;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.BenutzerDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.DBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.GeraeteDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.GeraetetypDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.KriterienDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefergebnisseDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefungDBHelper;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * AsyncTask für die Synchronisierung der Hauptdatenbank mit der temporären Datenbank
 */
public class SyncAsyncTask extends AsyncTask<Void, ContentValues, Void> {
    private final Activity CONTEXT;
    private final ProgressDialog DIALOG;
    private DBConnectionStatusEnum dbConnectionStatusEnum;
    private boolean connection = false, dbAsyncStatus = false, password = false, insert = false;
    private ArrayList<ContentValues> result = null;

    /**
     * Erzeugt ein SyncAsyncTask-Objekt
     * @param currentActivity Aktuelle Activity die den Task ausführt
     */
    public SyncAsyncTask(Activity currentActivity) {
        this.CONTEXT = currentActivity;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.DIALOG.setMessage("");
        this.DIALOG.setTitle("Synchronisieren");
        this.DIALOG.setCancelable(false);
        this.DIALOG.setIndeterminate(false);
        this.DIALOG.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.DIALOG.setMax(6);
        this.DIALOG.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Verbindung wird geprüft
        if (checkConnection("Verbindung wird geprüft", 0)) {

            DBHelper dbHelper;

            //Benutzerdaten aktualisieren
            ArrayList<ContentValues> result;
            dbHelper = new BenutzerDBHelper(this.CONTEXT);
            ArrayList<ContentValues> benutzerBackup = dbHelper.getRows();
            ArrayList<ContentValues> userList = new ArrayList<>();
            try {
                result = getData("SELECT benutzername, passwort FROM benutzer", "Benutzerdaten werden aktualisiert", 1);
            }catch (SQLException e){
                return null;
            }
                dbHelper.deleteAllFromTable();
            for(ContentValues contentValues : result) {
                userList.add(contentValues);
                if(-1 == ((BenutzerDBHelper) dbHelper).insertRow(contentValues.getAsString("benutzername"),
                        DBUtils.encodeRootPasswort(contentValues.getAsString("passwort")))) {
                    dbHelper.insertAll(benutzerBackup);
                    return null;
                }
            }

            StringBuilder sqlBuilder = new StringBuilder();

            //Prüfungen aller Benutzer hochladen
            //Wenn das Passwort, dass zu der Zeitpunkt der Prüfung gespeichert wurde nicht mit dem aktuellen übereinstimmt werden diese gelöscht
            for(ContentValues userValues : userList){

                String user = userValues.getAsString("benutzername"),
                        password = DBUtils.encodeRootPasswort(userValues.getAsString("passwort"));

                dbHelper = new PruefungDBHelper(this.CONTEXT);

                ArrayList<ContentValues> resultPruefungen = ((PruefungDBHelper) dbHelper).getRowsByBenutzer(user);

                //Für jede Prüfung
                for(ContentValues contentValues : resultPruefungen) {
                    dbHelper = new PruefungDBHelper(this.CONTEXT);

                    String geraeteBarcode = contentValues.getAsString("geraete_barcode");
                    long currentID = contentValues.getAsLong("idpruefung");
                    if(((PruefungDBHelper) dbHelper).isPasswordEqualByIDPruefung(currentID, password)){
                        ArrayList<ContentValues> resultPruefergebnissen;
                        sqlBuilder.append("INSERT INTO pruefungen (geraete_barcode, idbenutzer, datum, bemerkungen) VALUES " +
                                "('" + geraeteBarcode +
                                "', (SELECT idbenutzer FROM benutzer WHERE benutzername = '" + user +
                                "'), CURDATE(), '" +
                                contentValues.getAsString("bemerkungen") + "');");

                        sqlBuilder.append("INSERT INTO pruefergebnisse VALUES ");

                        dbHelper = new PruefergebnisseDBHelper(this.CONTEXT);

                        resultPruefergebnissen = ((PruefergebnisseDBHelper) dbHelper).getRowsByIDPruefung(currentID);

                        dbHelper = new KriterienDBHelper(this.CONTEXT);
                        //Für jedes Prüfergebnis
                        for (ContentValues contentValues2 : resultPruefergebnissen){
                            switch (((KriterienDBHelper)dbHelper).getAnzeigeartByID(contentValues2.getAsLong("idkriterium"))) {
                                case "b":
                                    sqlBuilder.append("((SELECT p.idpruefung FROM pruefungen p WHERE p.geraete_barcode = '" +
                                            geraeteBarcode + "' ORDER BY p.idpruefung DESC LIMIT 1), " +
                                            contentValues2.getAsString("idkriterium") + ", '" + (contentValues2.getAsBoolean("messwert") ? "true" : "false") + "'),");
                                    break;
                                default:
                                    sqlBuilder.append("((SELECT p.idpruefung FROM pruefungen p WHERE p.geraete_barcode = '" +
                                            geraeteBarcode + "' ORDER BY p.idpruefung DESC LIMIT 1), " +
                                            contentValues2.getAsString("idkriterium") + ", '" + contentValues2.getAsString("messwert") + "'),");
                                    break;
                            }
                        }
                        sqlBuilder.setCharAt(sqlBuilder.length() - 1, ';');
                    }
                }
            }
            if(sqlBuilder.length() > 0)

                //Insert ausführen, bei Erfolg alle Prüfungen löschen
                if (insertData(sqlBuilder.toString().trim(), "Prüfungen werden hochgeladen", 2)) {
                    new PruefungDBHelper(this.CONTEXT).deleteAllFromTable();
                    new PruefergebnisseDBHelper(this.CONTEXT).deleteAllFromTable();
                }

                //Restliche Daten werden geladen, beim Misserfolg werden die Daten unberüht wiederhergestellt
            ArrayList<ContentValues> geraeteBackup = new GeraeteDBHelper(this.CONTEXT).getRows();
            ArrayList<ContentValues> geraeteTypenBackup = new GeraetetypDBHelper(this.CONTEXT).getRows();
            ArrayList<ContentValues> kriterienBackup = new KriterienDBHelper(this.CONTEXT).getRows();

            try {
                dbHelper = new GeraeteDBHelper(this.CONTEXT);
                dbHelper.deleteAllFromTable();
                ArrayList<Long> ids = new ArrayList();
                result = getData("SELECT g.geraete_barcode, g.idgeraetetyp, g.anschaffungsdatum, g.seriennummer FROM geraete g LEFT JOIN (SELECT MAX(datum) AS datum, geraete_barcode FROM pruefungen GROUP BY geraete_barcode) p ON (g.geraete_barcode = p.geraete_barcode) WHERE DATEDIFF(CURDATE(), p.datum) >= 365 OR p.datum IS NULL", "Geräte werden aktualisiert", 3);
                for (ContentValues contentValues : result) {
                    if(-1 == ((GeraeteDBHelper) dbHelper).insertRow(contentValues.getAsString("geraete_barcode"), contentValues.getAsInteger("idgeraetetyp"), contentValues.getAsString("anschaffungsdatum"), contentValues.getAsString("seriennummer"))) throw new SQLException();
                    if (!ids.contains(contentValues.getAsLong("idgeraetetyp"))) {
                        ids.add(contentValues.getAsLong("idgeraetetyp"));
                    }
                }

                dbHelper = new GeraetetypDBHelper(this.CONTEXT);
                dbHelper.deleteAllFromTable();
                ContentValues contentValues;
                for (long id : ids) {
                    contentValues = getData("SELECT g.idgeraetetyp, h.bezeichnung AS hersteller, g.headertext, g.footertext, g.bezeichnung FROM geraetetypen g INNER JOIN hersteller h ON (g.idhersteller = h.idhersteller) WHERE idgeraetetyp = " + id, "Gerätetypen werden aktualisiert", 4).get(0);
                    if(-1 == ((GeraetetypDBHelper) dbHelper).insertRow(contentValues.getAsInteger("idgeraetetyp"), contentValues.getAsString("hersteller"), contentValues.getAsString("headertext"), contentValues.getAsString("footertext"), contentValues.getAsString("bezeichnung"))) throw new SQLException();

                }

                dbHelper = new KriterienDBHelper(this.CONTEXT);
                dbHelper.deleteAllFromTable();
                for (long id : ids) {
                    result = getData("SELECT idkriterium, idgeraetetyp, text, anzeigeart FROM pruefkriterien WHERE status = TRUE AND idgeraetetyp = " + id, "Kriterien werden aktualisiert", 5);
                    for (ContentValues kriterien : result) {
                        if(-1 == ((KriterienDBHelper) dbHelper).insertRow(kriterien.getAsLong("idkriterium"), kriterien.getAsInteger("idgeraetetyp"), kriterien.getAsString("text"), kriterien.getAsString("anzeigeart"))) throw new SQLException();
                    }
                }
            } catch (SQLException e){
                dbHelper = new GeraeteDBHelper(this.CONTEXT);
                dbHelper.deleteAllFromTable();
                dbHelper.insertAll(geraeteBackup);
                dbHelper = new GeraetetypDBHelper(this.CONTEXT);
                dbHelper.deleteAllFromTable();
                dbHelper.insertAll(geraeteTypenBackup);
                dbHelper = new KriterienDBHelper(this.CONTEXT);
                dbHelper.deleteAllFromTable();
                dbHelper.insertAll(kriterienBackup);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void o) {
        super.onPostExecute(o);
        this.DIALOG.hide();
        this.DIALOG.dismiss();
    }

    @Override
    protected void onProgressUpdate(ContentValues... values) {
        super.onProgressUpdate(values);
        ContentValues contentValues = values[0];
        this.DIALOG.setMessage(contentValues.getAsString("message"));
        this.DIALOG.setProgress(contentValues.getAsInteger("actualProgress"));
        try {

            //Je nach Operation DBAsyncTask ausführen
            switch (contentValues.getAsString("operation")) {
                case "login":
                    DBAsyncTask.getLoginInstance(this.CONTEXT, new DBAsyncTask.DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            if ((resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.LOGIN_SUCCESS.getText())) {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.LOGIN_SUCCESS;
                                SyncAsyncTask.this.password = true;
                            } else {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.LOGIN_FAILED;
                                SyncAsyncTask.this.password = false;
                            }
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }, false, contentValues.getAsString("user"), contentValues.getAsString("password")).executeOnExecutor(AsyncTaskOperationEnum.LOGIN, false);
                    break;
                case "check_connection":
                    DBAsyncTask.getInstance(this.CONTEXT, new DBAsyncTask.DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            if ((resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText())) {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.CONNECTED;
                                SyncAsyncTask.this.connection = true;
                            } else {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.CONNECTION_FAILED;
                                SyncAsyncTask.this.connection = false;
                            }
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }, false).executeOnExecutor(AsyncTaskOperationEnum.CHECK_CONNECTION, false);
                    break;
                case "get_data":
                    DBAsyncTask.getInstance(this.CONTEXT, new DBAsyncTask.DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            if(resultArray.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText())) {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.SUCCESS;
                                resultArray.remove(0);
                                SyncAsyncTask.this.result = resultArray;
                                SyncAsyncTask.this.dbAsyncStatus = true;
                            } else {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.TRANSFER_FAILED;
                            }
                        }
                    }, false).executeOnExecutor(AsyncTaskOperationEnum.GET_DATA, false, contentValues.getAsString("sql"));
                    break;
                case "insert_data":
                    DBAsyncTask.getInstance(this.CONTEXT, new DBAsyncTask.DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            if((resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText())) {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.SUCCESS;
                                insert = true;
                            }
                            else {
                                dbConnectionStatusEnum = DBConnectionStatusEnum.INSERT_FAILED;
                                insert = false;
                            }
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }, false).executeOnExecutor(AsyncTaskOperationEnum.INSERT_DATA, false, contentValues.getAsString("sql"));
                    break;
            }
        } catch (MalformedURLException e) {
            Toast.makeText(this.CONTEXT, "URL nicht korrekt", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Erstellt ein ContentValues-Objekt, der folgende Parameter enthält
     * @param operation Die von DBAsyncTask auszuführende Operation
     * @param message Information die in dem Progress-Dialog angezeigt werden soll
     * @param actualProgress Ein int der darstellt bei welchem Schritt sich der Task gerade befindet
     * @param sql Die auszuführende SQL-Anweisungen
     * @param user Benutzer falls eingeloggt werden soll
     * @param password Passwort für den Benutzer
     * @return Eine ContenValues mit den Parametern
     */
    private ContentValues createDBTaskList(AsyncTaskOperationEnum operation, String message, int actualProgress, @Nullable String sql, @Nullable String user, @Nullable String password) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", message);
        contentValues.put("operation", operation.getText());
        contentValues.put("actualProgress", actualProgress);
        contentValues.put("sql", sql);
        contentValues.put("user", user);
        contentValues.put("password", password);
        return contentValues;
    }

    /**
     * Prüft die Verbindung mit der Datenbank
     * @param message Information die in dem Progress-Dialog angezeigt werden soll
     * @param actualProgress Ein int der darstellt bei welchem Schritt sich der Task gerade befindet
     * @return True wenn Verbindung erfolgreich war
     */
    private boolean checkConnection(String message, int actualProgress) {
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.CHECK_CONNECTION, message, actualProgress, null, null, null));
        do {
        } while (!this.dbAsyncStatus);
        this.dbAsyncStatus = false;
        if (!this.connection) {
            return false;
        }
        this.connection = false;
        return true;
    }

    /**
     * Fügt Daten in die Datenbank ein
     * @param query SQL-Injection
     * @param message Information die in dem Progress-Dialog angezeigt werden soll
     * @param actualProgress Ein int der darstellt bei welchem Schritt sich der Task gerade befindet
     * @return
     */
    private boolean insertData(String query, String message, int actualProgress) {
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.INSERT_DATA, message, actualProgress, query, null, null));
        do {
        } while (!this.dbAsyncStatus);
        if(dbConnectionStatusEnum != DBConnectionStatusEnum.SUCCESS) return false;
        this.dbAsyncStatus = false;
        return this.insert;
    }

    /**
     * Lädt Daten aus der Datenbank
     * @param query SQL-Query
     * @param message Information die in dem Progress-Dialog angezeigt werden soll
     * @param actualProgress Ein int der darstellt bei welchem Schritt sich der Task gerade befindet
     * @return
     * @throws SQLException
     */
    private ArrayList<ContentValues> getData(String query, String message, int actualProgress) throws SQLException{
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.GET_DATA, message, actualProgress, query, null, null));
        do {
        } while (!this.dbAsyncStatus);
        if(dbConnectionStatusEnum != DBConnectionStatusEnum.SUCCESS) throw new SQLException("SQL-Anfrage hat kein Ergebnis geliefert");
        this.dbAsyncStatus = false;
        return this.result;
    }
}
