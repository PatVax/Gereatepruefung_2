package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import java.util.ArrayList;

public class ListHeaderFragment extends Fragment {
    private static final String ARG_PARAMS = "list";
    private static boolean showing = true;
    private TextView footerTextView;
    private TextView geraetetypTextViewRight;
    private TextView headerTextView;
    private TextView herstellerTextViewRight;
    private OnFragmentInteractionListener mListener;
    private ArrayList<ContentValues> valuesArray;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public static ListHeaderFragment newInstance(ArrayList<Parcelable> params) {
        ListHeaderFragment fragment = new ListHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAMS, params);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.valuesArray = getArguments().getParcelableArrayList(ARG_PARAMS);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_header, container, false);
        this.geraetetypTextViewRight = (TextView) view.findViewById(R.id.geraetetypTextViewRight);
        this.herstellerTextViewRight = (TextView) view.findViewById(R.id.herstellerTextViewRight);
        this.headerTextView = (TextView) view.findViewById(R.id.headerTextView);
        this.footerTextView = (TextView) view.findViewById(R.id.footerTextView);
        this.geraetetypTextViewRight.setText(((ContentValues) this.valuesArray.get(0)).getAsString("GeraeteName"));
        this.herstellerTextViewRight.setText(((ContentValues) this.valuesArray.get(0)).getAsString("HerstellerName"));
        if (((ContentValues) this.valuesArray.get(0)).getAsString("HeaderText").equals("")) {
            this.headerTextView.setVisibility(View.GONE);
        } else {
            this.headerTextView.setText(((ContentValues) this.valuesArray.get(0)).getAsString("HeaderText"));
        }
        if (((ContentValues) this.valuesArray.get(0)).getAsString("FooterText").equals("")) {
            this.footerTextView.setVisibility(View.GONE);
        } else {
            this.footerTextView.setText(((ContentValues) this.valuesArray.get(0)).getAsString("FooterText"));
        }
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (this.mListener != null) {
            this.mListener.onFragmentInteraction(uri);
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            this.mListener = (OnFragmentInteractionListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
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

    public boolean isShowing() {
        return showing;
    }
}