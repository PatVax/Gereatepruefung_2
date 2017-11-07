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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter.ListAdapter;
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

public class ListActivity extends AppCompatActivity implements OnFragmentInteractionListener, ListHeaderFragment.OnFragmentInteractionListener {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private EditText bemerkungenEditText;
    private Button checkAllButton;
    private ListHeaderFragment listHeaderFragment;
    private ListScrollViewFragment listScrollViewFragment;
    Parcelable listState;
    private Menu menu;
    private Button submitButton;
    private ArrayList<ContentValues> values;
    ArrayList<ContentValues> valuesArray;
    ContentValues viewStates;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(240);
        setContentView((int) R.layout.activity_list);
        this.listScrollViewFragment = ListScrollViewFragment.newInstance(getIntent().getParcelableArrayListExtra("contents"));
        this.listHeaderFragment = ListHeaderFragment.newInstance(getIntent().getParcelableArrayListExtra("contents"));
        this.bemerkungenEditText = (EditText) findViewById(R.id.bemerkungenEditText);
        this.checkAllButton = (Button) findViewById(R.id.checkAllButton);
        this.submitButton = (Button) findViewById(R.id.submitButton);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.listScrollViewFragment, this.listScrollViewFragment);
        transaction.replace(R.id.listHeaderFragment, this.listHeaderFragment);
        transaction.commit();
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
        if (item.getItemId() == R.id.menu_hideHeader) {
            if (this.listHeaderFragment.isShowing()) {
                this.listHeaderFragment.hide();
                item.setVisible(false);
                this.menu.getItem(3).setVisible(true);
            }
        } else if (item.getItemId() == R.id.menu_showHeader && !this.listHeaderFragment.isShowing()) {
            this.listHeaderFragment.show();
            item.setVisible(false);
            this.menu.getItem(2).setVisible(true);
        }
        return super.onOptionsItemSelected(item);
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
            this.listState = savedInstanceState.getParcelable("list_state");
            this.valuesArray = savedInstanceState.getParcelableArrayList("list_items");
            this.viewStates = (ContentValues) savedInstanceState.getParcelable("list_items_state");
            if (savedInstanceState.getBoolean("bemerkungen_is_showing")) {
                this.bemerkungenEditText.setVisibility(0);
            } else {
                this.bemerkungenEditText.setVisibility(8);
            }
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.listState != null) {
            this.listScrollViewFragment.setListAdapter(new ListAdapter(this, this.valuesArray, this.viewStates));
            this.listScrollViewFragment.getListView().onRestoreInstanceState(this.listState);
        }
        this.listState = null;
        if (!this.listHeaderFragment.isShowing()) {
            this.listHeaderFragment.hide();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("list_state", this.listScrollViewFragment.getListView().onSaveInstanceState());
        outState.putParcelableArrayList("list_items", ((ListAdapter) this.listScrollViewFragment.getListView().getAdapter()).getValuesArray());
        outState.putParcelable("list_items_state", ((ListAdapter) this.listScrollViewFragment.getListView().getAdapter()).getCurrentViewState());
        outState.putBoolean("bemerkungen_is_showing", this.bemerkungenEditText.getVisibility() == 0);
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
                if (this.bemerkungenEditText.getVisibility() == 8) {
                    this.bemerkungenEditText.setVisibility(0);
                    return;
                } else if (this.bemerkungenEditText.getVisibility() == 0) {
                    this.bemerkungenEditText.setVisibility(8);
                    return;
                } else {
                    this.bemerkungenEditText.setVisibility(0);
                    return;
                }
            case R.id.submitButton:
                ListAdapter bufferListAdapter = (ListAdapter) this.listScrollViewFragment.getListView().getAdapter();
                final ArrayList<ContentValues> bufferValuesArray = bufferListAdapter.getValuesArray();
                final ContentValues bufferCurrentViewState = bufferListAdapter.getCurrentViewState();
                String sql = "INSERT INTO prüfungen (Geräte_Barcode, idBenutzer, Datum, Bemerkungen) VALUES ('" + ((ContentValues) bufferValuesArray.get(0)).getAsString("Geräte_Barcode") + "', (SELECT idBenutzer FROM Benutzer WHERE Benutzername = '" + prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), "") + "'), CURDATE(), '" + this.bemerkungenEditText.getText().toString() + "')";
                try {
                    DBAsyncTask dBAsyncTask = new DBAsyncTask(this, new DBAsyncResponse() {
                        public void processFinish(ArrayList<ContentValues> result) {
                            String sql = "INSERT INTO prüfergebnisse VALUES ";
                            if (((ContentValues) result.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCES.getText())) {
                                StringBuilder builder = new StringBuilder();
                                String barcode = ((ContentValues) bufferValuesArray.get(0)).getAsString("Geräte_Barcode");
                                for (int i = 0; i < bufferValuesArray.size() - 1; i++) {
                                    String asString = ((ContentValues) bufferValuesArray.get(i)).getAsString("Anzeigeart");
                                    int i2 = -1;
                                    switch (asString.hashCode()) {
                                        case 98:
                                            if (asString.equals("b")) {
                                                i2 = 0;
                                                break;
                                            }
                                            break;
                                    }
                                    switch (i2) {
                                        case 0:
                                            builder.append("((SELECT p.IDPrüfung FROM prüfungen p WHERE p.geräte_barcode = '" + barcode + "' ORDER BY p.Datum DESC LIMIT 1), " + ((ContentValues) bufferValuesArray.get(i)).getAsString("IDKriterium") + ", '" + (bufferCurrentViewState.getAsBoolean(new StringBuilder().append("").append(i).toString()).booleanValue() ? "true" : "false") + "'),");
                                            break;
                                        default:
                                            builder.append("((SELECT p.IDPrüfung FROM prüfungen p WHERE p.geräte_barcode = '" + barcode + "' ORDER BY p.Datum DESC LIMIT 1), " + ((ContentValues) bufferValuesArray.get(i)).getAsString("IDKriterium") + ", '" + bufferCurrentViewState.getAsString("" + i) + "'),");
                                            break;
                                    }
                                }
                                builder.append("((SELECT p.IDPrüfung FROM prüfungen p WHERE p.geräte_barcode = '" + barcode + "' ORDER BY p.Datum DESC LIMIT 1), " + ((ContentValues) bufferValuesArray.get(bufferValuesArray.size() - 1)).getAsString("IDKriterium") + ", '" + (bufferCurrentViewState.getAsBoolean(new StringBuilder().append("").append(bufferValuesArray.size() + -1).toString()).booleanValue() ? "true" : "false") + "')");
                                try {
                                    DBAsyncTask dBAsyncTask = new DBAsyncTask(ListActivity.this, new DBAsyncResponse() {
                                        public void processFinish(ArrayList<ContentValues> result) {
                                            if (((ContentValues) result.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.SUCCES.getText())) {
                                                ListActivity.this.finish();
                                            }
                                        }
                                    });
                                    String[] strArr = new String[3];
                                    strArr[0] = AsyncTaskOperationEnum.INSERT_DATA.getText();
                                    strArr[1] = prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true) ? "1" : "0";
                                    strArr[2] = sql + builder.toString().trim();
                                    dBAsyncTask.execute(strArr);
                                } catch (MalformedURLException e) {
                                    Toast.makeText(ListActivity.this, "URL nicht korrekt", 0);
                                }
                            }
                        }
                    });
                    String[] strArr = new String[3];
                    strArr[0] = AsyncTaskOperationEnum.INSERT_DATA.getText();
                    strArr[1] = prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true) ? "1" : "0";
                    strArr[2] = sql;
                    dBAsyncTask.execute(strArr);
                    return;
                } catch (MalformedURLException e) {
                    Toast.makeText(this, "URL nicht korrekt", 0);
                    return;
                }
            default:
                return;
        }
    }

    public void onFragmentInteraction(Uri uri) {
    }
}