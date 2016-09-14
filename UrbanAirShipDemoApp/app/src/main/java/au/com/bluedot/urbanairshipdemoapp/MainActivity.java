package au.com.bluedot.urbanairshipdemoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Replace with your credentials
    private final String USER_NAME = "adil@bluedotinnovation.com";
    private final String API_KEY = "b2f58bc0-52df-11e6-99f7-06a56cd124c5";
    private final String PACKAGE_NAME = "com.uat3.crowdtestapp";
    private final String URL = "https://uat3.bluedotinnovation.com/pointapi-v1/";




    BluedotAdapter bluedotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluedotAdapter= BluedotAdapter.getInstance(this);
        bluedotAdapter.startSDK(PACKAGE_NAME,API_KEY,USER_NAME,URL,true);

    }

}
