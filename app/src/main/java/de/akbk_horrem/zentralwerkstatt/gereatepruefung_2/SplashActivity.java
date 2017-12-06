package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.net.MalformedURLException;
import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            //SharedPreferences
            SharedPreferences prefs = getSharedPreferences(SharedPreferenceEnum.SHARED_PREFERENCE.getText(), MODE_PRIVATE);

            //Wenn Offline Modus an ist
            if(!prefs.getBoolean(SharedPreferenceEnum.OFFLINE_MODE.getText(), false)) {

                //SplashActivity für die innere Klasse
                final Activity activity = this;

                //Datenbank Verbindungsversuch
                DBAsyncTask.getInstance(this, new DBAsyncTask.DBAsyncResponse() {
                    public void processFinish(final ArrayList<ContentValues> resultArrayList) {

                        //Wenn erfolgreich
                        if ((resultArrayList.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText()))) {

                            //MainActivity starten mit Information über Verbindungsaufbau
                            startActivity(new Intent(activity, MainActivity.class).putExtra("connection", true));
                            finish();

                        //Sonst
                        } else {

                            //MainActivity starten mit Information über Verbindungsaufbau
                            startActivity(new Intent(activity, MainActivity.class).putExtra("connection", false));
                            finish();
                        }
                    }
                }, false).execute(AsyncTaskOperationEnum.CHECK_CONNECTION, false);
            }
            //Sonst
            else {

                //MainActivity starten
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }

        //Wenn zusammengestelltes Verbindungs-Url kein gültiges Format hat
        } catch (MalformedURLException e) {

            //MainActivity starten
            startActivity(new Intent(this, MainActivity.class).putExtra("connection", false));
            finish();
        }
    }
}
