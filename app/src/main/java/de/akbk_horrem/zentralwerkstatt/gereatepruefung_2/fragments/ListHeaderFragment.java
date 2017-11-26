package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;

public class ListHeaderFragment extends Fragment {
    private static final String ARG_PARAMS = "pruefung";
    private static boolean showing = true;
    private TextView footerTextView;
    private TextView geraetetypTextViewRight;
    private TextView headerTextView;
    private TextView herstellerTextViewRight;
    private TextView barcodeTextViewRight;
    private TextView datumTextViewRight;
    private OnFragmentInteractionListener mListener;
    private Pruefung pruefung;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public static ListHeaderFragment newInstance(Pruefung pruefung) {
        ListHeaderFragment fragment = new ListHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAMS, pruefung);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.pruefung = getArguments().getParcelable(ARG_PARAMS);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_header, container, false);
        this.geraetetypTextViewRight = view.findViewById(R.id.geraetetypTextViewRight);
        this.herstellerTextViewRight = view.findViewById(R.id.herstellerTextViewRight);
        this.barcodeTextViewRight = view.findViewById(R.id.barcodeTextViewRight);
        this.datumTextViewRight = view.findViewById(R.id.datumTextViewRight);
        this.headerTextView = view.findViewById(R.id.headerTextView);
        this.footerTextView = view.findViewById(R.id.footerTextView);
        updateView(null);
        return view;
    }

    public void updateView(@Nullable Pruefung pruefung) {
        if (pruefung != null) this.pruefung = pruefung;
        this.geraetetypTextViewRight.setText(this.pruefung.getGeraeteName());
        this.herstellerTextViewRight.setText(this.pruefung.getHerstellerName());
        this.barcodeTextViewRight.setText(this.pruefung.getBarcode());
        this.datumTextViewRight.setText(this.pruefung.getFormatDatum());
        if (this.pruefung.getHeaderText() == null || this.pruefung.getHeaderText().equals("")) {
            this.headerTextView.setVisibility(View.GONE);
        } else {
            this.headerTextView.setText(this.pruefung.getHeaderText());
        }
        if (this.pruefung.getFooterText() == null || this.pruefung.getFooterText().equals("")) {
            this.footerTextView.setVisibility(View.GONE);
        } else {
            this.footerTextView.setText(this.pruefung.getFooterText());
        }
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