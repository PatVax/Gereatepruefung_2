package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListHeaderFragment extends Fragment {
    private static final String ARG_PARAMS = "pruefung";
    private static boolean showing = true;
    private TextView footerTextView;
    private TextView geraetetypTextViewRight;
    private TextView headerTextView;
    private TextView herstellerTextViewRight;
    private TextView barcodeTextViewRight;
    private TextView seriennummerTextViewRight;
    private TextView datumTextViewRight;

    /**
     * Erzeugt eine Neue Instanz der Klasse
     * @param pruefung Die Prüfung das von dem Fragment dargestellt werden soll
     * @return Das neuerzeugte Objekt
     */
    public static ListHeaderFragment newInstance(Pruefung pruefung) {
        ListHeaderFragment fragment = new ListHeaderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAMS, pruefung);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_header, container, false);
        this.geraetetypTextViewRight = view.findViewById(R.id.geraetetypTextViewRight);
        this.herstellerTextViewRight = view.findViewById(R.id.herstellerTextViewRight);
        this.barcodeTextViewRight = view.findViewById(R.id.barcodeTextViewRight);
        this.seriennummerTextViewRight = view.findViewById(R.id.seriennummerTextViewRight);
        this.datumTextViewRight = view.findViewById(R.id.datumTextViewRight);
        this.headerTextView = view.findViewById(R.id.headerTextView);
        this.footerTextView = view.findViewById(R.id.footerTextView);
        if(getArguments() != null) updateView((Pruefung)getArguments().getParcelable(ARG_PARAMS));
        return view;
    }

    /**
     * Aktualisiert den View anhand der Prüfung
     * @param pruefung Prüfung Instanz für die, das View angepasst werden soll. Darf nicht null sein.
     */
    public void updateView(@NonNull Pruefung pruefung) {
        if(pruefung != null) {
            this.geraetetypTextViewRight.setText(pruefung.getGeraeteName());
            this.herstellerTextViewRight.setText(pruefung.getHerstellerName());
            this.barcodeTextViewRight.setText(pruefung.getBarcode());
            this.seriennummerTextViewRight.setText(pruefung.getSeriennummer());
            this.datumTextViewRight.setText(pruefung.getFormatDatum());
            if (pruefung.getHeaderText() == null || pruefung.getHeaderText().equals("")) {
                this.headerTextView.setVisibility(View.GONE);
            } else {
                this.headerTextView.setText(pruefung.getHeaderText());
            }
            if (pruefung.getFooterText() == null || pruefung.getFooterText().equals("")) {
                this.footerTextView.setVisibility(View.GONE);
            } else {
                this.footerTextView.setText(pruefung.getFooterText());
            }
        }
    }

    /**
     * Zeigt den Fragment. Showing wird true gesetzt.
     */
    public void show() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).show(this).commit();
        showing = true;
    }

    /**
     * Versteckt den Fragment. Showing wird false gesetzt
     */
    public void hide() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.abc_slide_in_top, R.anim.abc_slide_out_top).hide(this).commit();
        showing = false;
    }

    /**
     * Die Funktion gibt an ob der Fragment zurzeit angezeigt wird
     * @return Die Funktion liefert true zurück wenn der Fragment angezeigt wird
     */
    public boolean isShowing() {
        return showing;
    }
}