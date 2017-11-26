package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter.ListAdapter;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;

public class ListScrollViewFragment extends ListFragment {
    private static final String PRUEFUNG = "pruefung", ENABLED = "enabled";
    private ListView listView;
    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public static ListScrollViewFragment newInstance(Pruefung pruefung, boolean enabled) {
        ListScrollViewFragment fragment = new ListScrollViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRUEFUNG, pruefung);
        args.putBoolean(ENABLED, enabled);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_scroll_view, container, false);
        this.listView = view.findViewById(R.id.listScrollView);
        this.listView.setAdapter(new ListAdapter(getContext(), (Pruefung) getArguments().getParcelable(PRUEFUNG), getArguments().getBoolean(ENABLED)));
        return view;
    }

    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
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

    public void onPause() {
        super.onPause();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void checkAll() {
        ((ListAdapter) this.listView.getAdapter()).checkAll();
    }

    @Override
    public ListView getListView() {
        return this.listView;
    }

    public Parcelable getListState(){
        return getListView().onSaveInstanceState();
    }

    public void setListState(Parcelable listState){
        getListView().onRestoreInstanceState(listState);
    }
}