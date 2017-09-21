package au.com.bluedot.urbanairshipdemoapp;

import android.*;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Replace with your credentials
    private final String USER_NAME = "adil@bluedotinnovation.com";
    private final String API_KEY = "b2f58bc0-52df-11e6-99f7-06a56cd124c5";
    private final String PACKAGE_NAME = "com.uat3.crowdtestapp";
    private final String URL = "https://uat3.bluedotinnovation.com/pointapi-v1/";
    private static final int PERMISSION_REQUEST_CODE = 101;


    BluedotAdapter bluedotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluedotAdapter= BluedotAdapter.getInstance(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (checkPermission() && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)) {
            bluedotAdapter.startSDK(PACKAGE_NAME,API_KEY,USER_NAME,URL,true);
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Checks for status of required Location permission
     * @return - status of required permission
     */
    private boolean checkPermission() {
        int status_fine = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int status_coarse = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return (status_fine == PackageManager.PERMISSION_GRANTED) && (status_coarse == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Displays user dialog for runtime permission request
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:

                boolean permissionGranted = true;
                for(Integer i : grantResults) {
                    permissionGranted = permissionGranted && (i == PackageManager.PERMISSION_GRANTED);
                }

                if(permissionGranted) {
                    bluedotAdapter.startSDK(PACKAGE_NAME,API_KEY,USER_NAME,URL,true);
                } else {

                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) ) {

                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Information");
                        alertDialog.setMessage(getResources().getString(R.string.permission_needed));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                        alertDialog.setTitle("Information");
                        alertDialog.setMessage(getResources().getString(R.string.location_permissions_mandatory));
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                                    }
                                });
                        alertDialog.show();
                    }


                }
        }
    }

}
