package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter.ListAdapter;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;

/**
 * A simple {@link ListFragment} subclass.
 */
public class ListScrollViewFragment extends ListFragment {
    private static final String PRUEFUNG = "pruefung", ENABLED = "enabled";
    private ListView listView;

    /**
     * Erzeugt eine Neue Instanz der Klasse
     * @param pruefung Die Prüfung das von dem Fragment dargestellt werden soll
     * @param enabled Gibt an ob die Liste vom Benutzer bearbeitet werden soll
     * @return Das neuerzeugte Objekt
     */
    public static ListScrollViewFragment newInstance(Pruefung pruefung, boolean enabled) {
        ListScrollViewFragment fragment = new ListScrollViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRUEFUNG, pruefung);
        args.putBoolean(ENABLED, enabled);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_scroll_view, container, false);
        this.listView = view.findViewById(R.id.listScrollView);
        this.listView.setAdapter(new ListAdapter(getContext(), (Pruefung) getArguments().getParcelable(PRUEFUNG), getArguments().getBoolean(ENABLED)));
        return view;
    }

    /**
     * Markiert alle Ja/Nein Felder in der Liste
     */
    public void checkAll() {
        ((ListAdapter) this.listView.getAdapter()).checkAll();
    }

    @Override
    public ListView getListView() {
        return this.listView;
    }

    /**
     * @return Liefert den Zustand der Liste zurück
     */
    public Parcelable getListState(){
        return getListView().onSaveInstanceState();
    }

    /**
     * @param listState Setzt den Zustand der Liste
     */
    public void setListState(Parcelable listState){
        getListView().onRestoreInstanceState(listState);
    }
}