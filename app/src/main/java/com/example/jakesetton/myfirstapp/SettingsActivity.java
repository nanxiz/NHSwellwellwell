package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_settings);



    }
    //IDEA ABOUT THE LEARN MORE IN SETTINGS
    //MAKE THAT SLIDERS NOT INCLUDE PRIVACY AND MAKE PRIVACY A ACTIVITY
    //REUSE THE SLIDERS FOR LEARN MORE

    public void viewMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void viewProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    public void viewShare(View view) {
        Intent intent = new Intent(this, ShareActivity.class);
        startActivity(intent);
    }
    public void viewSetting(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void Logoff(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void infoscreen(View view){
        Intent intent = new Intent(this, MonitoringInfo.class);
        //add bundle later
        startActivity(intent);
    }

    public void EditGuardian(View view){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String message = "implementing profile";
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();

    }

    public void launchBrowser(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.nhs.uk/conditions/stress-anxiety-depression/improve-mental-wellbeing/"));
        startActivity(browserIntent);
    }

    public void statslaunch(View view) {

        int hasSMSPermission = checkSelfPermission(Manifest.permission.READ_SMS);
        //int hasBrowserPermission = checkSelfPermission(Manifest.permission.);
        int hasCallLogPermission = checkSelfPermission(Manifest.permission.READ_CALL_LOG);
        int hasSMSReceivePermission = checkSelfPermission(Manifest.permission.RECEIVE_SMS);
        int hasUsageStatsPermission = checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS);
        int hasFilesPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasSMSPermission != PackageManager.PERMISSION_GRANTED && hasCallLogPermission != PackageManager.PERMISSION_GRANTED && hasSMSReceivePermission != PackageManager.PERMISSION_GRANTED && hasUsageStatsPermission != PackageManager.PERMISSION_GRANTED && hasFilesPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[] {Manifest.permission.READ_SMS, Manifest.permission.READ_CALL_LOG, Manifest.permission.RECEIVE_SMS, Manifest.permission.PACKAGE_USAGE_STATS, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);

        }

        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);// Context.USAGE_STATS_SERVICE);
        Calendar beginCal = Calendar.getInstance();
        beginCal.set((Calendar.WEEK_OF_MONTH), (Calendar.WEEK_OF_MONTH - 2));

        Calendar endCal = Calendar.getInstance();

        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        Log.v("Results:","results for " + beginCal.getTime().toString() + " - " + endCal.getTime().toString());
        if (queryUsageStats.size() == 0) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

}
