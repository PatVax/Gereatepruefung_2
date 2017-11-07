package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 * Created by Patryk on 07.11.2017.
 */

public class ListAdapter extends ArrayAdapter<String> {
    private Context context;
    private ContentValues currentViewState = new ContentValues();
    private ArrayList<ContentValues> valuesArray;

    public ListAdapter(Context context, ArrayList<ContentValues> valuesArray) {
        super(context, -1);
        this.context = context;
        this.valuesArray = valuesArray;
        for (int i = 0; i < valuesArray.size(); i++) {
            String asString = ((ContentValues) this.valuesArray.get(i)).getAsString("Anzeigeart");
            switch (this.valuesArray.get(i).getAsString("anzeigeart")) {
                case "b":
                    this.currentViewState.put("" + i, Boolean.valueOf(false));
                    break;
                default:
                    this.currentViewState.put("" + i, "");
                    break;
            }
        }
    }

    public ListAdapter(Context context, ArrayList<ContentValues> valuesArray, ContentValues viewState) {
        super(context, -1);
        this.context = context;
        this.valuesArray = valuesArray;
        this.currentViewState = viewState;
    }

    @NonNull
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String asString = ((ContentValues) this.valuesArray.get(position)).getAsString("Anzeigeart");
        boolean z = true;
        switch (asString.hashCode()) {
            case 98:
                if (asString.equals("b")) {
                    z = true;
                    break;
                }
                break;
            case 104:
                if (asString.equals("h")) {
                    z = false;
                    break;
                }
                break;
        }
        switch (this.valuesArray.get(position).getAsString("anzeigeart")) {
            case "h":
                convertView = inflater.inflate(R.layout.list_item_info, null);
                ((TextView) convertView.findViewById(R.id.infoTextView)).setText(((ContentValues) this.valuesArray.get(position)).getAsString("Text"));
                return convertView;
            case "b":
                convertView = inflater.inflate(R.layout.list_item_bool, null);
                CheckBox boolCheckBox = (CheckBox) convertView.findViewById(R.id.boolCheckBox);
                ((TextView) convertView.findViewById(R.id.boolTextView)).setText(((ContentValues) this.valuesArray.get(position)).getAsString("Text"));
                if (this.currentViewState.containsKey(position + "")) {
                    boolCheckBox.setChecked(((Boolean) this.currentViewState.get(position + "")).booleanValue());
                } else {
                    this.currentViewState.put(position + "", Boolean.valueOf(false));
                }
                boolCheckBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        boolean lastCheckState = ((Boolean) ListAdapter.this.currentViewState.get(position + "")).booleanValue();
                        ListAdapter.this.currentViewState.remove(position + "");
                        ListAdapter.this.currentViewState.put(position + "", Boolean.valueOf(!lastCheckState));
                    }
                });
                return convertView;
            default:
                convertView = inflater.inflate(R.layout.list_item_wert, null);
                final EditText wertEditText = (EditText) convertView.findViewById(R.id.wertEditText);
                ((TextView) convertView.findViewById(R.id.wertTextView)).setText(((ContentValues) this.valuesArray.get(position)).getAsString("Text"));
                wertEditText.setHint(((ContentValues) this.valuesArray.get(position)).getAsString("Anzeigeart"));
                if (this.currentViewState.containsKey(position + "")) {
                    wertEditText.setText((String) this.currentViewState.get(position + ""));
                } else {
                    this.currentViewState.put(position + "", "");
                }
                wertEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            ListAdapter.this.currentViewState.remove(position + "");
                            ListAdapter.this.currentViewState.put(position + "", wertEditText.getText().toString());
                        }
                    }
                });
                return convertView;
        }
    }

    public void checkAll() {
        for (Entry<String, Object> entry : this.currentViewState.valueSet()) {
            if (entry.getValue() instanceof Boolean) {
                this.currentViewState.put((String) entry.getKey(), Boolean.valueOf(true));
            }
        }
        notifyDataSetChanged();
    }

    public ArrayList<ContentValues> getValuesArray() {
        return this.valuesArray;
    }

    public ContentValues getCurrentViewState() {
        return this.currentViewState;
    }

    public int getCount() {
        return this.valuesArray.size();
    }
}
