package au.com.bluedot.urbanairshipdemoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import au.com.bluedot.point.net.engine.ServiceManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override protected void onStart() {
        super.onStart();

        Button init = findViewById(R.id.bInit);
        init.setEnabled(
                !ServiceManager.getInstance(getApplicationContext()).isBluedotServiceInitialized());
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        MainApplication mainApplication = (MainApplication) getApplicationContext();
        switch (ID) {
            case R.id.bInit:
                mainApplication.initPointSDK();
                break;

            case R.id.bReset:
                mainApplication.reset();
                break;

            default:
                break;
        }
    }

}
