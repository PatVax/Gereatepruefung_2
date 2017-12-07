package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefungDBAsyncTask;

/**
 * Created by Patryk on 26.11.2017.
 * <p>
 * Die Klasse liefert nützliche Funktionen zur Datenbankanbindung in der App
 */
public class DBUtils {

    /**
     * Die Funktion erzeugt einen Hash der mit dem beim Einloggen im uebergabe.php benutzten Algorithm übereinstimmt
     *
     * @param passwort Das zu verschlüsselnde Passwort
     * @return Die Funktion liefert einen String zurück der den Eingabeparameter in Form eines Hashes darstellt
     */
    public static String encodePasswort(String passwort) {
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

    /**
     * Die Funktion erzeugt einen Hash der mit dem beim Datenbankzugriff über uebergabe.php benutzten Algorithm übereinstimmt
     *
     * @param passwort Das zu verschlüsselnde Passwort
     * @return Die Funktion liefert einen String zurück der den Eingabeparameter in Form eines Hashes darstellt
     */
    public static String encodeRootPasswort(String passwort) {
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

    /**
     * Die Funktion konvertiert einen jsonString zu einer ArrayList von ContentValues
     *
     * @param jsonString Ein JSON-String den es zu konvertieren gilt
     * @return Die Funktiont liefert eine ArrayList von ContentValues zurück die die übergebenen Daten darstellt
     * @throws JSONException Die Funktion löst eine Ausnahme aus wenn der übergebene Parameter kein JSON-String-Format hat
     */
    public static ArrayList<ContentValues> jsonArrayToContentValues(String jsonString) throws JSONException {
        ArrayList<ContentValues> outputArray = new ArrayList();
        if (jsonString == null || jsonString.equals("")) {
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

    /**
     * Die Funktion holt alle Informationen aus der temporären Datenbank die zur erzeugung von dem Pruefung Objekt benötigt werden. Das Ergenis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert.
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param barcode Der Barcode des gesuchten Geräts
     */
    public static void getPruefliste(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse, String barcode) {
        PruefungDBAsyncTask.getPruefliste(context, dbAsyncResponse, barcode);
    }

    /**
     * Die Funktion holt alle Informationen aus der temporären Datenbank die zur erzeugung von dem Pruefung Objekt benötigt werden. Darin sind Informationen enthalten die eine fertige Liste in der {@link de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.ListActivity} angezeigt werden können(fertige Prüfung die nicht bearbeitet werden soll) Das Ergenis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert.
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param barcode Der Barcode des gesuchten Geräts
     * @param offset 0-basierter Index wievielte Prüfung für das jeweilige Gerät abgerufen werden soll
     */
    public static void getPruefung(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse, String barcode, int offset) {
        PruefungDBAsyncTask.getPruefung(context, dbAsyncResponse, barcode, offset);
    }

    /**
     * Fügt eine Prüfung in die temporäre Datenbank ein. Das Ergenis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert(Ein Objekt wenn erfolgreich oder null falls nicht).
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param pruefung Die Prüfung die eingefügt werden soll
     * @param benutzer Der Benutzer der die Prüfung durchgeführt hat
     * @param password Das Passwort des Benutzers
     */
    public static void insertPruefung(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse, @NonNull Pruefung pruefung, @NonNull String benutzer, @NonNull String password) {
        PruefungDBAsyncTask.insertPruefung(context, dbAsyncResponse, pruefung, benutzer, password);
    }

    /**
     * Prüft ob die Anmeldedaten mit der temporären Datenbank übereinstimmen. Das Ergenis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert(Ein Objekt wenn erfolgreich oder null falls nicht).
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param benutzer Der Banutzer der eingeloggt werden soll
     * @param password Das Passwort für den Benutzer
     */
    public static void login(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse, @NonNull String benutzer, @NonNull String password) {
        PruefungDBAsyncTask.login(context, dbAsyncResponse, benutzer, password);
    }

    /**
     * Löscht eine Prüfung aus der temporären Datenbank. Das Ergebnis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert(Ein Objekt wenn erfolgreich oder null falls nicht).
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param idPruefung Die ID der zu löschenden Prüfung
     */
    public static void deletePruefung(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse, long idPruefung){
        PruefungDBAsyncTask.deletePruefung(context, dbAsyncResponse, idPruefung);
    }

}
