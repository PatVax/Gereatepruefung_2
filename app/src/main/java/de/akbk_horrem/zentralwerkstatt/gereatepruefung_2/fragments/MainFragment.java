package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private Button loginButton,
            pruefenButton,
            scannerButton,
            syncButton,
            verbindungButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //View vorbereiten
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Zuweisung der Views
        this.loginButton = view.findViewById(R.id.loginButton);
        this.pruefenButton = view.findViewById(R.id.pruefenButton);
        this.verbindungButton = view.findViewById(R.id.verbindungButton);
        this.scannerButton = view.findViewById(R.id.einstellungenButton);
        this.syncButton = view.findViewById(R.id.syncButton);

        if (savedInstanceState != null) { //Wenn ein gespeicherter Zustand vorliegt den Zustand annehmen
            this.loginButton.setEnabled(savedInstanceState.getBoolean("loginButtonEnabled"));
            this.pruefenButton.setEnabled(savedInstanceState.getBoolean("pruefenButtonEnabled"));
            this.verbindungButton.setEnabled(savedInstanceState.getBoolean("verbindungButtonEnabled"));
            this.scannerButton.setEnabled(savedInstanceState.getBoolean("scannerButtonEnabled"));
            this.syncButton.setEnabled(savedInstanceState.getBoolean("syncButtonEnabled"));
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Zustand der Views speichern
        outState.putBoolean("loginButtonEnabled", this.loginButton.isEnabled());
        outState.putBoolean("pruefenButtonEnabled", this.pruefenButton.isEnabled());
        outState.putBoolean("verbindungButtonEnabled", this.verbindungButton.isEnabled());
        outState.putBoolean("scannerButtonEnabled", this.scannerButton.isEnabled());
        outState.putBoolean("syncButtonEnabled", this.syncButton.isEnabled());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
