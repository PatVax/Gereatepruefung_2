package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.Toast;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.json.JSONException;

/**
 * Eine Klasse für die Verbindung mit der uebergabe.php
 */
public class DBAsyncTask extends AsyncTask<String, ContentValues, ArrayList<ContentValues>> {
    private static final String SHARED_PREFERENCE = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private final String BENUTZER;
    private final Activity CONTEXT;
    private final ProgressDialog DIALOG;
    private final URL LINK;
    private final String PASSWORT;
    private final DBAsyncTask.DBAsyncResponse RESPONSE;
    private final String ROOT_PASSWORT;
    private final boolean SHOW_DIALOG;
    private SharedPreferences prefs;

    public interface DBAsyncResponse {

        /**
         * Wird aufgerufen wenn DBAsyncTask beendet wurde
         * @param resultArrayList Das Ergebnis aus dem Task. Erster Eintrag enthält Informationen über Erfolg des Tasks. Erster Eintrag muss entfernt werden bevor die Liste weitergegeben wird.
         */
        void processFinish(@Nullable ArrayList<ContentValues> resultArrayList);
    }

    /**
     * Startet das Task
     * @param asyncTaskOperationEnum Gibt an welche Operation ausgeführt werden soll
     * @param showToast Gibt an ob nach dem Task ein Toast erscheinen soll, der dem Nutzer über dem Erfolg oder Misserfolg des Tasks informiert
     * @param sql Stellt eine Query dar die in der Datenbank ausgeführt werden soll
     */
    public void execute (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast, String sql) {
        this.execute(asyncTaskOperationEnum.getText(), showToast ? "1" : "0", sql);
    }

    /**
     * Startet das Task
     * @param asyncTaskOperationEnum Gibt an welche Operation ausgeführt werden soll
     * @param showToast Gibt an ob nach dem Task ein Toast erscheinen soll, der dem Nutzer über dem Erfolg oder Misserfolg des Tasks informiert
     */
    public void execute (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast) {
        this.execute(asyncTaskOperationEnum.getText(), showToast ? "1" : "0");
    }

    /**
     * Startet das Task parallel
     * @param asyncTaskOperationEnum Gibt an welche Operation ausgeführt werden soll
     * @param showToast Gibt an ob nach dem Task ein Toast erscheinen soll, der dem Nutzer über dem Erfolg oder Misserfolg des Tasks informiert
     * @param sql Stellt eine Query dar die in der Datenbank ausgeführt werden soll
     */
    public void executeOnExecutor (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast, String sql) {
        this.executeOnExecutor(THREAD_POOL_EXECUTOR, asyncTaskOperationEnum.getText(), showToast ? "1" : "0", sql);
    }

    /**
     * Startet das Task parallel
     * @param asyncTaskOperationEnum Gibt an welche Operation ausgeführt werden soll
     * @param showToast Gibt an ob nach dem Task ein Toast erscheinen soll, der dem Nutzer über dem Erfolg oder Misserfolg des Tasks informiert
     */
    public void executeOnExecutor (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast) {
        this.executeOnExecutor(THREAD_POOL_EXECUTOR, asyncTaskOperationEnum.getText(), showToast ? "1" : "0");
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Einloggen
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param showDialog Gibt an ob ein Progressdialog während des Tasks erscheinen soll
     * @param benutzer Benutzername mit dem das Einloggen erfolgen soll
     * @param passwort Ein Passwort für den Benutzer
     * @return Eine DBAsyncTask Instanz
     * @throws MalformedURLException
     */
    public static DBAsyncTask getLoginInstance(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, boolean showDialog, String benutzer, String passwort) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response, showDialog, benutzer, passwort);
    }

    /**
     * Erzeugt ein standart DBAsyncTask Objekt
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param showDialog Gibt an ob ein Progressdialog während des Tasks erscheinen soll
     * @return Eine DBAsyncTask Instanz
     * @throws MalformedURLException
     */
    public static DBAsyncTask getInstance(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, boolean showDialog) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response, showDialog);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Verbindungschecks
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param showDialog Gibt an ob ein Progressdialog während des Tasks erscheinen soll
     * @param link Ein URL für die Verbindung
     * @param rootPasswort Das Passwort für den Zugriff an die Datenbank
     * @return Eine DBAsyncTask Instanz
     */
    public static DBAsyncTask getConnectionCheckInstance(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, boolean showDialog, URL link, String rootPasswort){
        return new DBAsyncTask(currentActivity, response, showDialog, link, rootPasswort);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Einloggen
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param benutzer Benutzername mit dem das Einloggen erfolgen soll
     * @param passwort Ein Passwort für den Benutzer
     * @return Eine DBAsyncTask Instanz
     * @throws MalformedURLException
     */
    public static DBAsyncTask getLoginInstance(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, String benutzer, String passwort) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response, benutzer, passwort);
    }

    /**
     * Erzeugt ein standart DBAsyncTask Objekt
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @return Eine DBAsyncTask Instanz
     * @throws MalformedURLException
     */
    public static DBAsyncTask getInstance(Activity currentActivity, DBAsyncTask.DBAsyncResponse response) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Verbindungschecks
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param link Ein URL für die Verbindung
     * @param rootPasswort Das Passwort für den Zugriff an die Datenbank
     * @return Eine DBAsyncTask Instanz
     */
    public static DBAsyncTask getConnectionCheckInstance(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, URL link, String rootPasswort){
        return new DBAsyncTask(currentActivity, response, link, rootPasswort);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Verbindungschecks
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param showDialog Gibt an ob ein Progressdialog während des Tasks erscheinen soll
     * @param link Ein URL für die Verbindung
     * @param rootPasswort Das Passwort für den Zugriff an die Datenbank
     */
    private DBAsyncTask(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, boolean showDialog, URL link, String rootPasswort) {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = link;
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = rootPasswort;
        this.RESPONSE = response;
        this.SHOW_DIALOG = showDialog;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Einloggen
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param showDialog Gibt an ob ein Progressdialog während des Tasks erscheinen soll
     * @param benutzer Benutzername mit dem das Einloggen erfolgen soll
     * @param passwort Ein Passwort für den Benutzer
     * @throws MalformedURLException
     */
    private DBAsyncTask(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, boolean showDialog, String benutzer, String passwort) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)));
        this.BENUTZER = benutzer;
        this.PASSWORT = DBUtils.encodePasswort(passwort);
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = showDialog;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    /**
     * Erzeugt ein standart DBAsyncTask Objekt
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param showDialog Gibt an ob ein Progressdialog während des Tasks erscheinen soll
     * @throws MalformedURLException
     */
    private DBAsyncTask(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, boolean showDialog) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)));
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = showDialog;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Verbindungschecks
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param link Ein URL für die Verbindung
     * @param rootPasswort Das Passwort für den Zugriff an die Datenbank
     */
    private DBAsyncTask(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, URL link, String rootPasswort) {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        this.CONTEXT = currentActivity;
        this.LINK = link;
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = rootPasswort;
        this.RESPONSE = response;
        this.SHOW_DIALOG = true;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    /**
     * Erzeugt ein DBAsyncTask Objekt für Einloggen
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param benutzer Benutzername mit dem das Einloggen erfolgen soll
     * @param passwort Ein Passwort für den Benutzer
     * @throws MalformedURLException
     */
    private DBAsyncTask(Activity currentActivity, DBAsyncTask.DBAsyncResponse response, String benutzer, String passwort) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)));
        this.BENUTZER = benutzer;
        this.PASSWORT = DBUtils.encodePasswort(passwort);
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = true;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    /**
     * Erzeugt ein standart DBAsyncTask Objekt
     * @param currentActivity Aktuelle Activity
     * @param response Eine Instanz des {@link DBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @throws MalformedURLException
     */
    private DBAsyncTask(Activity currentActivity, DBAsyncTask.DBAsyncResponse response) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)));
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = true;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(SHOW_DIALOG) {
            this.DIALOG.setMessage(" ");
            this.DIALOG.setTitle(" ");
            this.DIALOG.setCancelable(false);
            this.DIALOG.setIndeterminate(true);
            this.DIALOG.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            this.DIALOG.show();
        }
    }

    @Override
    protected ArrayList<ContentValues> doInBackground(String... params) {
        ArrayList<ContentValues> resultArray = new ArrayList();
        ContentValues result = new ContentValues();

        //Die Information weitergeben ob ein Toast erscheinen soll, der dem Nutzer über dem Erfolg oder Misserfolg des Tasks informiert
        result.put("showToast", params[1]);
        BufferedReader reader;
        String line;

        //Ja nach dem welche Operation durchgeführt werden soll
        switch (params[0])
        {
            //Verbindung wird geprüft
            case "check_connection":

                publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));

                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), checkConnection() ?
                        DBConnectionStatusEnum.CONNECTED.getText() :
                        DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                resultArray.add(result);
                return  resultArray;
            //Benutzer wird Eingeloggt
            case "login":
                publishProgress(createProgressList("Anmelden", "Sie werden angemeldet"));

                if (checkConnection()) {

                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), login().getText());

                } else
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                resultArray.add(result);
                return resultArray;
            //Benutzername für den Datenbankzugriff wird abgerufen
            case "get_database_user":

                publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));

                try {

                    if (checkConnection()) {
                        reader = getReader(AsyncTaskOperationEnum.GET_DATABASE_USER.getText(), null, this.ROOT_PASSWORT, this.LINK);
                        line = reader.readLine();
                        reader.close();
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), line != null ? DBConnectionStatusEnum.CONNECTED.getText() : DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                        result.put(DBConnectionStatusEnum.DATABASE_USER.getText(), line.toString().trim());

                    } else
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                } catch (IOException|KeyManagementException|NoSuchAlgorithmException e) {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                } finally {
                    resultArray.add(result);
                    return resultArray;
                }
            //Daten aus der SQL-Anfrage werden abgerufen
            case "get_data":

                publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));

                try {

                    if (checkConnection()) {

                        //SQL-Anfrage wird ausgeführt
                        publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
                        reader = getReader(params[0], params[2], this.ROOT_PASSWORT, this.LINK);

                        //Ergebnisse werden verarbeitet
                        publishProgress(createProgressList("Verarbeiten", "Daten werden verarbeitet"));
                        line = reader.readLine();

                        if (line.trim().equals("[]"))
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.BARCODE_FAILED.getText());

                        else if(line.trim().equals(""))
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());

                        else
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.SUCCESS.getText());

                        resultArray.add(result);

                        //Ergebnisse werden dem Ergebnis-Array angefügt
                        for (ContentValues contentValues : DBUtils.jsonArrayToContentValues(line.trim()))
                            resultArray.add(contentValues);

                    } else {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                        resultArray.add(result);
                    }

                } catch (IOException|KeyManagementException|NoSuchAlgorithmException|JSONException e) {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());

                } finally {
                    return resultArray;
                }
                //Daten werden mit einer SQL-Anfrage in die Datenbank eingefügt
            case "insert_data":
                publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));

                try {

                    if (checkConnection()) {

                        //SQL-Anfrage wird ausgeführt
                        publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
                        reader = getReader(params[0], params[2], this.ROOT_PASSWORT, this.LINK);
                        line = reader.readLine();

                        if (line.trim().equals("1"))
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.SUCCESS.getText());

                        else if(line.trim().equals("0"))
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.INSERT_FAILED.getText());

                        else result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());

                    } else
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                } catch (IOException|KeyManagementException|NoSuchAlgorithmException e) {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.INSERT_FAILED.getText());

                } finally {
                    resultArray.add(result);
                    return resultArray;
                }
            default:
                return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<ContentValues> resultArray) {
        super.onPostExecute(resultArray);
        Toast toast = null;
        if(SHOW_DIALOG) {
            this.DIALOG.hide();
            this.DIALOG.dismiss();
        }
        ContentValues result = resultArray.get(0);
        if (result.getAsString("showToast").equals("1")) {
            switch (result.getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()))
            {
                case "connection_failed":
                    toast = Toast.makeText(this.CONTEXT, "Verbindung Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
                case "success":
                    toast = Toast.makeText(this.CONTEXT, "Übertragung Erfolgreich", Toast.LENGTH_SHORT);
                    break;
                case "insert_failed":
                    toast = Toast.makeText(this.CONTEXT, "Einfügen der Daten Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
                case "connected":
                    toast = Toast.makeText(this.CONTEXT, "Verbindung wurde geprüft", Toast.LENGTH_SHORT);
                    break;
                case "login_failed":
                    toast = Toast.makeText(this.CONTEXT, "Anmeldung Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
                case "login_success":
                    toast = Toast.makeText(this.CONTEXT, "Anmeldung Erfolgreich", Toast.LENGTH_SHORT);
                    break;
                case "barcode_failed":
                    toast = Toast.makeText(this.CONTEXT, "Prüfung nicht hinterlegt", Toast.LENGTH_SHORT);
                    break;
                case "database_failed":
                    toast = Toast.makeText(this.CONTEXT, "Datenbank nicht erreicht", Toast.LENGTH_SHORT);
                    break;
                case "transfer_failed":
                    toast = Toast.makeText(this.CONTEXT, "Übertragung Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
            }
            if (toast != null) {
                toast.show();
            }
        }
        this.RESPONSE.processFinish(resultArray);
    }

    @Override
    protected void onProgressUpdate(ContentValues... values) {
        super.onProgressUpdate(values);
        if(SHOW_DIALOG){
            this.DIALOG.setTitle(values[0].getAsString("title"));
            this.DIALOG.setMessage(values[0].getAsString("message"));
        }
    }

    /**
     * Erzeugt ein ContentValues-Array, das in publishProgress Callback bearbeitet werden kann
     * @param title Titel des Dialogs
     * @param message Information des Dialogs
     * @return Die Parameter in ein ContentValues-Objekt zusammengepackt
     */
    private ContentValues[] createProgressList(String title, String message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("message", message);
        return new ContentValues[]{contentValues};
    }

    /**
     * Erzeugt eine BufferedReaderinstanz, die die Antwort aus dem Server darstellt
     * @param operationString Gibt an welche Operation an dem Server ausgeführt werden soll. Soll ein String sein der {@link AsyncTaskOperationEnum#getText()} Form sein
     * @param anfrage SQL-Anfrage an den Server
     * @param rootPasswort Das Passwort für die Datenbankverbindung
     * @param urlConnection Eine URL Instanz die zur Verbindung mit dem Server dient. Nur URLs die mit dem Protocol https(nicht getestet) oder http arbeiten.
     * @return Liefert einen BufferedReader mit dem Ergebnis aus dem Server zurück
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private BufferedReader getReader(String operationString, @Nullable String anfrage, String rootPasswort, URL urlConnection) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        URLConnection connection;

        //https nicht getestet
        if (urlConnection.getProtocol().equals("https")) {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new SecureRandom());
            URLConnection httpsConnection = urlConnection.openConnection();
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);
            httpsConnection.setConnectTimeout(5000);
            ((HttpsURLConnection)httpsConnection).setSSLSocketFactory(sc.getSocketFactory());
            httpsConnection.connect();
            connection = httpsConnection;
        } else if(urlConnection.getProtocol().equals("http")){
            URLConnection httpConnection = urlConnection.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setConnectTimeout(5000);
            httpConnection.connect();
            connection = httpConnection;
        } else throw new NoSuchAlgorithmException("Protocolformat wird nicht unterstützt");

        //Output Stream erzeugen
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(URLEncoder.encode("url", "ISO-8859-1") + "="
                + URLEncoder.encode(operationString, "ISO-8859-1") + "&"
                + URLEncoder.encode("sql", "ISO-8859-1") + "="
                + URLEncoder.encode(anfrage != null ? anfrage : "", "ISO-8859-1") + "&"
                + URLEncoder.encode("user", "ISO-8859-1") + "="
                + URLEncoder.encode(this.BENUTZER, "ISO-8859-1") + "&"
                + URLEncoder.encode("pass", "ISO-8859-1") + "="
                + URLEncoder.encode(this.PASSWORT, "ISO-8859-1") + "&"
                + URLEncoder.encode("rootPass", "ISO-8859-1") + "="
                + URLEncoder.encode(DBUtils.encodeRootPasswort(rootPasswort), "ISO-8859-1"));
        writer.flush();
        writer.close();

        //BufferedReader aus dem Input Stream erzeugen
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * Prüft ob diese Instanz eine erfolgreiche Verbindung aufbauen kann
     * @return True wenn die Verbindung erfolgreich war
     */
    private boolean checkConnection() {
        BufferedReader reader;
        String line;
        try {
            reader = getReader(AsyncTaskOperationEnum.CHECK_CONNECTION.getText(), null, this.ROOT_PASSWORT, this.LINK);
            line = reader.readLine();
            reader.close();

            return line.trim().equals("1") ? true : false;

        } catch (IOException|KeyManagementException|NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Prüft ob diese Instanz eine erfolgreiche Anmeldung durchführen kann
     * @return {@link DBConnectionStatusEnum#LOGIN_SUCCESS} wenn erfolgreich, {@link DBConnectionStatusEnum#LOGIN_FAILED} wenn Anmeldung nicht erfolgreich war, {@link DBConnectionStatusEnum#CONNECTION_FAILED} wenn die Verbindung fehlgeschlagen hat
     */
    private DBConnectionStatusEnum login(){
        BufferedReader reader;
        String line;

        try{
            reader = getReader(AsyncTaskOperationEnum.LOGIN.getText(), null, this.ROOT_PASSWORT, this.LINK);
            line = reader.readLine();
            reader.close();

            switch (line.trim()) {
                case "1":
                    return DBConnectionStatusEnum.LOGIN_SUCCESS;
                case "0":
                    return DBConnectionStatusEnum.LOGIN_FAILED;
                default:
                    return DBConnectionStatusEnum.CONNECTION_FAILED;
            }

        } catch (IOException|KeyManagementException|NoSuchAlgorithmException e) {
            return DBConnectionStatusEnum.CONNECTION_FAILED;
        }
    }
}
