package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;

/**
 * Created by Patryk on 04.12.2017.
 * Eine Klasse für den asynchronen Zugriff auf die temporäre Datenbank
 */
public class PruefungDBAsyncTask extends AsyncTask<String, Void, ArrayList<ContentValues>> {

    @Nullable private final Pruefung pruefung;
    private final Activity context;
    private final ProgressDialog DIALOG;
    private final PruefungDBAsyncTask.DBAsyncResponse mListener;

    public interface DBAsyncResponse {

        /**
         * Wird aufgerufen wenn DBAsyncTask beendet wurde
         * @param resultArrayList Das Ergebnis aus dem Task. Falls null hatte das Task kein Erfolg.
         */
        void processFinish(@Nullable ArrayList<ContentValues> resultArrayList);
    }

    /**
     * Erzeugt eine Instanz der Klasse
     * @param context Der Context der den Task ausführt
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     */
    private PruefungDBAsyncTask(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse){
        this.context = context;
        this.DIALOG = new ProgressDialog(context);
        this.mListener = dbAsyncResponse;
        this.pruefung = null;
    }

    /**
     * Erzeugt eine Instanz der Klasse
     * @param context Der Context der den Task ausführt
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param pruefung Eine Prüfung mit der in dem Task gearbeitet werden soll
     */
    private PruefungDBAsyncTask(Activity context, PruefungDBAsyncTask.DBAsyncResponse dbAsyncResponse, Pruefung pruefung){
        this.context = context;
        this.DIALOG = new ProgressDialog(context);
        this.mListener = dbAsyncResponse;
        this.pruefung = pruefung;
    }

    @Override
    protected ArrayList<ContentValues> doInBackground(String... args) {
        ArrayList<ContentValues> result = new ArrayList<>();

        //Aktion die ausgeführt werden soll
        switch (args[0]){
            case "login": //Einloggen
                if(new BenutzerDBHelper(this.context).getPasswordByBenutzername(args[1]).equals(DBUtils.encodePasswort(args[2]))) {
                    return new ArrayList<>();
                }else return null;
            case "getPruefung": //Eine bestehende Prüfung anfordern
                int offset = Integer.parseInt(args[2]);
                ContentValues pruefung;
                try {
                    pruefung = new PruefungDBHelper(this.context).getRowsByBarcode(args[1]).get(offset);
                    result.addAll(new PruefergebnisseDBHelper(this.context).getRowsByIDPruefung(pruefung.getAsLong("idpruefung")));
                    result.get(0).putAll(pruefung);
                }catch(IndexOutOfBoundsException e){
                    cancel(true);
                    while(!isCancelled());
                    return null;
                }
            case "getPruefliste": //Die Prüfliste erstellen
                ContentValues geraet = new ContentValues();
                ArrayList<ContentValues> kriterien;
                try {
                    geraet.putAll(new GeraeteDBHelper(this.context).getRowByBarcode(args[1]));
                    geraet.putAll(new GeraetetypDBHelper(this.context).getRow(geraet.getAsLong("idgeraetetyp")));

                    kriterien = new KriterienDBHelper(this.context).getKriterienRowsByGeraetetyp(geraet.getAsLong("idgeraetetyp"));
                }catch(NullPointerException e){
                    cancel(true);
                    while(!isCancelled());
                    return null;
                }

                //Wenn result bereits gefüllt ist, die Informationen zu den Kriterien dazu hinzufügen
                if(result.size() > 0) {
                    if(result.size() == kriterien.size()) {
                        for (int i = 0; i < kriterien.size() && i < result.size(); i++)
                            result.get(i).putAll(kriterien.get(i));
                    } else {
                        cancel(true);
                        while(!isCancelled());
                        return null;
                    }
                } else result.addAll(kriterien); //Ansonsten result mit Kriterieninformationen füllen
                result.get(0).putAll(geraet);
                return result;
            case "insertPruefung": //Prüfung in die Datenbank hinzufügen
                long id = new PruefungDBHelper(this.context).insertRow(args[1], this.pruefung.getBarcode(), this.pruefung.getDatum(), this.pruefung.getBemerkungen(), DBUtils.encodePasswort(args[2]));
                if (id == -1){
                    cancel(true);
                    while(!isCancelled());
                    return null;
                }
                //Dazu gehörige Prüfergebnisse werden hinzugefügt
                ArrayList<ContentValues> insertList = this.pruefung.getValues();
                for (int i = 0; i < insertList.size(); i++){
                    insertList.get(i).put("idpruefung", id);
                }
                //Beim Fehler wird die Erfolgreich hinzugefügte Prüfung gelöscht
                if (!new PruefergebnisseDBHelper(this.context).insertAll(insertList)){
                    new PruefungDBHelper(this.context).deleteFromTable(id);
                    cancel(true);
                    while(!isCancelled());
                    return null;
                }
                return new ArrayList<>();
            case "deletePruefung": //Prüfung aus der Datenbank löschen
                if(new PruefungDBHelper(this.context).deleteFromTable(Long.parseLong(args[1])) == 1)
                {
                    if(!(new PruefergebnisseDBHelper(this.context).deleteRowsByIDPruefung(Long.parseLong(args[1])) > 0)){
                        cancel(true);
                        while(!isCancelled());
                        return null;
                    }
                }else {
                    cancel(true);
                    while(!isCancelled());
                    return null;
                }
                return new ArrayList<>();
            default: return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.DIALOG.setMessage("Verarbeiten...");
        this.DIALOG.setCancelable(false);
        this.DIALOG.setIndeterminate(true);
        this.DIALOG.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.DIALOG.show();
    }

    @Override
    protected void onPostExecute(ArrayList<ContentValues> result) {
        super.onPostExecute(result);
        this.DIALOG.hide();
        this.DIALOG.dismiss();
        mListener.processFinish(result);
    }

    @Override
    protected void onCancelled(ArrayList<ContentValues> result) {
        super.onCancelled(result);
        this.DIALOG.hide();
        this.DIALOG.dismiss();
        mListener.processFinish(null);
    }

    /**
     * Die Funktion holt alle Informationen aus der temporären Datenbank die zur erzeugung von dem Pruefung Objekt benötigt werden. Das Ergebnis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert.
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param barcode Der Barcode des gesuchten Geräts
     */
    public static void getPruefliste(Activity context, DBAsyncResponse dbAsyncResponse, String barcode){
        new PruefungDBAsyncTask(context, dbAsyncResponse).execute("getPruefliste", barcode);
    }

    /**
     * Die Funktion holt alle Informationen aus der temporären Datenbank die zur erzeugung von dem Pruefung Objekt benötigt werden. Darin sind Informationen enthalten die eine fertige Liste in der {@link de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.ListActivity} angezeigt werden können(fertige Prüfung die nicht bearbeitet werden soll) Das Ergebnis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert.
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param barcode Der Barcode des gesuchten Geräts
     * @param offset 0-basierter Index wievielte Prüfung für das jeweilige Gerät abgerufen werden soll
     */
    public static void getPruefung(Activity context, DBAsyncResponse dbAsyncResponse, String barcode, int offset){
        new PruefungDBAsyncTask(context, dbAsyncResponse).execute("getPruefung", barcode, offset + "");
    }

    /**
     * Fügt eine Prüfung in die temporäre Datenbank ein. Das Ergebnis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert(Ein Objekt wenn erfolgreich oder null falls nicht).
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param pruefung Die Prüfung die eingefügt werden soll
     * @param benutzer Der Benutzer der die Prüfung durchgeführt hat
     * @param password Das Passwort des Benutzers
     */
    public static void insertPruefung(Activity context, DBAsyncResponse dbAsyncResponse, @NonNull Pruefung pruefung, @NonNull String benutzer, @NonNull String password){
        new PruefungDBAsyncTask(context, dbAsyncResponse, pruefung).execute("insertPruefung", benutzer, password);
    }

    /**
     * Prüft ob die Anmeldedaten mit der temporären Datenbank übereinstimmen. Das Ergebnis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert(Ein Objekt wenn erfolgreich oder null falls nicht).
     *
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param benutzer Der Banutzer der eingeloggt werden soll
     * @param password Das Passwort für den Benutzer
     */
    public static void login(Activity context, DBAsyncResponse dbAsyncResponse, @NonNull String benutzer, @NonNull String password){
        new PruefungDBAsyncTask(context, dbAsyncResponse).execute("login", benutzer, password);
    }

    /**
     * Löscht eine Prüfung aus der temporären Datenbank. Das Ergebnis wird mit {@link PruefungDBAsyncTask.DBAsyncResponse#processFinish(ArrayList<ContentValues>)} Methode zurückgeliefert(Ein Objekt wenn erfolgreich oder null falls nicht).
     * @param context Aktuelles Context der App
     * @param dbAsyncResponse Eine Instanz des {@link PruefungDBAsyncTask.DBAsyncResponse} Interface. Wird genutzt um das Ergebnis zurück zu liefern.
     * @param idPruefung Die ID der zu löschenden Prüfung
     */
    public static void deletePruefung(Activity context, DBAsyncResponse dbAsyncResponse, long idPruefung){
        new PruefungDBAsyncTask(context, dbAsyncResponse).execute("deletePruefung", idPruefung + "");
    }
}
