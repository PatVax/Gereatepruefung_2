package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerbindungEinstellungenFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class VerbindungEinstellungenFragment extends Fragment {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private static boolean showing = true; //Wird der Fragment angezeigt?
    private OnFragmentInteractionListener mListener;
    private CheckBox offlineModeCheckBox;
    private EditText pfadEditText;
    private EditText rootEditText;
    private CheckBox showToastCheckBox;
    private EditText uriEditText;

    public interface OnFragmentInteractionListener {
        /**
         * Wird aufgerufen wenn Verbindung mit der Hauptdatenbank fehlschlägt
         */
        void onConnectionFailed();

        /**
         * Wird aufgerufen wenn Verbindung mit der Hauptdatenbank erfolgreich abgeschlossen wird
         */
        void onConnectionSucces();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View vorbereiten
        View view = inflater.inflate(R.layout.fragment_verbindung_einstellungen, container, false);

        //Zuweisung der Views
        this.uriEditText = view.findViewById(R.id.uriEditText);
        this.pfadEditText = view.findViewById(R.id.pfadEditText);
        this.rootEditText = view.findViewById(R.id.rootEditText);
        this.rootEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Falls Eingabe bestätigt wurde
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    VerbindungEinstellungenFragment.this.acceptConnection();
                    return true;
                }
                return false;
            }
        });
        this.offlineModeCheckBox = view.findViewById(R.id.offlineModeCheckBox);
        this.offlineModeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Offline Modus umschalten
                SharedPreferences.Editor prefsEdit = VerbindungEinstellungenFragment.this.getActivity().getSharedPreferences(VerbindungEinstellungenFragment.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                uriEditText.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                pfadEditText.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                rootEditText.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                prefsEdit.putBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), isChecked);
                prefsEdit.apply();
                mListener.onConnectionSucces();
                if(isChecked) cancelConnection();
                else acceptConnection();
            }
        });
        this.showToastCheckBox = view.findViewById(R.id.showMessageCheckBox);
        this.showToastCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //ShowToast Modus umschalten
                SharedPreferences.Editor prefsEdit = VerbindungEinstellungenFragment.this.getActivity().getSharedPreferences(VerbindungEinstellungenFragment.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
                prefsEdit.putBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), isChecked);
                prefsEdit.apply();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Gespeicherte Verbindungseinstellungen wiederherstellen
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0);
        this.uriEditText.setText(prefs.getString(SharedPreferenceEnum.HOST.getText(), ""));
        this.pfadEditText.setText(prefs.getString(SharedPreferenceEnum.PFAD.getText(), ""));
        this.rootEditText.setText(prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), ""));
        if (prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {
            this.uriEditText.setVisibility(View.GONE);
            this.pfadEditText.setVisibility(View.GONE);
            this.rootEditText.setVisibility(View.GONE);
            this.offlineModeCheckBox.setChecked(true);
        } else {
            this.uriEditText.setVisibility(View.VISIBLE);
            this.pfadEditText.setVisibility(View.VISIBLE);
            this.rootEditText.setVisibility(View.VISIBLE);
            this.offlineModeCheckBox.setChecked(false);
        }
        this.showToastCheckBox.setChecked(prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    /**
     * Zeigt den Fragment. Showing wird true gesetzt.
     */
    public void show() {
        if(!showing) {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).show(this).commit();
            showing = true;
        }
    }

    /**
     * Versteckt den Fragment. Showing wird false gesetzt
     */
    public void hide() {
        if(showing) {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).hide(this).commit();
            showing = false;
        }
    }

    /**
     * Die Methode führt Aktionen durch die bei einem Verbindungsversuch ausgeführt werden
     */
    public void acceptConnection() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        try {
            //Führt Verbindungscheck aus mit den benutzereingabebedingten Einstellungen
            DBAsyncTask.getConnectionCheckInstance(getActivity(), new DBAsyncTask.DBAsyncResponse() {
                public void processFinish(ArrayList<ContentValues> resultArray) {
                    //Wenn Verbindung erfolgreich war
                    if (resultArray.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText())) {
                        //Neue funktionsfähige Werte werden gespeichert für künftige Anfragen
                        SharedPreferences.Editor prefsEdit = prefs.edit();
                        prefsEdit.putString(SharedPreferenceEnum.HOST.getText(), VerbindungEinstellungenFragment.this.uriEditText.getText().toString());
                        prefsEdit.putString(SharedPreferenceEnum.PFAD.getText(), VerbindungEinstellungenFragment.this.pfadEditText.getText().toString());
                        prefsEdit.putString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), VerbindungEinstellungenFragment.this.rootEditText.getText().toString());
                        VerbindungEinstellungenFragment.this.offlineModeCheckBox.setChecked(false);
                        prefsEdit.apply();
                        VerbindungEinstellungenFragment.this.mListener.onConnectionSucces();
                    } else VerbindungEinstellungenFragment.this.mListener.onConnectionFailed(); //Ansonsten
                }
            }, new URL(String.format("%s/%s", this.uriEditText.getText().toString(), this.pfadEditText.getText().toString())), this.rootEditText.getText().toString()).
                    execute(AsyncTaskOperationEnum.CHECK_CONNECTION,
                            prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
        } catch (MalformedURLException e) {
            this.mListener.onConnectionFailed();
            Toast.makeText(getActivity(), "URL nicht korrekt", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Die Methode führt Aktionen durch die bei einem Verbindungseinstellungenabbruch ausgeführt werden
     */
    public void cancelConnection() {
        //Verbindungseinstellungen zurücksetzen
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        this.uriEditText.setText(prefs.getString(SharedPreferenceEnum.HOST.getText(), ""));
        this.pfadEditText.setText(prefs.getString(SharedPreferenceEnum.PFAD.getText(), ""));
        this.rootEditText.setText(prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), ""));
        hide();
    }

    /**
     * Die Funktion gibt an ob der Fragment zurzeit angezeigt wird
     * @return Die Funktion liefert true zurück wenn der Fragment angezeigt wird
     */
    public boolean isShowing() {
        return showing;
    }
}
