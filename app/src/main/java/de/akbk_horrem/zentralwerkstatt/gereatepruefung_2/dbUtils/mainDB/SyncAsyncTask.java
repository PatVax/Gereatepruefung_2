package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Pair;
import android.widget.Toast;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.BenutzerDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.DBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.GeraeteDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.GeraetetypDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.KriterienDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefergebnisseDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefungDBHelper;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class SyncAsyncTask extends AsyncTask<Void, Pair, Void> {
    private static final String SHARED_PREFERENCE = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private final Activity CONTEXT;
    private final ProgressDialog DIALOG;
    private boolean connection;
    private boolean dbAsyncStatus;
    DBHelper dbHelper;
    private boolean password = false;
    private SharedPreferences prefs;
    private ArrayList<ContentValues> result = null;

    public SyncAsyncTask(Activity currentActivity) {
        this.CONTEXT = currentActivity;
        this.prefs = this.CONTEXT.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.DIALOG = new ProgressDialog(this.CONTEXT);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.DIALOG.setMessage("");
        this.DIALOG.setTitle("");
        this.DIALOG.setCancelable(false);
        this.DIALOG.setIndeterminate(true);
        this.DIALOG.setProgressStyle(0);
        this.DIALOG.show();
    }

    protected Void doInBackground(Void... params) {
        if (checkConnection()) {
            ContentValues content;
            this.dbHelper = new BenutzerDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT benutzername, passwort, administrator FROM benutzer", "Benutzerdaten", "Benutzerdaten werden aktualisiert");
            for(ContentValues contentValues : result) {
                    ((BenutzerDBHelper) this.dbHelper).insertRow(contentValues.getAsString("benutzername"),
                            contentValues.getAsString("passwort"),
                            contentValues.getAsBoolean("administrator"));
            }

            ArrayList<Integer> ids = new ArrayList();

            this.dbHelper = new PruefungDBHelper(this.CONTEXT);
            ArrayList<ContentValues> contents = ((PruefungDBHelper) this.dbHelper).getRowsByBenutzer(this.prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), ""));
            for(ContentValues contentValues : result) {
                    if (contentValues.getAsInteger("Password_Checked").intValue() != 1) {
                        if (checkPruefungPassword(contentValues, "Prüfungen hochladen", "Prüfungen werden hochgeladen")) {
                            ids.add(contentValues.getAsInteger("IDPruefung"));
                        } else {
                            ((PruefungDBHelper) this.dbHelper).deleteRowByIDPruefung(contentValues.getAsInteger("IDPruefung"));
                        }
                    }
            }
            StringBuilder builder = new StringBuilder("INSERT INTO pruefungen (geraete_barcode, idbenutzer, datum, bemerkungen) VALUES ");
            for (int id : ids){
                builder.append("('" + (contents.get(id)).getAsString("Geraete_Barcode") +
                        "', '(SELECT idbenutzer FROM benutzer WHERE benutzername = " +
                        (contents.get(id)).getAsString("benutzer") + ")', CURDATE(), '" +
                        (contents.get(id)).getAsString("bemerkungen'),"));
            }
            builder.setCharAt(builder.length() - 1, ';');
            insertData(builder.toString().trim(), "Prüfungen hochladen", "Prüfungen werden hochgeladen");
            this.dbHelper = new PruefergebnisseDBHelper(this.CONTEXT);
            builder = new StringBuilder("INSERT INTO pruefergebnisse VALUES ");
            for (int id : ids){
                for (ContentValues contentValues : new ArrayList<ContentValues>(((PruefergebnisseDBHelper) this.dbHelper).getRowsByID(id))){
                    switch (contentValues.getAsString("anzeigeart")) {
                        case "b":
                            builder.append("((SELECT p.idpruefung FROM pruefungen p WHERE p.geraete_barcode = '" +
                                    contentValues.getAsString("geraete_barcode") + "' ORDER BY p.datum DESC LIMIT 1), " +
                                    contentValues.getAsString("idkriterium") + ", '" + (contentValues.getAsBoolean("Messwert") ? "true" : "false") + "'),");
                            break;
                        default:
                            builder.append("((SELECT p.idpruefung FROM pruefungen p WHERE p.geraete_barcode = '" +
                                    contentValues.getAsString("Geraete_Barcode") + "' ORDER BY p.Datum DESC LIMIT 1), " +
                                    contentValues.getAsString("idKriterium") + ", '" + contentValues.getAsString("Messwert") + "'),");
                            break;
                    }
                }
                builder.setCharAt(builder.length() - 1, ';');
                insertData(builder.toString().trim(), "Prüfungen hochladen", "Prüfergebnissen werden hochgeladen");
                ((PruefergebnisseDBHelper) this.dbHelper).deleteRowByIDPruefung(id);
            }

            this.dbHelper = new GeraeteDBHelper(this.CONTEXT);
            ids = new ArrayList();
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT g.geraete_barcode, g.idgeraetetyp, g.anschaffungsdatum, g.seriennummer FROM geraete g LEFT JOIN pruefungen p ON (g.geraete_barcode = p.geraete_barcode) WHERE DATEDIFF(CURDATE(), p.datum) >= 365 OR p.datum IS NULL", "Geräte", "Geräte werdem aktualisiert");
            for (ContentValues contentValues : result){
                ((GeraeteDBHelper) this.dbHelper).insertRow(contentValues.getAsString("geraete_barcode"), contentValues.getAsInteger("idgeraetetyp"), contentValues.getAsString("anschaffungsdatum"), contentValues.getAsString("seriennummer"));
                if (!ids.contains(contentValues.getAsInteger("idgeraetetyp"))) {
                    ids.add(contentValues.getAsInteger("idgeraetetyp"));
                }
            }

            this.dbHelper = new GeraetetypDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT g.idgeraetetyp, h.bezeichnung, g.headertext, g.footertext, g.bezeichnung FROM geraetetypen g INNER JOIN hersteller h ON (g.idhersteller = h.idhersteller)", "Geräte", "Gerätetypen werden aktualisiert");
            for (ContentValues contentValues : result){
                if (ids.contains(contentValues.getAsInteger("idgeraetetyp"))) {
                    ((GeraetetypDBHelper) this.dbHelper).insertRow(contentValues.getAsInteger("idgeraetetyp"), contentValues.getAsString("hersteller"), contentValues.getAsString("headertext"), contentValues.getAsString("footertext"), contentValues.getAsString("bezeichnung"));
                }
            }

            this.dbHelper = new KriterienDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT * FROM pruefkriterien WHERE status = TRUE", "Kriterien", "Kriterien werden aktualisiert");
            for (ContentValues contentValues : result){
                if (ids.contains(contentValues.getAsInteger("idgeraetetyp"))) {
                    ((KriterienDBHelper) this.dbHelper).insertRow(contentValues.getAsInteger("idkriterium"), contentValues.getAsInteger("idgeraetetyp"), contentValues.getAsString("text"), contentValues.getAsString("anzeigeart"), contentValues.getAsBoolean("status") ? 1 : 0);
                }
            }
        }
        return null;
    }

    protected void onPostExecute(Void o) {
        super.onPostExecute(o);
        this.DIALOG.hide();
        this.DIALOG.dismiss();
    }

    protected void onProgressUpdate(Pair... values) {
        super.onProgressUpdate(values);
        this.DIALOG.hide();
        try {
            switch ((AsyncTaskOperationEnum) values[2].second) {
                case LOGIN:
                    new DBAsyncTask(this.CONTEXT, new DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            if (((ContentValues) resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.LOGIN_SUCCES.getText())) {
                                SyncAsyncTask.this.password = true;
                            } else {
                                SyncAsyncTask.this.password = false;
                            }
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }, (String) values[6].second, (String) values[7].second).executeOnExecutor(THREAD_POOL_EXECUTOR, new String[]{AsyncTaskOperationEnum.LOGIN.getText(), "0"});
                    break;
                case CHECK_CONNECTION:
                    new DBAsyncTask(this.CONTEXT, new DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            if (((ContentValues) resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText())) {
                                SyncAsyncTask.this.connection = true;
                            } else {
                                SyncAsyncTask.this.connection = false;
                            }
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }).executeOnExecutor(THREAD_POOL_EXECUTOR, new String[]{AsyncTaskOperationEnum.CHECK_CONNECTION.getText(), "0"});
                    break;
                case GET_DATA:
                    new DBAsyncTask(this.CONTEXT, new DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            resultArray.remove(0);
                            SyncAsyncTask.this.result = resultArray;
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }).executeOnExecutor(THREAD_POOL_EXECUTOR, new String[]{((AsyncTaskOperationEnum) values[2].second).getText(), "0", (String) values[5].second});
                    break;
                case INSERT_DATA:
                    new DBAsyncTask(this.CONTEXT, new DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> resultArray) {
                            ContentValues result = (ContentValues) resultArray.get(0);
                            SyncAsyncTask.this.dbAsyncStatus = true;
                        }
                    }).executeOnExecutor(THREAD_POOL_EXECUTOR, new String[]{((AsyncTaskOperationEnum) values[2].second).getText(), "0", (String) values[5].second});
                    break;
            }
            this.DIALOG.show();
        } catch (MalformedURLException e) {
            Toast.makeText(this.CONTEXT, "URL nicht korrekt", 0);
        } catch (Throwable th) {
            this.DIALOG.show();
        }
    }

    private Pair[] createDBTaskList(AsyncTaskOperationEnum operation, String title, String message, int actualProgress, int maxProgress, String url, String benutzer, String password) {
        ArrayList<Pair> list = new ArrayList();
        list.add(new Pair("title", title));
        list.add(new Pair("message", message));
        list.add(new Pair("operation", operation));
        list.add(new Pair("actualProgress", Integer.valueOf(actualProgress)));
        list.add(new Pair("maxProgress", Integer.valueOf(maxProgress)));
        list.add(new Pair("url", url));
        list.add(new Pair("benutzer", benutzer));
        list.add(new Pair("password", password));
        return list.toArray(new Pair[list.size()]);
    }

    private boolean checkConnection() {
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.CHECK_CONNECTION, "", "", 0, 1, null, null, null));
        do {
        } while (!this.dbAsyncStatus);
        this.dbAsyncStatus = false;
        if (!this.connection) {
            return false;
        }
        this.connection = false;
        return true;
    }

    private boolean checkPruefungPassword(ContentValues content, String title, String message) {
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.LOGIN, title, message, 1, 5, null, content.getAsString("Benutzer"), content.getAsString("Password")));
        do {
        } while (!this.dbAsyncStatus);
        this.dbAsyncStatus = false;
        if (!this.password) {
            return false;
        }
        this.password = false;
        return true;
    }

    private void insertData(String query, String title, String message) {
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.INSERT_DATA, title, message, 1, 5, query, null, null));
        do {
        } while (!this.dbAsyncStatus);
        this.dbAsyncStatus = false;
    }

    private ArrayList<ContentValues> getData(String query, String title, String message) {
        publishProgress(createDBTaskList(AsyncTaskOperationEnum.GET_DATA, title, message, 1, 5, query, null, null));
        do {
        } while (!this.dbAsyncStatus);
        this.dbAsyncStatus = false;
        return this.result;
    }
}
