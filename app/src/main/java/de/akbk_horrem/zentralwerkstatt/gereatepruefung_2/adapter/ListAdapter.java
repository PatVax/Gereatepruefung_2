package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.R;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.Pruefung;

/**
 * Created by Patryk on 07.11.2017.
 */

public class ListAdapter extends ArrayAdapter<String> {
    private Context context;
    Pruefung pruefung;
    boolean enabled;

    public ListAdapter(Context context, Pruefung pruefung, boolean enabled) {
        super(context, -1);
        this.context = context;
        this.pruefung = pruefung;
        this.enabled = enabled;
    }

    @NonNull
    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (this.pruefung.getKriterien().get(position).getAsString("anzeigeart")) {
            case "h":
                convertView = inflater.inflate(R.layout.list_item_info, null);
                ((TextView) convertView.findViewById(R.id.infoTextView)).setText(( this.pruefung.getKriterien().get(position)).getAsString("text"));
                return convertView;
            case "b":
                convertView = inflater.inflate(R.layout.list_item_bool, null);
                CheckBox boolCheckBox = convertView.findViewById(R.id.boolCheckBox);
                ((TextView) convertView.findViewById(R.id.boolTextView)).setText(( this.pruefung.getKriterien().get(position)).getAsString("text"));
                boolCheckBox.setChecked(( this.pruefung.getValues().get(position).getAsBoolean("messwert")));
                boolCheckBox.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        ListAdapter.this.pruefung.getValues().get(position).put("messwert", !ListAdapter.this.pruefung.getValues().get(position).getAsBoolean("messwert"));
                    }
                });
                boolCheckBox.setEnabled(this.enabled);
                return convertView;
            default:
                convertView = inflater.inflate(R.layout.list_item_wert, null);
                final EditText wertEditText = convertView.findViewById(R.id.wertEditText);
                ((TextView) convertView.findViewById(R.id.wertTextView)).setText(this.pruefung.getKriterien().get(position).getAsString("text"));
                wertEditText.setHint(( this.pruefung.getKriterien().get(position)).getAsString("anzeigeart"));
                wertEditText.setText(this.pruefung.getValues().get(position).getAsString("messwert"));
                wertEditText.setImeOptions(pruefung.getKriterien().get(position + 1).getAsString("anzeigeart").equals("b") || pruefung.getKriterien().get(position + 1).getAsString("anzeigeart").equals("h") ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
                wertEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ListAdapter.this.pruefung.getValues().get(position).put("messwert", wertEditText.getText().toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                wertEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
                        ListView lv = (ListView) parent;
                        if (lv != null &&
                                position >= lv.getLastVisiblePosition() &&
                                position != pruefung.getValues().size() - 1) {
                            lv.smoothScrollToPosition(position + 1);
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                lv.postDelayed(new Runnable() {
                                    public void run() {
                                        TextView nextField = (TextView) v.focusSearch(View.FOCUS_DOWN);
                                        if (nextField != null) {
                                            nextField.requestFocus();
                                        }
                                    }
                                }, 200);
                                return true;
                            }
                        }
                        return false;
                    }
                });
                wertEditText.setEnabled(enabled);
                return convertView;
        }
    }

    public void checkAll() {
        for (int i = 0; i < pruefung.getCount(); i++)
            if(this.pruefung.getKriterien().get(i).getAsString("anzeigeart").equals("b"))
                this.pruefung.getValues().get(i).put("messwert", true);
        notifyDataSetChanged();
    }

    public Pruefung getPruefung() {
        return pruefung;
    }


    public int getCount() {
        return this.pruefung.getCount();
    }
}
