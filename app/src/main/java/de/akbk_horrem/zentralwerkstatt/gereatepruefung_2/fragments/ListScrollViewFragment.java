package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.fragments;

import android.content.ContentValues;
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
import java.util.ArrayList;

public class ListScrollViewFragment extends ListFragment {
    private static final String ARG_PARAMS = "list";
    private ListView listView;
    private OnFragmentInteractionListener mListener;
    private ArrayList<ContentValues> valuesArray;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public static ListScrollViewFragment newInstance(ArrayList<Parcelable> params) {
        ListScrollViewFragment fragment = new ListScrollViewFragment();
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
        View view = inflater.inflate(R.layout.fragment_list_scroll_view, container, false);
        this.listView = (ListView) view.findViewById(R.id.listScrollView);
        this.listView.setAdapter(new ListAdapter(getContext(), this.valuesArray));
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

    public ListView getListView() {
        return this.listView;
    }
}
