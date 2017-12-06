package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.PreferencesActivity;

import java.net.MalformedURLException;
import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefungDBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.MainDialogFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.MainFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.VerbindungEinstellungenFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.SyncAsyncTask;

public class MainActivity extends AppCompatActivity implements MainDialogFragment.OnFragmentInteractionListener, VerbindungEinstellungenFragment.OnFragmentInteractionListener{
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();

    //Wird eine neue Activity gestartet?
    private boolean isStartingActivity = false;
    private Button loginButton, pruefenButton, scannerButton, syncButton, verbindungButton;
    private MainDialogFragment mainDialogFragment;
    private VerbindungEinstellungenFragment verbindungEinstellungenFragment;
    private MainFragment mainFragment;

    @Override
    public void onBackPressed() {
        //Wenn ein DialogFragment angezeigt wird, verstecken
        if (this.mainDialogFragment.isShowing()) {
            this.mainDialogFragment.hide();
        } else if (this.verbindungEinstellungenFragment.isShowing()) {
            this.verbindungEinstellungenFragment.hide();
        } else { //Ansonsten übliches Backbutton verhalten
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main); //View Layout vorbereiten

        //Zuweisung der Fragments und Buttons
        this.mainDialogFragment = (MainDialogFragment) getSupportFragmentManager().findFragmentById(R.id.loginFragment);
        this.verbindungEinstellungenFragment = (VerbindungEinstellungenFragment) getSupportFragmentManager().findFragmentById(R.id.verbindungFragment);
        this.mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.mainFragment);
        this.loginButton = findViewById(R.id.loginButton);
        this.pruefenButton = findViewById(R.id.pruefenButton);
        this.verbindungButton = findViewById(R.id.verbindungButton);
        this.scannerButton = findViewById(R.id.einstellungenButton);
        this.syncButton = findViewById(R.id.syncButton);

        //SharedPreferences
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        if (savedInstanceState != null) { //Falls es einen gespeicherten Zustand gibt
            this.loginButton.setText(prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), "EINLOGGEN"));

        //Falls Offline Modus an ist
        } else if (prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {
            this.mainDialogFragment.show();
            this.verbindungEinstellungenFragment.hide();
            this.loginButton.setEnabled(true);

        } else { //Ansonsten je nach Testverbindungsaufbau fortfahren
            if (getIntent().getBooleanExtra("connection", false)) {
                MainActivity.this.mainDialogFragment.show();
                MainActivity.this.verbindungEinstellungenFragment.hide();
                MainActivity.this.loginButton.setEnabled(true);
            } else {
                MainActivity.this.mainDialogFragment.hide();
                MainActivity.this.verbindungEinstellungenFragment.show();
                MainActivity.this.loginButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.isStartingActivity = false; //isStartingActivity zurücksetzen

        //Entsprechendes Dialog anzeigen falls beim Schließen der Activity eins zu sehen war
        if (this.mainDialogFragment.isShowing()) {
            this.mainDialogFragment.show();
        } else if (this.verbindungEinstellungenFragment.isShowing()) {
            this.verbindungEinstellungenFragment.show();
        }
    }

    //Menu erstellen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    //Wenn ein MenuItem gedrückt wurde
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //SharedPreferences
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        //Wenn Manuelle Eingabe gedrückt wurde und ein Benutzer eingelogt ist
        if (item.getItemId() == R.id.menu_manuelleEingabe && prefs.getString(SharedPreferenceEnum.PASSWORT.getText(), null) != null) {

            //Einen AlertDialog mit dem Eingabefeld erzeugen und anzeigen
            final EditText input = new EditText(this); //Ein Eingabefeld deklarieren
            final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("Manuelle Eingabe").setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    MainActivity.this.fill_list(input.getText().toString(),
                            prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false),
                            prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
                }
            }).setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
            input.setInputType(InputType.TYPE_CLASS_NUMBER); //Nummernblock als Eingabetyp
            input.setHint("Barcode"); //Hinweis einstellen
            input.setImeOptions(EditorInfo.IME_ACTION_GO); //Bestätigungs Action festlegen

            //Falls Eingabe bestätigt wurde ListActivity öffnen
            input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (EditorInfo.IME_ACTION_GO == actionId) {
                        alertDialog.dismiss();
                        MainActivity.this.fill_list(input.getText().toString(),
                                prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false),
                                prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
                        return true;
                    }
                    return false;
                }
            });

            alertDialog.setView(input);
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {

        //SharedPreferences
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        //Bestimmte Aktion ausführen für den jeweiligen Button
        switch (view.getId()) {
            case R.id.loginButton: //MainDialogFragment anzeigen
                this.mainDialogFragment.show();
                if (this.verbindungEinstellungenFragment.isShowing()) {
                    this.verbindungEinstellungenFragment.hide();
                    return;
                }
                return;
            case R.id.pruefenButton: //Scanvorgang starten
                this.isStartingActivity = true; //Da neue Activity gestartet wird true setzen
                startActivityForResult(new Intent(Intents.Scan.ACTION), 0);
                return;
            case R.id.verbindungButton: //VerbindungsEinstellungenFragment anzeigen
                this.verbindungEinstellungenFragment.show();
                if (this.mainDialogFragment.isShowing()) {
                    this.mainDialogFragment.hide();
                    return;
                }
                return;
            case R.id.einstellungenButton: //Scannereinstellungen öffnen
                this.isStartingActivity = true; //Da neue Activity gestartet wird true setzen
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setClassName(this, PreferencesActivity.class.getName());
                startActivity(intent);
                return;
            case R.id.syncButton: //Die tempDB mit der mainDB synchronisieren
                if (prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) { //Wenn Offline Modus an Text anzeigen
                    Toast.makeText(this, "Im Offline-Modus nicht möglich", Toast.LENGTH_SHORT).show();
                    return;
                } else { //Ansonsten Synchronisierung starten
                    new SyncAsyncTask(this).execute(new Void[0]);
                    return;
                }
            case R.id.benutzerButton: //Wenn Benutzer Wechseln Taste gedrückt wurde
                this.mainDialogFragment.changeUser();
                return;
            case R.id.passwortButton: //Wenn Einloggen Taste gedrückt wurde
                this.mainDialogFragment.login();
                return;
            case R.id.acceptVerbindungButton: //Verbindungsdatenbestätigung
                this.verbindungEinstellungenFragment.acceptConnection();
                return;
            case R.id.cancelVerbindungButton: //Verbindungsdaten zurücksetzen
                this.verbindungEinstellungenFragment.cancelConnection();
                return;
            default:
                return;
        }
    }

    @Override
    public void onConnectionSucces() {
        this.loginButton.setEnabled(true);
        this.pruefenButton.setEnabled(false);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(false);
        this.syncButton.setEnabled(false);
        this.verbindungEinstellungenFragment.hide();
        this.mainDialogFragment.invalidatePassword();
        this.mainDialogFragment.show();
    }

    @Override
    public void onLoginSucces(String benutzer) {
        this.loginButton.setEnabled(true);
        this.pruefenButton.setEnabled(true);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(true);
        this.syncButton.setEnabled(true);
        this.loginButton.setText(benutzer);
        this.mainDialogFragment.hide();
    }

    @Override
    public void onConnectionFailed() {
        this.loginButton.setEnabled(false);
        this.pruefenButton.setEnabled(false);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(false);
        this.syncButton.setEnabled(false);
        this.mainDialogFragment.logout();
        this.mainDialogFragment.hide();
        this.verbindungEinstellungenFragment.show();
    }

    @Override
    public void onLogout() {
        this.loginButton.setEnabled(true);
        this.pruefenButton.setEnabled(false);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(false);
        this.syncButton.setEnabled(false);
        this.loginButton.setText("EINLOGGEN");
    }

    /**
     * Die Funktion erzeugt ein Pruefungobjekt. Die Daten werden je nach offlineMode von der Lokalen- oder Hauptdatenbank gezogen.
     * <p>
     * Das Pruefungobjekt wird anschließend an die ListActivity übergeben. Die ListActivity wird gestartet.
     * <p>
     * isStartingActivity wird true gesetzt
     * @param barcode Barcode des Geräts für das die Prüfung erstellt werden soll
     * @param offlineMode Wenn true werden die Prüfungsdaten aus der Lokalendatenbank gezogen
     * @param showMessage Gibt an ob ein Toast nach dem Vorgang angezeigt werden soll
     */
    private void fill_list(String barcode, boolean offlineMode, boolean showMessage) {
        if (offlineMode) {
            try {

                //Prüfung aus der Lokalendatenbank ziehen
                DBUtils.getPruefliste(this, new PruefungDBAsyncTask.DBAsyncResponse() {
                    @Override
                    public void processFinish(ArrayList<ContentValues> resultArrayList) {
                        if(resultArrayList != null) {
                            Pruefung pruefung = new Pruefung(resultArrayList);

                            isStartingActivity = true;

                            //ListActivity wird gestartet
                            Intent intent = new Intent(MainActivity.this, ListActivity.class);
                            intent.putExtra("contents", pruefung);
                            startActivity(intent);
                        } else{
                            Toast.makeText(MainActivity.this, "Daten nicht hinterlegt", Toast.LENGTH_SHORT);
                        }
                    }
                }, barcode);
                return;
            } catch (NullPointerException|IllegalArgumentException|IndexOutOfBoundsException e) {
                Toast.makeText(this, "Eintrag in der lokalen Datenbank nicht gefunden", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            try {

                //Prüfung aus der Hauptdatenbank ziehen
                DBAsyncTask.getInstance(this, new DBAsyncTask.DBAsyncResponse() {
                    public void processFinish(ArrayList<ContentValues> result) {

                        //Wenn die Anfrage erfolgreich war
                        if ((result.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText()))) {
                            result.remove(0);
                            if (result.size() > 0) {
                                isStartingActivity = true;

                                //ListActivity wird gestartet
                                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                                intent.putExtra("contents", new Pruefung(result));
                                MainActivity.this.startActivity(intent);
                            }
                        }
                    }
                }).execute(AsyncTaskOperationEnum.GET_DATA, showMessage,
                        "CALL getpruefliste(" + barcode + ")");
            } catch (MalformedURLException e) {
                Toast.makeText(this, "URL nicht korrekt", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Die Funktion gibt an ob MainActivity startet eine andere Activity
     * @return Die Funktion liefert true zurück wenn eine neue Activity gestartet wird
     */
    public boolean isStartingActivity() {
        return this.isStartingActivity;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //SharedPreferences
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        if (requestCode == 0 && resultCode == -1) {
            //fill_list wird aufgerufen mit dem gescannten Barcode
            this.fill_list(data.getStringExtra(Intents.Scan.RESULT),
                    prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false),
                    prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
        }
    }
}
