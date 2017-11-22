package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VerbindungEinstellungenFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VerbindungEinstellungenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VerbindungEinstellungenFragment extends Fragment {
    private static final String SHARED_PREFERENCES = SharedPreferenceEnum.SHARED_PREFERENCE.getText();
    private static boolean showing;
    private OnFragmentInteractionListener mListener;
    private CheckBox offlineModeCheckBox;
    private EditText pfadEditText;
    private EditText rootEditText;
    private TextView rootTextView;
    private CheckBox showToastCheckBox;
    private EditText uriEditText;

    public interface OnFragmentInteractionListener {
        void onConnectionFailed();

        void onConnectionSucces();

        void onFragmentInteraction(Uri uri);
    }

    public static VerbindungEinstellungenFragment newInstance() {
        return new VerbindungEinstellungenFragment();
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
        View view = inflater.inflate(R.layout.fragment_verbindung_einstellungen, container, false);
        this.uriEditText = (EditText) view.findViewById(R.id.uriEditText);
        this.pfadEditText = (EditText) view.findViewById(R.id.pfadEditText);
        this.rootEditText = (EditText) view.findViewById(R.id.rootEditText);
        this.rootEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (6 != actionId) {
                    return false;
                }
                VerbindungEinstellungenFragment.this.acceptConnection();
                return true;
            }
        });
        this.rootTextView = (TextView) view.findViewById(R.id.rootTextView);
        try {
            DBAsyncTask.getInstance(getActivity(), new DBAsyncResponse() {
                public void processFinish(ArrayList<ContentValues> result) {
                    if (((ContentValues) result.get(0)).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()) == DBConnectionStatusEnum.CONNECTED.getText()) {
                        VerbindungEinstellungenFragment.this.rootTextView.setText(((ContentValues) result.get(0)).getAsString(DBConnectionStatusEnum.DATABASE_USER.getText()) + "-Passwort");
                    } else {
                        VerbindungEinstellungenFragment.this.rootTextView.setText("Datenbankbenutzer-Passwort");
                    }
                }
            }).execute(AsyncTaskOperationEnum.GET_DATABASE_USER, false);
        } catch (MalformedURLException e) {
        }
        this.offlineModeCheckBox = (CheckBox) view.findViewById(R.id.offlineModeCheckBox);
        this.offlineModeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int i;
                int i2 = 8;
                SharedPreferences.Editor prefsEdit = VerbindungEinstellungenFragment.this.getActivity().getSharedPreferences(VerbindungEinstellungenFragment.SHARED_PREFERENCES, 0).edit();
                VerbindungEinstellungenFragment.this.uriEditText.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                EditText access$300 = VerbindungEinstellungenFragment.this.pfadEditText;
                if (isChecked) {
                    i = 8;
                } else {
                    i = 0;
                }
                access$300.setVisibility(i);
                EditText access$400 = VerbindungEinstellungenFragment.this.rootEditText;
                if (!isChecked) {
                    i2 = 0;
                }
                access$400.setVisibility(i2);
                prefsEdit.putBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), isChecked);
                prefsEdit.apply();
            }
        });
        this.showToastCheckBox = (CheckBox) view.findViewById(R.id.showMessageCheckBox);
        this.showToastCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor prefsEdit = VerbindungEinstellungenFragment.this.getActivity().getSharedPreferences(VerbindungEinstellungenFragment.SHARED_PREFERENCES, 0).edit();
                prefsEdit.putBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), isChecked);
                prefsEdit.apply();
            }
        });
        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onStart() {
        super.onStart();
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

    public void acceptConnection() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0);
        final SharedPreferences.Editor prefsEdit = prefs.edit();
        try {
            DBAsyncTask.getConnectionCheckInstance(getActivity(), new DBAsyncResponse() {
                public void processFinish(ArrayList<ContentValues> resultArray) {
                    if (resultArray.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText())) {
                        prefsEdit.putString(SharedPreferenceEnum.HOST.getText(), VerbindungEinstellungenFragment.this.uriEditText.getText().toString());
                        prefsEdit.putString(SharedPreferenceEnum.PFAD.getText(), VerbindungEinstellungenFragment.this.pfadEditText.getText().toString());
                        prefsEdit.putString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), VerbindungEinstellungenFragment.this.rootEditText.getText().toString());
                        VerbindungEinstellungenFragment.this.offlineModeCheckBox.setChecked(false);
                        try {
                            DBAsyncTask.getInstance(VerbindungEinstellungenFragment.this.getActivity(), new DBAsyncResponse() {
                                public void processFinish(ArrayList<ContentValues> result) {
                                    if (result.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText())) {
                                        VerbindungEinstellungenFragment.this.rootTextView.setText(result.get(0).getAsString(DBConnectionStatusEnum.DATABASE_USER.getText()) + "-Passwort");
                                    } else {
                                        VerbindungEinstellungenFragment.this.rootTextView.setText("Datenbankbenutzer-Passwort");
                                    }
                                }
                            }).execute(AsyncTaskOperationEnum.GET_DATABASE_USER, false);
                        } catch (MalformedURLException e) {
                        }
                        prefsEdit.apply();
                        prefsEdit.commit();
                        VerbindungEinstellungenFragment.this.mListener.onConnectionSucces();
                        return;
                    }
                    VerbindungEinstellungenFragment.this.mListener.onConnectionFailed();
                }
            }, new URL(String.format("%s/%s", new Object[]{this.uriEditText.getText().toString(), this.pfadEditText.getText().toString()})), this.rootEditText.getText().toString()).
                    execute(AsyncTaskOperationEnum.CHECK_CONNECTION,
                            prefs.getBoolean(SharedPreferenceEnum.SHOW_MESSAGE.getText(), true));
        } catch (MalformedURLException e) {
            this.mListener.onConnectionFailed();
            Toast.makeText(getActivity(), "URL nicht korrekt", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelConnection() {
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES, 0);
        this.uriEditText.setText(prefs.getString(SharedPreferenceEnum.HOST.getText(), ""));
        this.pfadEditText.setText(prefs.getString(SharedPreferenceEnum.PFAD.getText(), ""));
        this.rootEditText.setText(prefs.getString(SharedPreferenceEnum.ROOT_PASSWORT.getText(), ""));
        hide();
    }

    public boolean isShowing() {
        return showing;
    }
}
