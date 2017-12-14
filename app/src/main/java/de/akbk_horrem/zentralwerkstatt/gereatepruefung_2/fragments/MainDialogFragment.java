package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.MainActivity;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.DBUtils;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.tempDB.PruefungDBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MainDialogFragment extends Fragment {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private boolean showing = true; //Wird der Fragment angezeigt?
    private Button aendernButton;
    private EditText benutzerEditText;
    private OnFragmentInteractionListener mListener;
    private EditText passwortEditText;

    public interface OnFragmentInteractionListener {

        /**
         * Wird aufgerufen wenn Verbindung mit der Hauptdatenbank fehlschlägt
         */
        void onConnectionFailed();

        /**
         * Wird aufgerufen wenn ein Benutzer erfolgreichen Einlogevorgang durchgeführt hat
         * @param benutzer Benutzername des Eingelogten Benutzers
         */
        void onLoginSucces(String benutzer);

        /**
         * Wird aufgerufen wenn ein Benutzer sich ausgeloggt hat
         */
        void onLogout();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View vorbereiten
        View view = inflater.inflate(R.layout.fragment_main_dialog, container, false);

        //Zuweisung der Views
        this.aendernButton = view.findViewById(R.id.benutzerButton);
        this.benutzerEditText = view.findViewById(R.id.benutzerEditText);
        this.passwortEditText = view.findViewById(R.id.passwortEditText);
        this.passwortEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Falls Eingabe bestätigt wurde
                if (EditorInfo.IME_ACTION_DONE == actionId) {
                    MainDialogFragment.this.login();
                    return true;
                }
                return false;
            }
        });
        if (savedInstanceState == null) {
            //Gespeichertes Passwort löschen und anzeigen von zuletzt eingeloggten Benutzer
            SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
            prefsEdit.remove(SharedPreferenceEnum.PASSWORT.getText());
            prefsEdit.apply();
            this.benutzerEditText.setText(prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), ""));
            this.passwortEditText.setText("");
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //Wenn es keinen gespeicherten Benutzer gibt
        if (this.benutzerEditText.getText().toString().equals("")) {
            this.aendernButton.setEnabled(false);
            this.aendernButton.setVisibility(View.INVISIBLE);
            this.aendernButton.requestFocus();
            this.benutzerEditText.setEnabled(true);
        }else { //Ansonsten
            this.aendernButton.setEnabled(true);
            this.aendernButton.setVisibility(View.VISIBLE);
            this.passwortEditText.requestFocus();
            this.benutzerEditText.setEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Wenn der Fragment pausiert wird und keine neue Activity gestartet wird und keine Konfigurationsänderung vorliegt (z.B. Orientierungswechsel)
        if (!getActivity().isChangingConfigurations() && !((MainActivity) getActivity()).isStartingActivity()) {
            SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
            prefsEdit.remove(SharedPreferenceEnum.PASSWORT.getText());
            prefsEdit.apply();
            this.mListener.onLogout();
        }
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
     * Die Methode führt Aktionen durch die bei einem logout ausgeführt werden
     */
    public void logout() {
        //Bentuzerdaten löschen
        SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        prefsEdit.remove(SharedPreferenceEnum.BENUTZER.getText());
        prefsEdit.remove(SharedPreferenceEnum.PASSWORT.getText());
        prefsEdit.apply();
        this.benutzerEditText.setText("");
        this.benutzerEditText.setEnabled(true);
        this.benutzerEditText.requestFocus();
        this.passwortEditText.setText("");
        this.aendernButton.setEnabled(false);
        this.aendernButton.setVisibility(View.INVISIBLE);
        this.mListener.onLogout();
        //show();
    }

    /**
     * Die Methode führt Aktionen durch die bei einem login ausgeführt werden
     */
    public void login() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        if(!prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {
            try {
                //Login durchführen
                DBAsyncTask.getLoginInstance(getActivity(), new DBAsyncTask.DBAsyncResponse() {
                    public void processFinish(ArrayList<ContentValues> resultArray) {
                        //Wenn Einloggen erfolgreich war
                        if (resultArray.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.LOGIN_SUCCESS.getText())) {

                            SharedPreferences.Editor prefsEdit = prefs.edit();

                            String user = MainDialogFragment.this.benutzerEditText.getText().toString(),
                                    password = MainDialogFragment.this.passwortEditText.getText().toString();

                            //Benutzerdaten speichern
                            prefsEdit.putString(SharedPreferenceEnum.BENUTZER.getText(), user);
                            prefsEdit.putString(SharedPreferenceEnum.PASSWORT.getText(), password);
                            prefsEdit.apply();
                            MainDialogFragment.this.benutzerEditText.setEnabled(false);
                            MainDialogFragment.this.passwortEditText.setText("");
                            MainDialogFragment.this.aendernButton.setEnabled(true);
                            MainDialogFragment.this.aendernButton.setVisibility(View.VISIBLE);
                            MainDialogFragment.this.mListener.onLoginSucces(user);

                        }else if(resultArray.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.LOGIN_FAILED.getText())) {
                            invalidatePassword();
                            MainDialogFragment.this.mListener.onLogout();
                        } else { //Ansonsten
                            MainDialogFragment.this.mListener.onConnectionFailed();
                        }
                    }
                }, this.benutzerEditText.getText().toString(), this.passwortEditText.getText().toString()).
                        execute(AsyncTaskOperationEnum.LOGIN,
                                prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
            } catch (MalformedURLException e) {
                mListener.onConnectionFailed();
                Toast.makeText(getActivity(), "URL nicht korrekt", Toast.LENGTH_SHORT);
            }
        }else{
            //Login durchführen
            DBUtils.login(getActivity(), new PruefungDBAsyncTask.DBAsyncResponse() {
                @Override
                public void processFinish(ArrayList<ContentValues> resultArrayList) {
                    //Wenn Einloggen erfolgreich war
                    if(resultArrayList != null){
                    SharedPreferences.Editor prefsEdit = prefs.edit();

                    String user = MainDialogFragment.this.benutzerEditText.getText().toString(),
                            password = MainDialogFragment.this.passwortEditText.getText().toString();

                    //Benutzerdaten speichern
                    prefsEdit.putString(SharedPreferenceEnum.BENUTZER.getText(), user);
                    prefsEdit.putString(SharedPreferenceEnum.PASSWORT.getText(), password);
                    prefsEdit.commit();

                    MainDialogFragment.this.benutzerEditText.setEnabled(false);
                    MainDialogFragment.this.passwortEditText.setText("");
                    MainDialogFragment.this.aendernButton.setEnabled(true);
                    MainDialogFragment.this.aendernButton.setVisibility(View.VISIBLE);
                    MainDialogFragment.this.mListener.onLoginSucces(user);

                } else { //Ansonsten
                        invalidatePassword();
                }
                }
            }, this.benutzerEditText.getText().toString(), passwortEditText.getText().toString());
        }
    }

    /**
     * Löscht eventuellen Passworteintrag in SharedPreferences und passt die Activity an, für den Fall wo kein Nutzer eingeloggt ist, aber ein Benutzername in SharedPreferences existiert
     */
    public void invalidatePassword(){
        SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE).edit();
        prefsEdit.remove(SharedPreferenceEnum.PASSWORT.getText());
        prefsEdit.apply();
        MainDialogFragment.this.benutzerEditText.setEnabled(true);
        MainDialogFragment.this.passwortEditText.setText("");
        MainDialogFragment.this.aendernButton.setEnabled(false);
        MainDialogFragment.this.aendernButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Die Methode führt Aktionen durch die bei einem Benutzerwechsel ausgeführt werden
     */
    public void changeUser() {
        this.aendernButton.setEnabled(false);
        this.benutzerEditText.requestFocus();
        this.benutzerEditText.setEnabled(true);
        logout();
    }

    /**
     * Die Funktion gibt an ob der Fragment zurzeit angezeigt wird
     * @return Die Funktion liefert true zurück wenn der Fragment angezeigt wird
     */
    public boolean isShowing() {
        return showing;
    }
}
