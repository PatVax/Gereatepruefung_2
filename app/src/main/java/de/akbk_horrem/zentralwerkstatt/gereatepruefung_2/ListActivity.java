package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter.ListAdapter;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefungDBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.ListHeaderFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.ListScrollViewFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Stack;

public class ListActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private EditText bemerkungenEditText;
    private Button checkAllButton, submitButton;
    private Menu menu;
    private ListHeaderFragment listHeaderFragment;
    private ListScrollViewFragment listScrollViewFragment;
    private boolean offlineMode, stopped = false;

    /**
     * Ein Stack für die Historie der aufgerufenen Prüfungen
     */
    private Stack<Pruefung> pruefungStack = new Stack<>();

    /**
     * Ein Stack für die Zustände der Listen für die jeweilige Prüfungen
     */
    Stack<Parcelable> listStates = new Stack<>();

    @Override
    public void onBackPressed() { //Eine Prüfung zurückkehren. Letzte Prüfung? Dann Normale Funktion des Backbuttons aufrufen
        if(pruefungStack.size() > 1) {
        pruefungStack.pop();
        updateActivity(listHeaderFragment.isShowing());
        listScrollViewFragment.setListState(listStates.pop());
    }else super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
        setContentView(R.layout.activity_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.pruefungStack.push((Pruefung) getIntent().getParcelableExtra("contents"));
        this.checkAllButton = findViewById(R.id.checkAllButton);
        this.submitButton = findViewById(R.id.submitButton);
        this.bemerkungenEditText = findViewById(R.id.bemerkungenEditText);
        this.bemerkungenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                pruefungStack.peek().setBemerkungen(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        this.offlineMode = getIntent().getBooleanExtra("offline", false);
        updateActivity(savedInstanceState != null ? savedInstanceState.getBoolean("dialogShowing") : true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (this.listStates.size() > 0) {
            this.listScrollViewFragment.setListAdapter(new ListAdapter(this, pruefungStack.peek(), !(pruefungStack.size() > 1)));
            this.listScrollViewFragment.setListState(this.listStates.pop());
        }
        if (!this.listHeaderFragment.isShowing()) {
            this.listHeaderFragment.hide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.stopped = true; //Wenn die Activity gestoppt wurde
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_menu, menu);
        if (this.listHeaderFragment.isShowing()) {
            menu.getItem(3).setVisible(true);
            menu.getItem(4).setVisible(false);
        } else if (!this.listHeaderFragment.isShowing()) {
            menu.getItem(3).setVisible(false);
            menu.getItem(4).setVisible(true);
        }
        menu.getItem(0).setVisible(pruefungStack.size() > 1 && offlineMode);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = getSharedPreferences(this.SHARED_PREFERENCES, MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.menu_hideHeader: //Header anzeigen oder verbergen
                if (this.listHeaderFragment.isShowing()) {
                    this.listHeaderFragment.hide();
                    item.setVisible(false);
                    this.menu.getItem(4).setVisible(true);
                }
                return true;
            case R.id.menu_showHeader: //Footer anzeigen oder verbergen
                if (!this.listHeaderFragment.isShowing()) {
                    this.listHeaderFragment.show();
                    item.setVisible(false);
                    this.menu.getItem(3).setVisible(true);
                }
                return true;
            case R.id.menu_previousList: //Eine alte Prüfung laden und zu dem Stack hinzufügen

                if (!offlineMode) {
                    String sql = "CALL getpruefung(" + (pruefungStack.size() - 1) + ", '" + pruefungStack.peek().getBarcode() + "')";
                    try {
                        DBAsyncTask.getInstance(this, new DBAsyncTask.DBAsyncResponse() {
                            @Override
                            public void processFinish(ArrayList<ContentValues> result) {
                                if ((result.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText()))) {
                                    result.remove(0);
                                    pruefungStack.push(new Pruefung(result));
                                    listStates.push(listScrollViewFragment.getListState());
                                    updateActivity(listHeaderFragment.isShowing());
                                }
                            }
                        }).execute(AsyncTaskOperationEnum.GET_DATA, prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true), sql);
                    } catch (MalformedURLException e) {
                        Toast.makeText(this, "URL nicht korrekt", Toast.LENGTH_SHORT);
                    }
                }else {
                        DBUtils.getPruefung(this, new PruefungDBAsyncTask.DBAsyncResponse() {
                            @Override
                            public void processFinish(@Nullable ArrayList<ContentValues> resultArrayList) {
                                if(resultArrayList != null){
                                    pruefungStack.push(new Pruefung(resultArrayList));
                                    listStates.push(listScrollViewFragment.getListState());
                                    updateActivity(listHeaderFragment.isShowing());
                                }else Toast.makeText(ListActivity.this, "Prüfung lokal nicht hinterlegt", Toast.LENGTH_SHORT);
                            }
                        }, pruefungStack.peek().getBarcode(), pruefungStack.size() - 1);
                    }
                return true;
            case R.id.menu_nextList: //Die oberste Prüfung entfernen wenn es nicht die letzte ist
                if (pruefungStack.size() > 1) {
                    pruefungStack.pop();
                    updateActivity(listHeaderFragment.isShowing());
                    listScrollViewFragment.setListState(listStates.pop());
                }
                return true;
            case R.id.menu_deleteList: //Die oberste Prüfung aus der temporären Datenbank löschen
                if (pruefungStack.size() > 1) {
                    DBUtils.deletePruefung(this, new PruefungDBAsyncTask.DBAsyncResponse() {
                        @Override
                        public void processFinish(@Nullable ArrayList<ContentValues> resultArrayList) {
                            if(resultArrayList != null){
                                ListActivity.this.pruefungStack.pop();
                                updateActivity(listHeaderFragment.isShowing());
                                listScrollViewFragment.setListState(listStates.pop());
                            }
                        }
                    }, pruefungStack.peek().getIDPruefung());
                }
                return true;
        }
        return false;
    }

    /**
     * Aktualisiert die Activity
     */
    private void updateActivity(boolean showDialog) {

        //Instanziert die Fragments neu mit der aktuell zubetrachtenden Prüfung und ersetzt sie im View
        this.listScrollViewFragment = ListScrollViewFragment.newInstance(this.pruefungStack.peek(), !(pruefungStack.size() > 1));
        this.listHeaderFragment = ListHeaderFragment.newInstance(this.pruefungStack.peek(), showDialog);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.listScrollViewFragment, this.listScrollViewFragment);
        if(showDialog)
            transaction.replace(R.id.listHeaderFragment, this.listHeaderFragment);
        else
            transaction.replace(R.id.listHeaderFragment, this.listHeaderFragment).hide(this.listHeaderFragment);
        transaction.commitNow();

        //Bemerkungen Textfeld setzen
        this.bemerkungenEditText.setText(pruefungStack.peek().getBemerkungen());

        //Wenn die Prüfung eine wiederhergestellte Prüfung ist
        if((pruefungStack.size() > 1)){
            //CheckaAllButton und SubmitButton unsichtbar machen
            //Bemerkungen Textfeld deaktivieren
           this.checkAllButton.setVisibility(View.GONE);
           this.submitButton.setVisibility(View.GONE);
           this.bemerkungenEditText.setEnabled(false);
        }else{ //Ansonsten
            //CheckaAllButton und SubmitButton sichtbar machen
            //Bemerkungen Textfeld aktivieren
            this.checkAllButton.setVisibility(View.VISIBLE);
            this.submitButton.setVisibility(View.VISIBLE);
            this.bemerkungenEditText.setEnabled(true);
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            this.listStates = (Stack<Parcelable>) savedInstanceState.getSerializable("list_states");
            this.pruefungStack = (Stack<Pruefung>) savedInstanceState.getSerializable("pruefungStack");
            if (savedInstanceState.getBoolean("bemerkungen_is_showing")) {
                this.bemerkungenEditText.setVisibility(View.VISIBLE);
            } else {
                this.bemerkungenEditText.setVisibility(View.GONE);
            }
            this.offlineMode = savedInstanceState.getBoolean("offline");
            this.stopped = savedInstanceState.getBoolean("stopped");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        listStates.push(this.listScrollViewFragment.getListState());
        outState.putSerializable("list_states", listStates);
        outState.putSerializable("pruefungStack", pruefungStack);
        outState.putBoolean("bemerkungen_is_showing", this.bemerkungenEditText.getVisibility() == View.VISIBLE);
        outState.putBoolean("offline", this.offlineMode);
        outState.putBoolean("stopped", this.stopped);
        outState.putBoolean("dialogShowing", listHeaderFragment.isShowing());
        super.onSaveInstanceState(outState);
    }

    //Callback-Methode für Button-Clicks
    public void onClick(View view) {
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        switch (view.getId()) {
            case R.id.checkAllButton: //Alle Checkboxes ankreuzen
                new Builder(this).setTitle("Alle Ankreuzen").setPositiveButton("Ja", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ListActivity.this.listScrollViewFragment.checkAll();
                    }
                }).setNegativeButton("Nein", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return;
            case R.id.bemerkungenButton: //Bemerkungen Textfeld anzeigen oder verbergen
                if (this.bemerkungenEditText.getVisibility() == View.GONE) {
                    this.bemerkungenEditText.setVisibility(View.VISIBLE);
                    return;
                } else if (this.bemerkungenEditText.getVisibility() == View.VISIBLE) {
                    this.bemerkungenEditText.setVisibility(View.GONE);
                    return;
                } else {
                    this.bemerkungenEditText.setVisibility(View.VISIBLE);
                    return;
                }
            case R.id.submitButton: //Ergebnisse hochladen
                if(stopped) { //Wurde die Activity schonmal gestoppt?
                    //Passwort Neueingabe anfordern
                    //Wenn eine gültige Eingabe erfolgt stopped = false setzen
                    final EditText input = new EditText(this);
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).setCancelable(false).setPositiveButton("Ergebnisse senden", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(prefs.getString(SharedPreferenceEnum.PASSWORT.getText(), "").equals(input.getText().toString())) {
                                ListActivity.this.stopped = false;
                                dialogInterface.dismiss();
                                ListActivity.this.insertPruefung(ListActivity.this.pruefungStack.peek(), ListActivity.this.offlineMode);
                                return;
                            }
                        }
                    }).setNegativeButton("Prüfung abbrechen", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            ListActivity.this.finish();
                            return;
                        }
                    }).setTitle("Ihr Passwort eingeben").create();
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); //Passwort als Eingabetyp
                    input.setHint("Passwort"); //Hinweis einstellen
                    input.setImeOptions(EditorInfo.IME_ACTION_GO); //Bestätigungs Action festlegen

                    input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (EditorInfo.IME_ACTION_GO == actionId) {
                                if(prefs.getString(SharedPreferenceEnum.PASSWORT.getText(), "").equals(input.getText().toString())){
                                    ListActivity.this.stopped = false;
                                    alertDialog.dismiss();
                                    ListActivity.this.insertPruefung(ListActivity.this.pruefungStack.peek(), ListActivity.this.offlineMode);
                                    return true;
                                }
                            }
                            return false;
                        }
                    });

                    alertDialog.setView(input);
                    alertDialog.show();
                }else{ //Ansonsten den insert ausführen
                insertPruefung(this.pruefungStack.peek(), this.offlineMode);
                }
            default:
                return;
        }
    }

    /**
     * Fügt die Prüfung abhängig von offline Modus entweder in die Haupt- oder Temporäredatenbank. Wenn Onlinde Modus fehlschlägt wird die Prüfung in die temporäre Datenbank eingefügt. Nach dem vorgang wird die Instanz der ListActivity beendet.
     * @param pruefung Prüfung-Objekt der hinzugefügt sein soll
     * @param offlineMode true wenn in offline Modus gearbeitet werden soll
     */
    private void insertPruefung(final Pruefung pruefung, boolean offlineMode){
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        //Je nach Offline Modus
        if (!offlineMode) {
            StringBuilder sqlBuilder = new StringBuilder();
            //Inserts aus den Daten bilden
            sqlBuilder.append("INSERT INTO pruefungen (geraete_barcode, idbenutzer, datum, bemerkungen) VALUES ('" +
                    pruefung.getBarcode() +
                    "', (SELECT idbenutzer FROM benutzer WHERE benutzername = '" +
                    prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), "") +
                    "'), CURDATE(), '" + pruefung.getBemerkungen() + "');");
            sqlBuilder.append("INSERT INTO pruefergebnisse VALUES ");
            for (int i = 0; i < pruefung.getKriterien().size() - 1; i++) {
                sqlBuilder.append("((SELECT p.idpruefung FROM pruefungen p WHERE p.geraete_barcode = '" +
                        pruefung.getBarcode() + "' ORDER BY p.idpruefung DESC LIMIT 1), " +
                        (pruefung.getKriterien().get(i)).getAsString("idkriterium") + ", '" +
                        (pruefung.getKriterien().get(i).getAsString("anzeigeart").equals("b") ?
                                (pruefung.getValues().get(i).getAsBoolean("messwert") ? "true" : "false") :
                                pruefung.getValues().get(i).getAsString("messwert")) + "'),");
            }
            sqlBuilder.append("((SELECT p.idpruefung FROM pruefungen p WHERE p.geraete_barcode = '" +
                    pruefung.getBarcode() + "' ORDER BY p.idpruefung DESC LIMIT 1), " +
                    (pruefung.getKriterien().get(pruefung.getKriterien().size() - 1)).
                            getAsString("idkriterium") + ", '" +
                    (pruefung.getKriterien().get(pruefung.getKriterien().size() - 1).
                            getAsString("anzeigeart").equals("b") ?
                            (pruefung.getValues().get(pruefung.getValues().size() - 1).
                                    getAsBoolean("messwert") ? "true" : "false") :
                            pruefung.getValues().get(pruefung.getValues().size() - 1).
                                    getAsString("messwert")) + "');");
            try {
                //Inserts ausführen
                DBAsyncTask.getInstance(this, new DBAsyncTask.DBAsyncResponse() {
                    @Override
                    public void processFinish(ArrayList<ContentValues> result) {
                        //Wenn erfolgreich beenden
                        if ((result.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText())) {
                            ListActivity.this.finish();
                        }else { //Sonst Offline-Verhalten inizieren
                            Toast.makeText(ListActivity.this, "Einfügen fehlgeschlagen. In die temporäre Datenbank schreiben.", Toast.LENGTH_SHORT);
                            insertPruefung(pruefung, true);
                        }
                    }
                }).execute(AsyncTaskOperationEnum.INSERT_DATA, true, sqlBuilder.toString().trim());
            } catch (MalformedURLException e) {
                Toast.makeText(this, "URL nicht korrekt", Toast.LENGTH_SHORT);
                return;
            }
        } else { //Die Prüfung in die temporäre Datenbank einfügen
            DBUtils.insertPruefung(this, new PruefungDBAsyncTask.DBAsyncResponse() {
                @Override
                public void processFinish(@Nullable ArrayList<ContentValues> resultArrayList) {
                    if (resultArrayList == null) { //Wenn nicht erfolgreich den Benutzer informieren
                        Toast.makeText(ListActivity.this, "Prüfung könnte nicht hinzugefügt werden", Toast.LENGTH_SHORT);
                    } else ListActivity.this.finish(); //Sonst beenden
                }
            }, pruefung, prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), ""), prefs.getString(SharedPreferenceEnum.PASSWORT.getText(), ""));
        }
    }
}