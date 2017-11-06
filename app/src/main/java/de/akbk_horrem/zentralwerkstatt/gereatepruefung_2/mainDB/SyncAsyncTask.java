package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Pair;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.BenutzerDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.DBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.GeraeteDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.GeraetetypDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.KriterienDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.PruefergebnisseDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.tempDB.PruefungDBHelper;
import java.util.ArrayList;
import java.util.Iterator;

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

    class C04051 implements DBAsyncResponse {
        C04051() {
        }

        public void processFinish(ArrayList<ContentValues> resultArray) {
            if (((ContentValues) resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.LOGIN_SUCCES.getText())) {
                SyncAsyncTask.this.password = true;
            } else {
                SyncAsyncTask.this.password = false;
            }
            SyncAsyncTask.this.dbAsyncStatus = true;
        }
    }

    class C04062 implements DBAsyncResponse {
        C04062() {
        }

        public void processFinish(ArrayList<ContentValues> resultArray) {
            if (((ContentValues) resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText())) {
                SyncAsyncTask.this.connection = true;
            } else {
                SyncAsyncTask.this.connection = false;
            }
            SyncAsyncTask.this.dbAsyncStatus = true;
        }
    }

    class C04073 implements DBAsyncResponse {
        C04073() {
        }

        public void processFinish(ArrayList<ContentValues> resultArray) {
            resultArray.remove(0);
            SyncAsyncTask.this.result = resultArray;
            SyncAsyncTask.this.dbAsyncStatus = true;
        }
    }

    class C04084 implements DBAsyncResponse {
        C04084() {
        }

        public void processFinish(ArrayList<ContentValues> resultArray) {
            ContentValues result = (ContentValues) resultArray.get(0);
            SyncAsyncTask.this.dbAsyncStatus = true;
        }
    }

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
            int id;
            this.dbHelper = new BenutzerDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT Benutzername, Passwort, Administrator FROM Benutzer", "Benutzerdaten", "Benutzerdaten werden aktualisiert");
            Iterator it = this.result.iterator();
            while (it.hasNext()) {
                content = (ContentValues) it.next();
                ((BenutzerDBHelper) this.dbHelper).insertRow(content.getAsString("Benutzername"), content.getAsString("Passwort"), content.getAsBoolean("Administrator").booleanValue());
            }
            ArrayList<Integer> ids = new ArrayList();
            this.dbHelper = new GeraeteDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT g.Geräte_Barcode, g.IDGerätetyp, g.Anschaffungsdatum, g.Seriennummer FROM Geräte g LEFT JOIN Prüfungen p ON (g.Geräte_Barcode = p.Geräte_Barcode) WHERE DATEDIFF(CURDATE(), p.Datum) >= 365 OR p.Datum IS NULL", "Geräte", "Geräte werdem aktualisiert");
            it = this.result.iterator();
            while (it.hasNext()) {
                content = (ContentValues) it.next();
                ((GeraeteDBHelper) this.dbHelper).insertRow(content.getAsString("Geräte_Barcode"), content.getAsInteger("IDGerätetyp").intValue(), content.getAsString("Anschaffungsdatum"), content.getAsString("Seriennummer"));
                if (!ids.contains(content.getAsInteger("IDGerätetyp"))) {
                    ids.add(content.getAsInteger("IDGerätetyp"));
                }
            }
            this.dbHelper = new GeraetetypDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT g.IDGerätetyp, h.Bezeichnung, g.HeaderText, g.FooterText, g.Bezeichnung FROM Gerätetypen g INNER JOIN Hersteller h ON (g.IDHersteller = h.IDHersteller)", "Geräte", "Gerätetypen werden aktualisiert");
            Iterator it2 = this.result.iterator();
            while (it2.hasNext()) {
                content = (ContentValues) it2.next();
                if (ids.contains(content.getAsInteger("IDGerätetyp"))) {
                    ((GeraetetypDBHelper) this.dbHelper).insertRow(content.getAsInteger("IDGerätetyp").intValue(), content.getAsString("Hersteller"), content.getAsString("HeaderText"), content.getAsString("FooterText"), content.getAsString("Bezeichnung"));
                }
            }
            this.dbHelper = new KriterienDBHelper(this.CONTEXT);
            this.dbHelper.deleteAllFromTable();
            this.result = getData("SELECT * FROM Prüfkriterien WHERE Status = TRUE", "Kriterien", "Kriterien werden aktualisiert");
            it2 = this.result.iterator();
            while (it2.hasNext()) {
                content = (ContentValues) it2.next();
                if (ids.contains(content.getAsInteger("IDGerätetyp"))) {
                    ((KriterienDBHelper) this.dbHelper).insertRow(content.getAsInteger("IDKriterium").intValue(), content.getAsInteger("IDGerätetyp").intValue(), content.getAsString("Text"), content.getAsString("Anzeigeart"), content.getAsBoolean("Status").booleanValue() ? 1 : 0);
                }
            }
            this.dbHelper = new PruefungDBHelper(this.CONTEXT);
            ArrayList<ContentValues> contents = ((PruefungDBHelper) this.dbHelper).getRowsByBenutzer(this.prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), ""));
            ids = new ArrayList();
            it = contents.iterator();
            while (it.hasNext()) {
                content = (ContentValues) it.next();
                if (content.getAsInteger("Password_Checked").intValue() != 1) {
                    if (checkPruefungPassword(content, "Prüfungen hochladen", "Prüfungen werden hochgeladen")) {
                        ids.add(content.getAsInteger("idPruefung"));
                    } else {
                        ((PruefungDBHelper) this.dbHelper).deleteRowByIDPruefung(content.getAsInteger("idPruefung").intValue());
                    }
                }
            }
            StringBuilder builder = new StringBuilder("INSERT INTO prüfungen (Geräte_Barcode, idBenutzer, Datum, Bemerkungen) VALUES ");
            it = ids.iterator();
            while (it.hasNext()) {
                id = ((Integer) it.next()).intValue();
                builder.append("('" + ((ContentValues) contents.get(id)).getAsString("Geraete_Barcode") + "', '(SELECT IDBenutzer FROM Benutzer WHERE Benutzername = " + ((ContentValues) contents.get(id)).getAsString("Benutzer") + ")', CURDATE(), '" + ((ContentValues) contents.get(id)).getAsString("Bemerkungen'),"));
            }
            builder.setCharAt(builder.length() - 1, ';');
            insertData(builder.toString().trim(), "Prüfungen hochladen", "Prüfungen werden hochgeladen");
            this.dbHelper = new PruefergebnisseDBHelper(this.CONTEXT);
            builder = new StringBuilder("INSERT INTO prüfergebnisse VALUES ");
            it = ids.iterator();
            while (it.hasNext()) {
                id = ((Integer) it.next()).intValue();
                Iterator it3 = ((PruefergebnisseDBHelper) this.dbHelper).getRowsByID(id).iterator();
                while (it3.hasNext()) {
                    String asString = ((ContentValues) it3.next()).getAsString("anzeigeart");
                    Object obj = -1;
                    switch (asString.hashCode()) {
                        case 98:
                            if (asString.equals("b")) {
                                obj = null;
                                break;
                            }
                            break;
                    }
                    switch (obj) {
                        case null:
                            builder.append("((SELECT p.IDPrüfung FROM prüfungen p WHERE p.geräte_barcode = '" + ((ContentValues) contents.get(id)).getAsString("Geraete_Barcode") + "' ORDER BY p.Datum DESC LIMIT 1), " + ((ContentValues) contents.get(id)).getAsString("idKriterium") + ", '" + (((ContentValues) contents.get(id)).getAsBoolean("Messwert").booleanValue() ? "true" : "false") + "'),");
                            break;
                        default:
                            builder.append("((SELECT p.IDPrüfung FROM prüfungen p WHERE p.geräte_barcode = '" + ((ContentValues) contents.get(id)).getAsString("Geraete_Barcode") + "' ORDER BY p.Datum DESC LIMIT 1), " + ((ContentValues) contents.get(id)).getAsString("idKriterium") + ", '" + ((ContentValues) contents.get(id)).getAsBoolean("Messwert") + "'),");
                            break;
                    }
                }
                builder.setCharAt(builder.length() - 1, ';');
                insertData(builder.toString().trim(), "Prüfungen hochladen", "Prüfergebnissen werden hochgeladen");
            }
        }
        return null;
    }

    protected void onPostExecute(Void o) {
        super.onPostExecute(o);
        this.DIALOG.hide();
        this.DIALOG.dismiss();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onProgressUpdate(Pair... r7) {
        /*
        r6 = this;
        super.onProgressUpdate(r7);
        r1 = r6.DIALOG;
        r1.hide();
        r2 = de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.SyncAsyncTask.C02735.f16xba0d6dfc;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = 2;
        r1 = r7[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = (de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum) r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.ordinal();	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r2[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        switch(r1) {
            case 1: goto L_0x0020;
            case 2: goto L_0x0060;
            case 3: goto L_0x008a;
            case 4: goto L_0x00bd;
            default: goto L_0x001a;
        };
    L_0x001a:
        r1 = r6.DIALOG;
        r1.show();
    L_0x001f:
        return;
    L_0x0020:
        r3 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.DBAsyncTask;	 Catch:{ MalformedURLException -> 0x0051 }
        r4 = r6.CONTEXT;	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.SyncAsyncTask$1;	 Catch:{ MalformedURLException -> 0x0051 }
        r5.<init>();	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = 6;
        r1 = r7[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = (java.lang.String) r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r2 = 7;
        r2 = r7[r2];	 Catch:{ MalformedURLException -> 0x0051 }
        r2 = r2.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r2 = (java.lang.String) r2;	 Catch:{ MalformedURLException -> 0x0051 }
        r3.<init>(r4, r5, r1, r2);	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = THREAD_POOL_EXECUTOR;	 Catch:{ MalformedURLException -> 0x0051 }
        r2 = 2;
        r2 = new java.lang.String[r2];	 Catch:{ MalformedURLException -> 0x0051 }
        r4 = 0;
        r5 = de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum.LOGIN;	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = r5.getText();	 Catch:{ MalformedURLException -> 0x0051 }
        r2[r4] = r5;	 Catch:{ MalformedURLException -> 0x0051 }
        r4 = 1;
        r5 = "0";
        r2[r4] = r5;	 Catch:{ MalformedURLException -> 0x0051 }
        r3.executeOnExecutor(r1, r2);	 Catch:{ MalformedURLException -> 0x0051 }
        goto L_0x001a;
    L_0x0051:
        r0 = move-exception;
        r1 = r6.CONTEXT;	 Catch:{ all -> 0x0083 }
        r2 = "URL nicht korrekt";
        r3 = 0;
        android.widget.Toast.makeText(r1, r2, r3);	 Catch:{ all -> 0x0083 }
        r1 = r6.DIALOG;
        r1.show();
        goto L_0x001f;
    L_0x0060:
        r1 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.DBAsyncTask;	 Catch:{ MalformedURLException -> 0x0051 }
        r2 = r6.CONTEXT;	 Catch:{ MalformedURLException -> 0x0051 }
        r3 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.SyncAsyncTask$2;	 Catch:{ MalformedURLException -> 0x0051 }
        r3.<init>();	 Catch:{ MalformedURLException -> 0x0051 }
        r1.<init>(r2, r3);	 Catch:{ MalformedURLException -> 0x0051 }
        r2 = THREAD_POOL_EXECUTOR;	 Catch:{ MalformedURLException -> 0x0051 }
        r3 = 2;
        r3 = new java.lang.String[r3];	 Catch:{ MalformedURLException -> 0x0051 }
        r4 = 0;
        r5 = de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum.CHECK_CONNECTION;	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = r5.getText();	 Catch:{ MalformedURLException -> 0x0051 }
        r3[r4] = r5;	 Catch:{ MalformedURLException -> 0x0051 }
        r4 = 1;
        r5 = "0";
        r3[r4] = r5;	 Catch:{ MalformedURLException -> 0x0051 }
        r1.executeOnExecutor(r2, r3);	 Catch:{ MalformedURLException -> 0x0051 }
        goto L_0x001a;
    L_0x0083:
        r1 = move-exception;
        r2 = r6.DIALOG;
        r2.show();
        throw r1;
    L_0x008a:
        r2 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.DBAsyncTask;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r6.CONTEXT;	 Catch:{ MalformedURLException -> 0x0051 }
        r3 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.SyncAsyncTask$3;	 Catch:{ MalformedURLException -> 0x0051 }
        r3.<init>();	 Catch:{ MalformedURLException -> 0x0051 }
        r2.<init>(r1, r3);	 Catch:{ MalformedURLException -> 0x0051 }
        r3 = THREAD_POOL_EXECUTOR;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = 3;
        r4 = new java.lang.String[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = 0;
        r1 = 2;
        r1 = r7[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = (de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum) r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.getText();	 Catch:{ MalformedURLException -> 0x0051 }
        r4[r5] = r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = 1;
        r5 = "0";
        r4[r1] = r5;	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = 2;
        r1 = 5;
        r1 = r7[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = (java.lang.String) r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r4[r5] = r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r2.executeOnExecutor(r3, r4);	 Catch:{ MalformedURLException -> 0x0051 }
        goto L_0x001a;
    L_0x00bd:
        r2 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.DBAsyncTask;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r6.CONTEXT;	 Catch:{ MalformedURLException -> 0x0051 }
        r3 = new de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.SyncAsyncTask$4;	 Catch:{ MalformedURLException -> 0x0051 }
        r3.<init>();	 Catch:{ MalformedURLException -> 0x0051 }
        r2.<init>(r1, r3);	 Catch:{ MalformedURLException -> 0x0051 }
        r3 = THREAD_POOL_EXECUTOR;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = 3;
        r4 = new java.lang.String[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = 0;
        r1 = 2;
        r1 = r7[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = (de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum) r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.getText();	 Catch:{ MalformedURLException -> 0x0051 }
        r4[r5] = r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = 1;
        r5 = "0";
        r4[r1] = r5;	 Catch:{ MalformedURLException -> 0x0051 }
        r5 = 2;
        r1 = 5;
        r1 = r7[r1];	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = r1.second;	 Catch:{ MalformedURLException -> 0x0051 }
        r1 = (java.lang.String) r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r4[r5] = r1;	 Catch:{ MalformedURLException -> 0x0051 }
        r2.executeOnExecutor(r3, r4);	 Catch:{ MalformedURLException -> 0x0051 }
        goto L_0x001a;
        */
        throw new UnsupportedOperationException("Method not decompiled: de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB.SyncAsyncTask.onProgressUpdate(android.util.Pair[]):void");
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
        return (Pair[]) list.toArray(new Pair[list.size()]);
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
