package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.mainDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.net.HttpURLConnection;
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
    private SharedPreferences prefs;

    public DBAsyncTask(Activity currentActivity, DBAsyncResponse response, URL link, String rootPasswort) {
        this.prefs = currentActivity.getSharedPreferences(SHARED_PREFERENCE, 0);
        this.CONTEXT = currentActivity;
        this.LINK = link;
        this.BENUTZER = "";
        this.PASSWORT = "";
        this.ROOT_PASSWORT = rootPasswort;
        this.RESPONSE = response;
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
        this.DIALOG = new ProgressDialog(currentActivity);
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

    protected ArrayList<ContentValues> doInBackground(String... params) {
        ArrayList<ContentValues> resultArray = new ArrayList();
        StringBuilder builder;
        ContentValues result = new ContentValues();
        result.put("showToast", params[1]);
        BufferedReader reader;
        String line;
        String lastLine;
        if (params[0].equals(AsyncTaskOperationEnum.CHECK_CONNECTION.getText())) {
            publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));
            try {
                reader = getReader(params[0], this.ROOT_PASSWORT, this.LINK);
                line = null;
                while (true) {
                    lastLine = reader.readLine();
                    if (lastLine == null) {
                        break;
                    }
                    line = lastLine;
                }
                reader.close();
                if (line.trim().equals("1")) {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTED.getText());
                } else if (line.trim().equals("0")) {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.DATABASE_FAILED.getText());
                }
                resultArray.add(result);
            } catch (IOException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (KeyManagementException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (NoSuchAlgorithmException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            }
        } else if (params[0].equals(AsyncTaskOperationEnum.LOGIN.getText())) {
            publishProgress(createProgressList("Anmelden", "Sie werden angemeldet"));
            try {
                reader = getReader(AsyncTaskOperationEnum.CHECK_CONNECTION.getText(), this.ROOT_PASSWORT, this.LINK);
                line = null;
                while (true) {
                    lastLine = reader.readLine();
                    if (lastLine == null) {
                        break;
                    }
                    line = lastLine;
                }
                reader.close();
                if (line.trim().equals("1")) {
                    reader = getReader(params[0], this.ROOT_PASSWORT, this.LINK);
                    line = null;
                    while (true) {
                        lastLine = reader.readLine();
                        if (lastLine == null) {
                            break;
                        }
                        line = lastLine;
                    }
                    reader.close();
                    if (line.trim().equals("1")) {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_SUCCES.getText());
                    } else if (line.trim().equals("0")) {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                    }
                } else {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                }
                resultArray.add(result);
            } catch (IOException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (KeyManagementException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (NoSuchAlgorithmException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            }
        } else if (params[0].equals(AsyncTaskOperationEnum.GET_DATABASE_USER.getText())) {
            publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
            try {
                reader = getReader(AsyncTaskOperationEnum.CHECK_CONNECTION.getText(), this.ROOT_PASSWORT, this.LINK);
                line = null;
                while (true) {
                    lastLine = reader.readLine();
                    if (lastLine == null) {
                        break;
                    }
                    line = lastLine;
                }
                reader.close();
                if (line.trim().equals("1")) {
                    reader = getReader(AsyncTaskOperationEnum.GET_DATABASE_USER.getText(), this.ROOT_PASSWORT, this.LINK);
                    line = null;
                    while (true) {
                        lastLine = reader.readLine();
                        if (lastLine == null) {
                            break;
                        }
                        line = lastLine;
                    }
                    reader.close();
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), line != null ? DBConnectionStatusEnum.CONNECTED.getText() : DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                    result.put(DBConnectionStatusEnum.DATABASE_USER.getText(), line.toString().trim());
                    resultArray.add(result);
                } else {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                }
                resultArray.add(result);
            } catch (IOException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (KeyManagementException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (NoSuchAlgorithmException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.LOGIN_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            }
        } else if (params[0].equals(AsyncTaskOperationEnum.GET_DATA.getText())) {
            publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));
            try {
                reader = getReader(AsyncTaskOperationEnum.CHECK_CONNECTION.getText(), this.ROOT_PASSWORT, this.LINK);
                line = null;
                while (true) {
                    lastLine = reader.readLine();
                    if (lastLine == null) {
                        break;
                    }
                    line = lastLine;
                }
                reader.close();
                if (line.trim().equals("1")) {
                    publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
                    reader = getReader(params[2], this.ROOT_PASSWORT, this.LINK);
                    publishProgress(createProgressList("Verarbeiten", "Daten werden verarbeitet"));
                    builder = new StringBuilder();
                    line = reader.readLine();
                    if (line != null) {
                        builder.append(line);
                    }
                    reader.close();
                    if (builder.toString().equals("[]")) {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.BARCODE_FAILED.getText());
                    } else {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.SUCCES.getText());
                    }
                    resultArray.add(result);
                    Iterator it = jsonArrayToContentValues(builder.toString().trim()).iterator();
                    while (it.hasNext()) {
                        resultArray.add((ContentValues) it.next());
                    }
                } else {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                    resultArray.add(result);
                }
            } catch (IOException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (KeyManagementException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (NoSuchAlgorithmException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (JSONException e) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            }
        } else if (params[0].equals(AsyncTaskOperationEnum.INSERT_DATA.getText())) {
            publishProgress(createProgressList("Verbinden", "Verbindung wird geprüft"));
            try {
                reader = getReader(AsyncTaskOperationEnum.CHECK_CONNECTION.getText(), this.ROOT_PASSWORT, this.LINK);
                line = null;
                while (true) {
                    lastLine = reader.readLine();
                    if (lastLine == null) {
                        break;
                    }
                    line = lastLine;
                }
                reader.close();
                if (line.trim().equals("1")) {
                    publishProgress(createProgressList("Übertragen", "Daten werden übertragen"));
                    reader = getReader(params[2], this.ROOT_PASSWORT, this.LINK);
                    builder = new StringBuilder();
                    line = reader.readLine();
                    if (line != null) {
                        builder.append(line);
                    }
                    reader.close();
                    if (line.trim().equals("1")) {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.SUCCES.getText());
                    } else {
                        result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.INSERT_FAILED.getText());
                    }
                } else {
                    result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.CONNECTION_FAILED.getText());
                }
                resultArray.add(result);
            } catch (IOException e13) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (KeyManagementException e14) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            } catch (NoSuchAlgorithmException e15) {
                result.put(DBConnectionStatusEnum.CONNECTION_STATUS.getText(), DBConnectionStatusEnum.TRANSFER_FAILED.getText());
                resultArray.add(result);
                return resultArray;
            }
        }
        return resultArray;
    }

    protected void onPostExecute(ArrayList<ContentValues> resultArray) {
        super.onPostExecute(resultArray);
        Toast toast = null;
        this.DIALOG.hide();
        this.DIALOG.dismiss();
        ContentValues result = (ContentValues) resultArray.get(0);
        if (result.getAsString("showToast").equals("1")) {
            String asString = result.getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText());
            int i = -1;
            switch (asString.hashCode()) {
                case -1474478914:
                    if (asString.equals("connection_failed")) {
                        i = 2;
                        break;
                    }
                    break;
                case -891515280:
                    if (asString.equals("succes")) {
                        i = 3;
                        break;
                    }
                    break;
                case -749667197:
                    if (asString.equals("insert_failed")) {
                        i = 8;
                        break;
                    }
                    break;
                case -579210487:
                    if (asString.equals("connected")) {
                        i = 0;
                        break;
                    }
                    break;
                case -545183277:
                    if (asString.equals("login_failed")) {
                        i = 5;
                        break;
                    }
                    break;
                case -154721274:
                    if (asString.equals("login_succes")) {
                        i = 6;
                        break;
                    }
                    break;
                case -7726884:
                    if (asString.equals("barcode_failed")) {
                        i = 7;
                        break;
                    }
                    break;
                case 661188481:
                    if (asString.equals("database_failed")) {
                        i = 1;
                        break;
                    }
                    break;
                case 1215703825:
                    if (asString.equals("transfer_failed")) {
                        i = 4;
                        break;
                    }
                    break;
            }
            switch (i) {
                case 0:
                    toast = Toast.makeText(this.CONTEXT, "Verbindung wurde geprüft", Toast.LENGTH_SHORT);
                    break;
                case 1:
                    toast = Toast.makeText(this.CONTEXT, "Datenbank nicht erreicht", Toast.LENGTH_SHORT);
                    break;
                case 2:
                    toast = Toast.makeText(this.CONTEXT, "Verbindung Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
                case 3:
                    toast = Toast.makeText(this.CONTEXT, "Übertragung Erfolgreich", Toast.LENGTH_SHORT);
                    break;
                case 4:
                    toast = Toast.makeText(this.CONTEXT, "Übertragung Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
                case 5:
                    toast = Toast.makeText(this.CONTEXT, "Anmeldung Fehlgeschlagen", Toast.LENGTH_SHORT);
                    break;
                case 6:
                    toast = Toast.makeText(this.CONTEXT, "Anmeldung Erfolgreich", Toast.LENGTH_SHORT);
                    break;
                case 7:
                    toast = Toast.makeText(this.CONTEXT, "Barcode nicht hinterlegt", Toast.LENGTH_SHORT);
                    break;
                case 8:
                    toast = Toast.makeText(this.CONTEXT, "Einfügen der Daten Fehlgeschlagen", Toast.LENGTH_SHORT);
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
        for (int index = 0; index < values.length; index++) {
            if (values[index].first.equals("title")) {
                this.DIALOG.setTitle((String) values[index].second);
            } else if (values[index].first.equals("message")) {
                this.DIALOG.setMessage((String) values[index].second);
            }
        }
    }

    private static ArrayList<ContentValues> jsonArrayToContentValues(String jsonString) throws JSONException {
        ArrayList<ContentValues> outputArray = new ArrayList();
        if (jsonString == null) {
            throw new JSONException("");
        } else {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                ContentValues output = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                Iterator<String> iterator = object.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
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

    private BufferedReader getReader(String anfrage, String rootPasswort, URL urlConnection) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        URLConnection connection;
        if (this.LINK.getProtocol().equals("https")) {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new SecureRandom());
            URLConnection httpsConnection = (HttpsURLConnection) urlConnection.openConnection();
            httpsConnection.setDoOutput(true);
            httpsConnection.setDoInput(true);
            httpsConnection.setConnectTimeout(5000);
            //httpsConnection.setSSLSocketFactory(sc.getSocketFactory());
            httpsConnection.connect();
            connection = httpsConnection;
        } else {
            URLConnection httpConnection = (HttpURLConnection) urlConnection.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setConnectTimeout(5000);
            httpConnection.connect();
            connection = httpConnection;
        }
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(URLEncoder.encode("url", "ISO-8859-1") + "=" + anfrage + "&" + URLEncoder.encode("user", "ISO-8859-1") + "=" + URLEncoder.encode(this.BENUTZER, "ISO-8859-1") + "&" + URLEncoder.encode("pass", "ISO-8859-1") + "=" + URLEncoder.encode(this.PASSWORT, "ISO-8859-1") + "&" + URLEncoder.encode("rootPass", "ISO-8859-1") + "=" + URLEncoder.encode(encodeRootPasswort(rootPasswort), "ISO-8859-1"));
        writer.flush();
        writer.close();
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
