package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2;

import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter.ListAdapter;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.ListHeaderFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.ListScrollViewFragment;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments.ListScrollViewFragment.OnFragmentInteractionListener;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Stack;

public class ListActivity extends AppCompatActivity implements OnFragmentInteractionListener, ListHeaderFragment.OnFragmentInteractionListener {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private EditText bemerkungenEditText;
    private Button checkAllButton, submitButton;
    private ListHeaderFragment listHeaderFragment;
    private ListScrollViewFragment listScrollViewFragment;
    private Stack<Pruefung> pruefungStack = new Stack<>();
    Stack<Parcelable> listStates = new Stack<>();
    private Menu menu;

    @Override
    public void onBackPressed() {
        if(pruefungStack.size() > 1) {
        pruefungStack.pop();
        updateActivity();
        listScrollViewFragment.setListState(listStates.pop());
    }else super.onBackPressed();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
        setContentView(R.layout.activity_list);
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
        updateActivity();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_list_menu, menu);
        if (this.listHeaderFragment.isShowing()) {
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setVisible(false);
        } else if (!this.listHeaderFragment.isShowing()) {
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(true);
        }
        this.menu = menu;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = getSharedPreferences(this.SHARED_PREFERENCES, MODE_PRIVATE);
        switch (item.getItemId())
        {
            case R.id.menu_hideHeader:
                if (this.listHeaderFragment.isShowing()) {
                this.listHeaderFragment.hide();
                item.setVisible(false);
                this.menu.getItem(3).setVisible(true);
            }
                break;
            case R.id.menu_showHeader:
                if(!this.listHeaderFragment.isShowing()) {
                    this.listHeaderFragment.show();
                    item.setVisible(false);
                    this.menu.getItem(2).setVisible(true);
                }
                break;
            case R.id.menu_previousList:
                String sql = "CALL getpruefung(" + (pruefungStack.size() - 1) + ", '" + pruefungStack.peek().getBarcode() + "')";
                try {
                    DBAsyncTask.getInstance(this, new DBAsyncResponse() {
                        @Override
                        public void processFinish(ArrayList<ContentValues> result) {
                            if ((result.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText()))) {
                                result.remove(0);
                                pruefungStack.push(new Pruefung(result));
                                listStates.push(listScrollViewFragment.getListState());
                                updateActivity();
                            }
                        }
                    }).execute(AsyncTaskOperationEnum.GET_DATA, prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true), sql);
                } catch (MalformedURLException e) {
                    Toast.makeText(this, "URL nicht korrekt", Toast.LENGTH_SHORT);
                }
                break;
            case R.id.menu_nextList:
                if(pruefungStack.size() > 1) {
                    pruefungStack.pop();
                    updateActivity();
                    listScrollViewFragment.setListState(listStates.pop());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateActivity() {
        this.listScrollViewFragment = ListScrollViewFragment.newInstance(this.pruefungStack.peek(), !(pruefungStack.size() > 1));
        this.listHeaderFragment = ListHeaderFragment.newInstance(this.pruefungStack.peek());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.listScrollViewFragment, this.listScrollViewFragment);
        transaction.replace(R.id.listHeaderFragment, this.listHeaderFragment);
        transaction.commitNow();
        this.bemerkungenEditText.setText(pruefungStack.peek().getBemerkungen());
        if((pruefungStack.size() > 1)){
           this.checkAllButton.setVisibility(View.GONE);
           this.submitButton.setVisibility(View.GONE);
           this.bemerkungenEditText.setEnabled(false);
        }else{
            this.checkAllButton.setVisibility(View.VISIBLE);
            this.submitButton.setVisibility(View.VISIBLE);
            this.bemerkungenEditText.setEnabled(true);
        }
    }

    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    protected void onStart() {
        super.onStart();
    }

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
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.listStates.size() > 0) {
            this.listScrollViewFragment.setListAdapter(new ListAdapter(this, pruefungStack.peek(), !(pruefungStack.size() > 1)));
            this.listScrollViewFragment.setListState(this.listStates.pop());
        }
        if (!this.listHeaderFragment.isShowing()) {
            this.listHeaderFragment.hide();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        listStates.push(this.listScrollViewFragment.getListState());
        outState.putSerializable("list_states", listStates);
        outState.putSerializable("pruefungStack", pruefungStack);
        outState.putBoolean("bemerkungen_is_showing", this.bemerkungenEditText.getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
    }

    protected void onRestart() {
        super.onRestart();
    }

    public void onClick(View view) {
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES, 0);
        switch (view.getId()) {
            case R.id.checkAllButton:
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
            case R.id.bemerkungenButton:
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
            case R.id.submitButton:
                final Pruefung pruefung = this.pruefungStack.peek();
                StringBuilder sqlBuilder = new StringBuilder();
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
                    DBAsyncTask.getInstance(this, new DBAsyncResponse() {
                        @Override
                        public void processFinish(ArrayList<ContentValues> result) {
                            if ((result.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCESS.getText())) {
                                ListActivity.this.finish();
                            }
                        }
                    }).execute(AsyncTaskOperationEnum.INSERT_DATA, prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true), sqlBuilder.toString().trim());
                }catch(MalformedURLException e) {
                    Toast.makeText(this, "URL nicht korrekt", Toast.LENGTH_SHORT);
                    return;
                }
            default:
                return;
        }
    }

    public void onFragmentInteraction(Uri uri) {
    }
}