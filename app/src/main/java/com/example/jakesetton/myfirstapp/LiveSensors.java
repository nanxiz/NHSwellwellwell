package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.everything.providers.android.browser.BrowserProvider;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.telephony.TelephonyProvider;

public class LiveSensors extends AppCompatActivity {
    private static final int STORAGE_CODE = 1000;
    private ActivityManager mActivityManager;

    //why the fk are these static???
    public static TextView stepview;
    public static TextView messageview;
    public static TextView callminuteview;
    public static TextView searchview;
    public static TextView callview;
    public static TextView socialview;

    public static int stepstaken;
    String stepvalue;

    String messagecount;
    String callscount;


    DatabaseHelper myDb;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    TelephonyProvider telephonyProvider;
    BrowserProvider browserProvider;
    CallsProvider callsProvider;


    Button createPdf;

    static int prevweekstepcount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Some live statistics");
        super.onCreate(savedInstanceState);
        preferences = this.getSharedPreferences("MyPreferences", this.MODE_PRIVATE);
        editor = preferences.edit();
        myDb = new DatabaseHelper(this);
        setContentView(R.layout.activity_live_sensors);
        stepview = (TextView) findViewById(R.id.steptext);
        messageview = (TextView) findViewById(R.id.msgcount);
        callview = (TextView) findViewById(R.id.callcount);
        socialview = (TextView) findViewById(R.id.socialView);
        callminuteview = (TextView) findViewById(R.id.minuteView);




        Intent intent = getIntent();

        //stepvalue: steps taken
        stepvalue = intent.getStringExtra(MainActivity.UPDATE_TEXT);
        int totalmsgcount = preferences.getInt("messagecount", 0);
        int totalcallcount = preferences.getInt("callcount", 0);
        telephonyProvider = new TelephonyProvider(this);
        browserProvider = new BrowserProvider(this);
        callsProvider = new CallsProvider(this);
        int smses = telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList().size();
        int mmses = telephonyProvider.getMms(TelephonyProvider.Filter.SENT).getList().size();

        int calls = callsProvider.getCalls().getList().size();

        //messagecount: message sent
        messagecount = String.valueOf(smses + mmses - totalmsgcount);
        
        //callscount: and you made xx calls
        callscount = String.valueOf(calls - totalcallcount);

        if (Integer.parseInt(messagecount) < 0) {
            messagecount += " (did you delete some messages recently?)";
        }
        if (Integer.parseInt(callscount) < 0) {
            callscount += " (did you delete some calls recently?)";
        }

        if (!myDb.isdbempty()) {
            Cursor res = myDb.getLastLine();
            res.moveToFirst();
            prevweekstepcount = res.getInt(0);
            Log.v("prevweekstepcount:", String.valueOf(prevweekstepcount));
        }
        Log.v("Step b4 subtraction", stepvalue);

        stepvalue = String.valueOf(Integer.parseInt(stepvalue) - prevweekstepcount);

        Log.v("Step after subtraction", stepvalue);

        stepview.setText(stepvalue + " steps so far");
        messageview.setText(messagecount + " texts");
        callview.setText(callscount + " calls");

        // for the current week's usage stats, get stats from between the current date and when the last alarm went off
        final UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);// Context.USAGE_STATS_SERVICE);

        long startedtime = preferences.getLong("alarmtime", 0);
        Date intervaldate = new Date(startedtime);


        Calendar endCal = Calendar.getInstance();
        Calendar beginCal = Calendar.getInstance();
        beginCal.setTime(intervaldate);
        beginCal.add((Calendar.MINUTE), -60*24*7); //sets the stats gathering period to start from when the last score was predicted
        Log.v("start cal is now ", String.valueOf(new Date(beginCal.getTimeInMillis())));

        float socialapptime = 0; //seconds
        float calltime = 0; //seconds

        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        for (UsageStats app : queryUsageStats) {
            if (app.getPackageName() == null || app.getTotalTimeInForeground() == 0) {
                continue;
            }
            if (app.getPackageName().contains("messaging") || app.getPackageName().contains("nstagram") || app.getPackageName().contains(".orca") || app.getPackageName().contains("snapchat") || app.getPackageName().contains("whatsapp")) {
                socialapptime += ((float) (app.getTotalTimeInForeground() / 1000));
            }

            if (app.getPackageName().contains("dialer") || app.getPackageName().contains("phone")) {
                calltime += ((float) (app.getTotalTimeInForeground() / 1000));
            }
        }




        //
        //
        //
        //
        float callMinutes; //youve chatted xx minutes on the phone
        float socialMinutes;//youve spend xx minutes on social media
        //
        callMinutes = calltime/60;
        socialMinutes = socialapptime/60;
        //
        //





        callminuteview.setText(String.format("%.2f", (callMinutes)) + " minutes on the phone");
        socialview.setText(String.format("%.2f", (socialMinutes)) + " minutes on social media");

    }





    public void viewMain(View view) { //takes the user back to the homepage
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void viewHistory(View view) { //takes the user to the score history page
        MainActivity main = new MainActivity();
        if (myDb.isdbempty()) {
            Toast.makeText(LiveSensors.this, "No scores logged yet! Scores will be available from the end of the first week.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, ScoreHistory.class);
        startActivity(intent);
    }
}
