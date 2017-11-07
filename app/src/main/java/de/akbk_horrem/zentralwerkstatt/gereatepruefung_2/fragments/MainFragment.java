package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private Button loginButton;
    private OnFragmentInteractionListener mListener;
    private Button pruefenButton;
    private Button scannerButton;
    private Button syncButton;
    private Button verbindungButton;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public static MainFragment newInstance(String param1, String param2) {
        return new MainFragment();
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        this.loginButton = (Button) view.findViewById(R.id.loginButton);
        this.pruefenButton = (Button) view.findViewById(R.id.pruefenButton);
        this.verbindungButton = (Button) view.findViewById(R.id.verbindungButton);
        this.scannerButton = (Button) view.findViewById(R.id.einstellungenButton);
        this.syncButton = (Button) view.findViewById(R.id.syncButton);
        if (savedInstanceState != null) {
            this.loginButton.setEnabled(savedInstanceState.getBoolean("loginButtonEnabled"));
            this.pruefenButton.setEnabled(savedInstanceState.getBoolean("pruefenButtonEnabled"));
            this.verbindungButton.setEnabled(savedInstanceState.getBoolean("verbindungButtonEnabled"));
            this.scannerButton.setEnabled(savedInstanceState.getBoolean("scannerButtonEnabled"));
            this.syncButton.setEnabled(savedInstanceState.getBoolean("syncButtonEnabled"));
        }
        return view;
    }

    public void onStart() {
        super.onStart();
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("loginButtonEnabled", this.loginButton.isEnabled());
        outState.putBoolean("pruefenButtonEnabled", this.pruefenButton.isEnabled());
        outState.putBoolean("verbindungButtonEnabled", this.verbindungButton.isEnabled());
        outState.putBoolean("scannerButtonEnabled", this.scannerButton.isEnabled());
        outState.putBoolean("syncButtonEnabled", this.syncButton.isEnabled());
        super.onSaveInstanceState(outState);
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }
}
