package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces;

import android.content.ContentValues;
import java.util.ArrayList;

public interface DBAsyncResponse {
    void processFinish(ArrayList<ContentValues> arrayList);
}
