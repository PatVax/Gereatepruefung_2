package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.MainDialogFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.MainFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.VerbindungEinstellungenFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.SyncAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.DBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.GeraeteDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.GeraetetypDBHelper;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.KriterienDBHelper;

public class MainActivity extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener, MainDialogFragment.OnFragmentInteractionListener, VerbindungEinstellungenFragment.OnFragmentInteractionListener{
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private boolean isStartingActivity = false;
    private Button loginButton;
    private MainDialogFragment mainDialogFragment;
    private Button pruefenButton;
    private Button scannerButton;
    private Button syncButton;
    private Button verbindungButton;
    private VerbindungEinstellungenFragment verbindungEinstellungenFragment;

    public void onBackPressed() {
        if (this.mainDialogFragment.isShowing()) {
            this.mainDialogFragment.hide();
        } else if (this.verbindungEinstellungenFragment.isShowing()) {
            this.verbindungEinstellungenFragment.hide();
        } else {
            super.onBackPressed();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mainDialogFragment = (MainDialogFragment) getSupportFragmentManager().findFragmentById(R.id.loginFragment);
        this.verbindungEinstellungenFragment = (VerbindungEinstellungenFragment) getSupportFragmentManager().findFragmentById(R.id.verbindungFragment);
        this.loginButton = findViewById(R.id.loginButton);
        this.pruefenButton = findViewById(R.id.pruefenButton);
        this.verbindungButton = findViewById(R.id.verbindungButton);
        this.scannerButton = findViewById(R.id.einstellungenButton);
        this.syncButton = findViewById(R.id.syncButton);
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        if(getIntent().getBooleanExtra("connection", false));
        if (savedInstanceState != null) {
            this.loginButton.setText(prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), "EINLOGGEN"));
        } else if (prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {
            this.mainDialogFragment.show();
            this.verbindungEinstellungenFragment.hide();
            this.loginButton.setEnabled(true);
        } else {
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

    protected void onStart() {
        super.onStart();
        this.isStartingActivity = false;
        if (this.mainDialogFragment.isShowing()) {
            this.mainDialogFragment.show();
        } else if (this.verbindungEinstellungenFragment.isShowing()) {
            this.verbindungEinstellungenFragment.show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        if (item.getItemId() == R.id.menu_manuelleEingabe && prefs.getString(SharedPreferenceEnum.PASSWORT.getText(), null) != null) {
            final EditText input = new EditText(this);
            input.setInputType(2);
            input.setHint("Barcode");
            input.setImeOptions(2);
            input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (EditorInfo.IME_ACTION_GO != actionId) {
                        return false;
                    }
                    MainActivity.this.fill_list(input.getText().toString());
                    return true;
                }
            });
            new AlertDialog.Builder(this).setView(input).setTitle("Manuelle Eingabe").setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.fill_list(input.getText().toString());
                }
            }).setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        switch (view.getId()) {
            case R.id.loginButton:
                this.mainDialogFragment.show();
                if (this.verbindungEinstellungenFragment.isShowing()) {
                    this.verbindungEinstellungenFragment.hide();
                    return;
                }
                return;
            case R.id.pruefenButton:
                this.isStartingActivity = true;
                startActivityForResult(new Intent(Intents.Scan.ACTION), 0);
                return;
            case R.id.verbindungButton:
                this.verbindungEinstellungenFragment.show();
                if (this.mainDialogFragment.isShowing()) {
                    this.mainDialogFragment.hide();
                    return;
                }
                return;
            case R.id.einstellungenButton:
                this.isStartingActivity = true;
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setClassName(this, PreferencesActivity.class.getName());
                startActivity(intent);
                return;
            case R.id.syncButton:
                if (prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {
                    Toast.makeText(this, "Im Offline-Modus nicht möglich", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    new SyncAsyncTask(this).execute(new Void[0]);
                    return;
                }
            case R.id.benutzerButton:
                this.mainDialogFragment.changeUser();
                return;
            case R.id.passwortButton:
                this.mainDialogFragment.login();
                return;
            case R.id.acceptVerbindungButton:
                this.verbindungEinstellungenFragment.acceptConnection();
                return;
            case R.id.cancelVerbindungButton:
                this.verbindungEinstellungenFragment.cancelConnection();
                return;
            default:
                return;
        }
    }

    public void onFragmentInteraction(Uri uri) {
    }

    public void onConnectionSucces() {
        this.loginButton.setEnabled(true);
        this.pruefenButton.setEnabled(false);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(false);
        this.syncButton.setEnabled(false);
        this.verbindungEinstellungenFragment.hide();
        this.mainDialogFragment.show();
    }

    public void onLoginSucces() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        this.loginButton.setEnabled(true);
        this.pruefenButton.setEnabled(true);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(true);
        this.syncButton.setEnabled(true);
        this.loginButton.setText(prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), ""));
        this.mainDialogFragment.hide();
    }

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

    public void onLogout() {
        this.loginButton.setEnabled(true);
        this.pruefenButton.setEnabled(false);
        this.verbindungButton.setEnabled(true);
        this.scannerButton.setEnabled(false);
        this.syncButton.setEnabled(false);
        this.loginButton.setText("EINLOGGEN");
    }

    private void fill_list(String barcode) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(SharedPreferenceEnum.BARCODE.getText(), barcode);
        prefsEdit.apply();
        if (prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {
            try {
                ArrayList<ContentValues> result = new ArrayList();
                result.add(((GeraeteDBHelper) new GeraeteDBHelper(this)).getRowByBarcode(barcode));
                ArrayList<ContentValues> oldResult = result;
                DBHelper dbHelper = new GeraetetypDBHelper(this);
                result = new ArrayList();
                result.add(((GeraetetypDBHelper) dbHelper).getGeraetetypByID(((ContentValues) oldResult.get(0)).getAsInteger("IDGeraetetyp").intValue()));
                ((ContentValues) result.get(0)).putAll((ContentValues) oldResult.get(0));
                oldResult = result;
                result = ((KriterienDBHelper) new KriterienDBHelper(this)).getKriterienByGeraetetyp(((ContentValues) oldResult.get(0)).getAsInteger("IDGeraetetyp").intValue());
                for(ContentValues contentValues : result) contentValues.putAll((ContentValues) oldResult.get(0));
                if (result.size() > 0) {
                    this.isStartingActivity = true;
                    Intent intent = new Intent(this, ListActivity.class);
                    intent.putParcelableArrayListExtra("contents", result);
                    startActivity(intent);
                    return;
                }
                return;
            } catch (NullPointerException e) {
                Toast.makeText(this, "Eintrag in der lokalen Datenbank nicht gefunden", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            try {
                String str;
                DBAsyncTask.getInstance(this, new DBAsyncResponse() {
                    public void processFinish(ArrayList<ContentValues> result) {
                        if ((result.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText()))) {
                            result.remove(0);
                            if (result.size() > 0) {
                                isStartingActivity = true;
                                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                                intent.putExtra("contents", new Pruefung(result));
                                MainActivity.this.startActivity(intent);
                            }
                        }
                    }
                }).execute(AsyncTaskOperationEnum.GET_DATA,
                        prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true),
                        "SELECT gt.bezeichnung AS geraetename, gt.headertext, gt.footertext, h.bezeichnung AS herstellername, p.idkriterium, p.text, p.anzeigeart, g.geraete_barcode " +
                                "FROM geraete g LEFT JOIN geraetetypen gt ON(gt.idgeraetetyp = g.idgeraetetyp) " +
                                "LEFT JOIN hersteller h ON (h.idhersteller = gt.idhersteller) " +
                                "RIGHT JOIN pruefkriterien p ON (p.idgeraetetyp = gt.idgeraetetyp) " +
                                "WHERE g.geraete_barcode = '" + barcode + "' ORDER BY p.idkriterium ASC");
            } catch (MalformedURLException e) {
                Toast.makeText(this, "URL nicht korrekt", Toast.LENGTH_SHORT);
            }
        }
    }

    public boolean isStartingActivity() {
        return this.isStartingActivity;
    }

    public void setStartingActivity(boolean startingActivity) {
        this.isStartingActivity = startingActivity;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == -1) {
            fill_list(data.getStringExtra(Intents.Scan.RESULT));
        }
    }
}
