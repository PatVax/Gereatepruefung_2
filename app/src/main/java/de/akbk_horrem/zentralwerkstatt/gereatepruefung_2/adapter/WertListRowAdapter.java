package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by Patryk on 07.11.2017.
 */

public class WertListRowAdapter extends ArrayAdapter {
    private Context context;

    public WertListRowAdapter(Context context) {
        super(context, -1);
        this.context = context;
    }

    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return super.getView(position, convertView, parent);
    }
}
