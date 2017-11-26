package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.widget.Toast;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBAsyncTask extends AsyncTask<String, Pair, ArrayList<ContentValues>> {
    private static final String SHARED_PREFERENCE = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private final String BENUTZER;
    private final Activity CONTEXT;
    private final ProgressDialog DIALOG;
    private final URL LINK;
    private final String PASSWORT;
    private final DBAsyncResponse RESPONSE;
    private final String ROOT_PASSWORT;
    private final boolean SHOW_DIALOG;
    private SharedPreferences prefs;

    public void execute (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast, String sql) {
        this.execute(asyncTaskOperationEnum.getText(), showToast ? "1" : "0", sql);
    }

    public void execute (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast) {
        this.execute(asyncTaskOperationEnum.getText(), showToast ? "1" : "0");
    }

    public void executeOnExecutor (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast, String sql) {
        this.executeOnExecutor(THREAD_POOL_EXECUTOR, asyncTaskOperationEnum.getText(), showToast ? "1" : "0", sql);
    }

    public void executeOnExecutor (AsyncTaskOperationEnum asyncTaskOperationEnum, boolean showToast) {
        this.executeOnExecutor(THREAD_POOL_EXECUTOR, asyncTaskOperationEnum.getText(), showToast ? "1" : "0");
    }

    public static DBAsyncTask getLoginInstance(Activity currentActivity, DBAsyncResponse response, boolean showDialog, String benutzer, String passwort) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response, showDialog, benutzer, passwort);
    }

    public static DBAsyncTask getInstance(Activity currentActivity, DBAsyncResponse response, boolean showDialog) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response, showDialog);
    }

    public static DBAsyncTask getConnectionCheckInstance(Activity currentActivity, DBAsyncResponse response, boolean showDialog, URL link, String rootPasswort){
        return new DBAsyncTask(currentActivity, response, showDialog, link, rootPasswort);
    }

    public static DBAsyncTask getLoginInstance(Activity currentActivity, DBAsyncResponse response, String benutzer, String passwort) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response, benutzer, passwort);
    }

    public static DBAsyncTask getInstance(Activity currentActivity, DBAsyncResponse response) throws MalformedURLException {
        return new DBAsyncTask(currentActivity, response);
    }

    public static DBAsyncTask getConnectionCheckInstance(Activity currentActivity, DBAsyncResponse response, URL link, String rootPasswort){
        return new DBAsyncTask(currentActivity, response, link, rootPasswort);
    }

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response, boolean showDialog, URL link, String rootPasswort) {
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

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response, boolean showDialog, String benutzer, String passwort) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", new Object[]{this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)}));
        this.BENUTZER = benutzer;
        this.PASSWORT = encodePasswort(passwort);
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = showDialog;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response, boolean showDialog) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", new Object[]{this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)}));
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = showDialog;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response, URL link, String rootPasswort) {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = link;
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = rootPasswort;
        this.RESPONSE = response;
        this.SHOW_DIALOG = true;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response, String benutzer, String passwort) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", new Object[]{this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)}));
        this.BENUTZER = benutzer;
        this.PASSWORT = encodePasswort(passwort);
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = true;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response) throws MalformedURLException {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = new URL(String.format("%s/%s", new Object[]{this.prefs.getString(SharedPreferenceEnum.HOST.getText(), null), this.prefs.getString(SharedPreferenceEnum.PFAD.getText(), null)}));
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = this.prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), "");
        this.RESPONSE = response;
        this.SHOW_DIALOG = true;
        this.DIALOG = new ProgressDialog(currentActivity);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        if(SHOW_DIALOG) {
            this.DIALOG.setMessage("");
            this.DIALOG.setTitle("");
            this.DIALOG.setCancelable(false);
            this.DIALOG.setIndeterminate(true);
            this.DIALOG.setProgressStyle(0);
            this.DIALOG.show();
        }
    }

    protected ArrayList<ContentValues> doInBackground(String... params) {
        ArrayList<ContentValues> resultArray = new ArrayList();
        ContentValues result = new ContentValues();
        result.put("showToast", params[1]);
        BufferedReader reader;
        String line;
        switch (params[0])
        {
            case "check_connection":

                publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));

                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), checkConnection() ?
                        DBConnectionStatusEnum.CONNECTED.getText() :
                        DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                resultArray.add(result);
                return  resultArray;
            case "login":
                publishProgress(createProgressList("Anmelden", "Sie werden angemeldet"));

                if (checkConnection()) {

                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), login().getText());

                } else
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                resultArray.add(result);
                return resultArray;
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
            case "get_data":

                publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));

                try {

                    if (checkConnection()) {
                        publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
                        reader = getReader(params[0], params[2], this.ROOT_PASSWORT, this.LINK);
                        publishProgress(createProgressList("Verarbeiten", "Daten werden verarbeitet"));
                        line = reader.readLine();

                        if (line.trim().equals("[]"))
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.BARCODE_FAILED.getText());

                        else
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.SUCCESS.getText());

                        resultArray.add(result);

                        for (ContentValues contentValues : jsonArrayToContentValues(line.trim()))
                            resultArray.add(contentValues);

                    } else
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());

                } catch (IOException|KeyManagementException|NoSuchAlgorithmException|JSONException e) {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());

                } finally {;
                    return resultArray;
                }
            case "insert_data":
                publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));

                try {

                    if (checkConnection()) {

                        publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
                        reader = getReader(params[0], params[2], this.ROOT_PASSWORT, this.LINK);
                        line = reader.readLine();

                        if (line.trim().equals("1"))
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.SUCCESS.getText());

                        else
                            result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.INSERT_FAILED.getText());

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

    protected void onProgressUpdate(Pair... values) {
        super.onProgressUpdate(values);
        if(SHOW_DIALOG)
            for (int i = 0; i < values.length; i++) {
                if (values[i].first.equals("title")) {
                    this.DIALOG.setTitle((String) values[i].second);
                } else if (values[i].first.equals("message")) {
                    this.DIALOG.setMessage((String) values[i].second);
                }
            }
    }

    private static ArrayList<ContentValues> jsonArrayToContentValues(String jsonString) throws JSONException {
        ArrayList<ContentValues> outputArray = new ArrayList();
        if (jsonString == null) {
            throw new JSONException("Given String is null");
        } else {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                ContentValues output = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                Iterator<String> iterator = object.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    output.put(key, object.getString(key));
                }
                outputArray.add(output);
            }
            return outputArray;
        }
    }

    private Pair[] createProgressList(String title, String message) {
        ArrayList<Pair> list = new ArrayList();
        list.add(new Pair("title", title));
        list.add(new Pair("message", message));
        return list.toArray(new Pair[list.size()]);
    }

    private BufferedReader getReader(String operationString, @Nullable String anfrage, String rootPasswort, URL urlConnection) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        URLConnection connection;
        if (this.LINK.getProtocol().equals("https")) {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new SecureRandom());
            URLConnection httpsConnection = urlConnection.openConnection();
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);
            httpsConnection.setConnectTimeout(5000);
            ((HttpsURLConnection)httpsConnection).setSSLSocketFactory(sc.getSocketFactory());
            httpsConnection.connect();
            connection = httpsConnection;
        } else {
            URLConnection httpConnection = urlConnection.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setConnectTimeout(5000);
            httpConnection.connect();
            connection = httpConnection;
        }
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
                + URLEncoder.encode(encodeRootPasswort(rootPasswort), "ISO-8859-1"));
        writer.flush();
        writer.close();
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

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
            return DBConnectionStatusEnum.LOGIN_FAILED;
        }
    }

    private String encodePasswort(String passwort) {
        try {
            StringBuffer hexString = new StringBuffer();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(passwort.getBytes());
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((hash[i] & 255) < 16) {
                    hexString.append("0" + Integer.toHexString(hash[i] & 255));
                } else {
                    hexString.append(Integer.toHexString(hash[i] & 255));
                }
            }
            return encodeRootPasswort(hexString.toString().toLowerCase()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private String encodeRootPasswort(String passwort) {
        if (passwort.equals("")) {
            return "";
        }
        try {
            StringBuffer hexString = new StringBuffer();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(passwort.getBytes());
            byte[] hash = md.digest();
            md = MessageDigest.getInstance("SHA-1");
            md.reset();
            md.update(hash);
            hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                if ((hash[i] & 255) < 16) {
                    hexString.append("0" + Integer.toHexString(hash[i] & 255));
                } else {
                    hexString.append(Integer.toHexString(hash[i] & 255));
                }
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
