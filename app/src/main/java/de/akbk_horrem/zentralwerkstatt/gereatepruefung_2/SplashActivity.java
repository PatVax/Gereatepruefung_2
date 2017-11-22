package de.akbk_horrem.zentralwerkstatt.gereatepruefung_2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.util.ArrayList;

import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.dbUtils.mainDB.DBAsyncTask;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.AsyncTaskOperationEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.DBConnectionStatusEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.enums.SharedPreferenceEnum;
import de.akbk_horrem.zentralwerkstatt.gereatepruefung_2.interfaces.DBAsyncResponse;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = this;
        try {
            DBAsyncTask.getInstance(this, new DBAsyncResponse() {
                public void processFinish(ArrayList<ContentValues> resultArray) {
                    if ((resultArray.get(0).getAsString(DBConnectionStatusEnum.CONNECTION_STATUS.getText()).equals(DBConnectionStatusEnum.CONNECTED.getText()))) {
                        startActivity(new Intent(activity, MainActivity.class).putExtra("connection", true));
                        finish();
                        return;
                    }else{


                        startActivity(new Intent(activity, MainActivity.class).putExtra("connection", false));
                        finish();
                    }
                }
            }).execute(AsyncTaskOperationEnum.CHECK_CONNECTION, false);
        } catch (MalformedURLException e) {
            startActivity(new Intent(activity, MainActivity.class).putExtra("connection", false));
            finish();
        }
    }
}
