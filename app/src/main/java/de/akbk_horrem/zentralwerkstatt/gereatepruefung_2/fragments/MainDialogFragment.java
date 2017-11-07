package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.MainActivity;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainDialogFragment extends Fragment {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private static boolean showing = false;
    private Button aendernButton;
    private EditText benutzerEditText;
    private OnFragmentInteractionListener mListener;
    private EditText passwortEditText;

    public interface OnFragmentInteractionListener {
        void onConnectionFailed();

        void onFragmentInteraction(Uri uri);

        void onLoginSucces();

        void onLogout();
    }

    public static MainDialogFragment newInstance(String param1, String param2) {
        return new MainDialogFragment();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_dialog, container, false);
        this.aendernButton = (Button) view.findViewById(R.id.benutzerButton);
        this.benutzerEditText = (EditText) view.findViewById(R.id.benutzerEditText);
        this.passwortEditText = (EditText) view.findViewById(R.id.passwortEditText);
        this.passwortEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (6 != actionId) {
                    return false;
                }
                MainDialogFragment.this.login();
                return true;
            }
        });
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0);
        if (savedInstanceState == null) {
            SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0).edit();
            prefsEdit.remove(SharedPreferenceEnum.PASSWORT.getText());
            prefsEdit.apply();
            this.benutzerEditText.setText(prefs.getString(SharedPreferenceEnum.BENUTZER.getText(), ""));
            this.passwortEditText.setText("");
        }
        return view;
    }

    public void onStart() {
        super.onStart();
        if (this.benutzerEditText.getText().toString().equals("")) {
            this.aendernButton.setEnabled(false);
            this.aendernButton.setVisibility(View.INVISIBLE);
            this.aendernButton.requestFocus();
            this.benutzerEditText.setEnabled(true);
            return;
        }
        this.aendernButton.setEnabled(true);
        this.aendernButton.setVisibility(View.VISIBLE);
        this.passwortEditText.requestFocus();
        this.benutzerEditText.setEnabled(false);
    }

    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations() && !((MainActivity) getActivity()).isStartingActivity()) {
            SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0).edit();
            prefsEdit.remove(SharedPreferenceEnum.PASSWORT.getText());
            prefsEdit.apply();
            this.mListener.onLogout();
        }
    }

    public void onStop() {
        super.onStop();
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void show() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).show(this).commit();
        showing = true;
    }

    public void hide() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).hide(this).commit();
        showing = false;
    }

    public void logout() {
        SharedPreferences.Editor prefsEdit = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0).edit();
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
        show();
    }

    public void login() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0);
        final SharedPreferences.Editor prefsEdit = prefs.edit();
        try {
            DBAsyncTask dBAsyncTask = new DBAsyncTask(getActivity(), new DBAsyncResponse() {
                public void processFinish(ArrayList<ContentValues> resultArray) {
                    if (((ContentValues) resultArray.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.LOGIN_SUCCES.getText())) {
                        MainDialogFragment.this.benutzerEditText.setEnabled(false);
                        MainDialogFragment.this.aendernButton.setEnabled(true);
                        MainDialogFragment.this.aendernButton.setVisibility(View.VISIBLE);
                        prefsEdit.putString(SharedPreferenceEnum.BENUTZER.getText(), MainDialogFragment.this.benutzerEditText.getText().toString());
                        prefsEdit.putString(SharedPreferenceEnum.PASSWORT.getText(), MainDialogFragment.this.passwortEditText.getText().toString());
                        prefsEdit.apply();
                        MainDialogFragment.this.passwortEditText.setText("");
                        MainDialogFragment.this.aendernButton.setEnabled(true);
                        MainDialogFragment.this.aendernButton.setVisibility(View.VISIBLE);
                        MainDialogFragment.this.mListener.onLoginSucces();
                    } else if (DBConnectionStatusEnum.CONNECTION_STATUS.getText().equals(DBConnectionStatusEnum.CONNECTION_FAILED.getText())) {
                        MainDialogFragment.this.mListener.onConnectionFailed();
                    }
                }
            }, this.benutzerEditText.getText().toString(), this.passwortEditText.getText().toString());
            String[] strArr = new String[2];
            strArr[0] = AsyncTaskOperationEnum.LOGIN.getText();
            strArr[1] = prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true) ? "1" : "0";
            dBAsyncTask.execute(strArr);
        } catch (MalformedURLException e) {
            Toast.makeText(getActivity(), "URL nicht korrekt", Toast.LENGTH_SHORT);
        }
    }

    public void changeUser() {
        this.aendernButton.setEnabled(false);
        this.benutzerEditText.requestFocus();
        this.benutzerEditText.setEnabled(true);
        logout();
    }

    public boolean isShowing() {
        return showing;
    }
}
