package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.BroadcastReceiver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import android.widget.Toast;


import android.support.v7.widget.Toolbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import me.everything.providers.android.browser.BrowserProvider;
import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.telephony.Mms;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    public static Button viewstats;
    static Button startServiceBtn;
    static Button stopServiceBtn;
    static Button startServiceBtn2;
    private Toolbar toolbar;

    boolean serviceOn;

    TelephonyProvider telephonyProvider;
    BrowserProvider browserProvider;
    CallsProvider callsProvider;

    //Accessing SMS and MMS sent text (for "Connect")
    private List<Sms> smses;
    private List<Mms> mmses;

    public String smsString;
    public String mmsString;

    public int oldsmssize;
    public int oldmmssize;
    int zero;

    //Accessing call logs (for "Give", "Connect")
    private Data<Call> calls;

    //Phone activity info
    static String countedSteps;
    String detectedSteps;
    String messageContent;
    int countedMessages;
    int countedCalls;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    public static int old;

    String alarmcheck;


    private static final String TAG = "SensorEvent";
    //Strings for the service
    public static final String UPDATE_TEXT = "com.example.jakesetton.myfirstapp.MESSAGE";

    DatabaseHelper myDb;

    private TensorFlowClassifier classifier;
    private float[] results;
    float[] dummy;
    public static TextView scoreView;
    public static TextView denominator;
    int weeklyscore;

    private static String choice;
    Switch privacySwitch;
    static String privacyStatus;

    static Button moreinfo;
    static TextView scoreMsg;
    static ImageView waysinfo;

    static List<JavaClassifier.Instance> instances;
    static double[] x;
    static int score;

    static double[] mean;
    static double[] stdev;

    static ProgressBar progress;
    static ProgressBar progress2;

    static View circle;

    Button createPdf;
    Dialog epicDialog;
    TextView titleTv,thefilename;
    ImageView closePopup, pdfimg;

    Button btnShare;

    private static final int STORAGE_CODE = 1000;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewstats = (Button) findViewById(R.id.stats);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress2 = (ProgressBar) findViewById(R.id.progBackground);
        waysinfo = (ImageView) findViewById(R.id.waysinfo);
        progress.setRotation(270);
        progress2.setRotation(270);
        scoreMsg = (TextView) findViewById(R.id.scoremsg);
        circle = (View) findViewById(R.id.view);
        privacySwitch = (Switch) findViewById(R.id.switch1);
        moreinfo = (Button) findViewById(R.id.moreinfo);
        //moreinfo.setPaintFlags(moreinfo.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
        myDb = new DatabaseHelper(this);
        //classifier = new TensorFlowClassifier(this);




        createPdf = findViewById(R.id.createPdf);

        toolbar = findViewById(R.id.toobar);
        setSupportActionBar(toolbar);

        ////2
       // TemplatePDF templatePDF = new TemplatePDF(getApplicationContext());




        //垃圾制造地
        preferences = this.getSharedPreferences("MyPreferences", this.MODE_PRIVATE);
        editor = preferences.edit();

        //a check to see if the app has already been set up (i.e. if Usage Stats have been enabled) - if not, launch the welcome page
        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);// Context.USAGE_STATS_SERVICE);
        Calendar beginCal = Calendar.getInstance();
        beginCal.add((Calendar.MINUTE), -60*24*7);
        Calendar endCal = Calendar.getInstance();
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());

        int hasSMSPermission = checkSelfPermission(Manifest.permission.READ_SMS);
        int hasCallLogPermission = checkSelfPermission(Manifest.permission.READ_CALL_LOG);
        int hasSMSReceivePermission = checkSelfPermission(Manifest.permission.RECEIVE_SMS);
        int hasUsageStatsPermission = checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS);
        int hasFilesPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasSMSPermission != PackageManager.PERMISSION_GRANTED && hasCallLogPermission != PackageManager.PERMISSION_GRANTED && hasSMSReceivePermission != PackageManager.PERMISSION_GRANTED && hasUsageStatsPermission != PackageManager.PERMISSION_GRANTED && hasFilesPermission != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, AboutPage.class);
            startActivity(intent);
        }
        if (queryUsageStats.size() == 0) {
            Intent intent = new Intent(this, AboutPage.class);
            startActivity(intent);
        }

        Log.d("Calling onCreate main: ", String.valueOf(System.currentTimeMillis()));

        privacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    editor.putString("privacystatus", "on");
                    editor.apply();
                }
                else {
                    editor.putString("privacystatus", "off");
                    editor.apply();
                }
            }
        });
        setup();







        createPdf = findViewById(R.id.createPdf);

        createPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //BASICALLY CHECKING FOR PERMISSION ISSUED OR NOT DUE TO VARIOUS OS SYSTEMS
                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                        //no permission granted so need to request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permissions, STORAGE_CODE);
                    }
                    else{
                        //permission granted
                        makePdf();

                    }

                }
                else{
                    makePdf();
                }
            }
        });


        epicDialog=new Dialog(this);


       // insertDummyData();

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

    public void viewlive(View view) {
        if (countedSteps == null) {
            //Toast.makeText(MainActivity.this, "Start monitoring in order to view stats!", Toast.LENGTH_LONG).show();

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            String message ="Start monitoring in order to view Activity!";
            dialog.setMessage(message);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
            return;
        }
        Intent intent = new Intent(this, LiveSensors.class);
        intent.putExtra(UPDATE_TEXT, countedSteps);
        Log.d("Counted steps: ", countedSteps);

        if (isMyServiceRunning(TheService.class) == true) {
            old = preferences.getInt("oldstep", 0);
            Log.d("Old value is now:", String.valueOf(old));
        }
        else if (isMyServiceRunning(TheService.class) == false) {
            Toast.makeText(MainActivity.this, "Start monitoring in order to view stats!", Toast.LENGTH_LONG).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            String message ="Start monitoring in order to view Activity!";
            dialog.setMessage(message);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
            return;
        }
        startActivity(intent);
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        scoreView = (TextView) findViewById(R.id.score);
        denominator = (TextView) findViewById(R.id.denominator);
        alarmcheck = preferences.getString("alarmstatus", "");
        if (alarmcheck.equals("on") && isMyServiceRunning(TheService.class)) {
            stopServiceBtn.setVisibility(View.VISIBLE);
            startServiceBtn.setVisibility(View.INVISIBLE);
            scoreMsg.setVisibility(View.VISIBLE);
            waysinfo.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
            if (myDb.isdbempty()) {
                scoreView.setVisibility(View.VISIBLE);
                circle.setVisibility(View.VISIBLE);
            }
        }
        Intent fromalarm = getIntent();
        if (fromalarm.getStringExtra("origin") != null) {
            if (fromalarm.getStringExtra("origin").equals("alarm")) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("On feedback called:", "now");
                            showFeedBack();
                    }
                }, 100);
            }
            fromalarm.removeExtra("origin");
        }
        if (fromalarm.getStringExtra("history") != null) { //code to jump to live week page from history
            if (fromalarm.getStringExtra("history").equals("liveweek")) {
                viewstats.performClick();
            }
        }
        Log.v("onResume:", "executed");

        //Send people to the first page of the app if they haven't granted permissions yet
        Calendar beginCal = Calendar.getInstance(); //is there a better way of doing this than checking for usage stats?
        beginCal.add((Calendar.MINUTE), -60*24*7);
        Calendar endCal = Calendar.getInstance();
        final UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        Intent intent;
        if (queryUsageStats.size() == 0) {
            intent = new Intent(this, AboutPage.class);
            startActivity(intent);
        }

        String latestscore = preferences.getString("score", "");
        Log.v("Latest score in S.Pref:", latestscore);
        if (!myDb.isdbempty() && !latestscore.equals("-1")) { //if there's a score in the db
                int scoreint = Integer.parseInt(latestscore);
                circle.setVisibility(View.INVISIBLE);
                //scoreint = 10;
                scoreView.setVisibility(View.VISIBLE);
                denominator.setVisibility(View.VISIBLE);
                moreinfo.setVisibility(View.VISIBLE);
                scoreMsg.setVisibility(View.VISIBLE);
                scoreView.setTextSize(145);
                waysinfo.setVisibility(View.VISIBLE);
                setProgressColours(scoreint);
            if (isMyServiceRunning(TheService.class)) {
                startServiceBtn.setVisibility(View.INVISIBLE);
                stopServiceBtn.setVisibility(View.VISIBLE);
            }
            else {
                stopServiceBtn.setVisibility(View.INVISIBLE);
                startServiceBtn2.setVisibility(View.VISIBLE);
                startServiceBtn.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void setup() {
        privacyStatus = preferences.getString("privacystatus", "");
        if (privacyStatus.equals("on")) {
            privacySwitch.setChecked(true);
        }
        startServiceBtn = (Button) findViewById(R.id.startmonitoring);
        startServiceBtn2 = (Button) findViewById(R.id.startmonitoring2);
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //starts the pedometer counting service
                editor.putInt("oldstep", zero);
                editor.apply();
                startStepCount(); //the method that starts the Service class
                insertData(view); //the method that establishes weekly data logging and classifications
                try{insertDummyData(view);}
                catch (FileNotFoundException e){

                }

            }
        });
        startServiceBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //starts the pedometer counting service
                String latest = preferences.getString("score", "");
                startStepCount(); //the method that starts the Service class
                insertData(view); //the method that establishes weekly data logging and classifications
                startServiceBtn2.setVisibility(View.INVISIBLE);
                circle.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                progress2.setVisibility(View.VISIBLE);
                scoreView.setVisibility(View.VISIBLE);
                editor.putString("score", latest);
                editor.apply();

            }
        });

        stopServiceBtn = (Button) findViewById(R.id.stopmonitoring);
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //if the user clicks 'stop monitoring'
                if (serviceOn) {
                    unregisterReceiver(broadcastReceiver);
                    stopService(new Intent(getBaseContext(), TheService.class));
                    editor.putString("alarmstatus", "off");
                    editor.apply();
                    serviceOn = false;
                    stopServiceBtn.setVisibility(View.INVISIBLE);
                    startServiceBtn2.setVisibility(View.VISIBLE);
                    if (myDb.isdbempty()) {
                        scoreMsg.setVisibility(View.INVISIBLE);
                        circle.setVisibility(View.VISIBLE);
                        denominator.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.INVISIBLE);
                        progress2.setVisibility(View.INVISIBLE);
                        scoreView.setVisibility(View.INVISIBLE);
                        startServiceBtn2.setVisibility(View.INVISIBLE);
                        startServiceBtn.setVisibility(View.VISIBLE);
                        waysinfo.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        if (isMyServiceRunning(TheService.class) == true) {
            startStepCount();
        }

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) { //checks whether the pedometer is working
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startStepCount(){
        Intent intent = new Intent(MainActivity.this,TheService.class);
        ContextCompat.startForegroundService(this, intent);
        registerReceiver(broadcastReceiver, new IntentFilter(TheService.BROADCAST_ACTION));
        serviceOn = true;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // call updateUI passing in our intent which is holding the data to display.
            updateViews(intent);
        }
    };

    private void updateViews(Intent intent) { //the pedometer service updates the static variable countedsteps
        if (intent.getStringExtra("Counted_Step") != null) {
            countedSteps = intent.getStringExtra("Counted_Step");
        }
        else {
            Log.v("counted int value", "null");
        }
        detectedSteps = intent.getStringExtra("Detected_Step");
        Log.d(TAG, String.valueOf(countedSteps));
    }


    public void insertData(View view) {
        //insertDummyData(view);
        //Start by converting training set .csv file into String, to be written as new file into internal storage
        //internal storage is not read-only, whereas Assets folder is, so cannot update file with new data otherwise
        String data = loadAssetTextAsString(this, "welldata_java_vsn.csv");
        writeStringAsFile(data, "welldata_java_vsn.csv");
        editor.putString("alarmstatus", "on");
        editor.apply();
        editor.putString("score", "-1");
        editor.apply();
        circle.setVisibility(View.VISIBLE);
        scoreMsg.setVisibility(View.VISIBLE);
        scoreView.setVisibility(View.VISIBLE);
        waysinfo.setVisibility(View.VISIBLE);

        telephonyProvider = new TelephonyProvider(this); //when the alarm is turned on, store the current total number of outgoing calls and texts
        browserProvider = new BrowserProvider(this);
        callsProvider = new CallsProvider(this);
        smses = telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList();
        mmses = telephonyProvider.getMms(TelephonyProvider.Filter.SENT).getList();
        messageContent = smses.toString();
        countedMessages = smses.size() + mmses.size();
        calls = callsProvider.getCalls();
        countedCalls = calls.getList().size();
        editor.putInt("callcount", countedCalls);
        editor.putInt("messagecount", countedMessages);
        editor.apply(); //(this is for comparison when the data is stored at the end of the first week)

        stopServiceBtn.setVisibility(View.VISIBLE);
        startServiceBtn.setVisibility(View.INVISIBLE);
        Log.v("alarmstatus is ", preferences.getString("alarmstatus", ""));
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long time= System.currentTimeMillis();
        Date d = new Date(time);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.MINUTE, 60*24*7);
        time = c.getTimeInMillis();
        editor.putLong("alarmtime", time);
        editor.apply();
        Log.v("alarmtime is now: ",String.valueOf(new Date(preferences.getLong("alarmtime", 0))));
        Intent intent = new Intent(this, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC, time,1000*60*60*24*7, pendingIntent);
        Toast.makeText(MainActivity.this, "Tracking Started!", Toast.LENGTH_LONG).show();

    }

    /*public void classify(){     // commented out as the Java Classifier is currently in use instead
        Cursor res = myDb.getLastLine();
        if (res.getCount() == 0) {
            Log.d("No data detected!", "cool");
            return;
        }
        if (myDb.isdbempty()) {
            Log.d("No data detected!", "great");
            return;
        }
        while (res.moveToNext()) { //might have to adjust
            weeklyscore = res.getInt(8);
        }

        scoreView = (TextView) findViewById(R.id.score);
        denominator = (TextView) findViewById(R.id.denominator);
        dummy =  new float[] {(float) 180000, (float) 11, (float) 2400, (float) 100, (float) 2, (float) 70, (float) 24000, (float) 1000};
        results = classifier.predictWelfare(JavaClassifier.inputstandardise(dummy));
        String s = "";
        float highest = -1;
        int score = 0;
        for (int i=0; i<results.length; i++) {
            if (results[i] > highest) {
                highest = results[i];
                score = i;
            }
        }
        Log.v("results are":, results);
        denominator.setVisibility(View.VISIBLE);
        moreinfo.setVisibility(View.VISIBLE);
        scoreView.setTextSize(96);
        if (score > 6) {
            scoreView.setTextColor(getResources().getColor(R.color.Black));
        }
        else if (4 < score && score < 7) {
            scoreView.setTextColor(getResources().getColor(R.color.colorBlue));
        }
        else {
            scoreView.setTextColor(getResources().getColor(R.color.colorDarkRed));
        }
        scoreView.setText(String.valueOf(weeklyscore));
    }*/


    @Override
    protected void onDestroy() {
        if (isMyServiceRunning(TheService.class) == true) {
            if (countedSteps != null) {
                old = preferences.getInt("oldstep", 0);
                int newcount = Integer.parseInt(countedSteps);
                Log.v("int new count = ", String.valueOf(newcount));
                Long testtime = preferences.getLong("alarmtime", 0);
                Log.v("current long:", String.valueOf(testtime));  //will need to delete this towards the end of the project
                editor.putInt("oldstep", newcount);
                editor.commit();
            }
        }
        super.onDestroy();
    }

    public void showFeedBack() { //this will be automated when the alarm goes off
        final TextView textViewtmp = (TextView) findViewById(R.id.text);
        Cursor res = myDb.getLastLine();
        while (res.moveToNext()) {

            AlertDialog.Builder build = new AlertDialog.Builder(this);
            Log.v("res 0:", String.valueOf(res.getInt(0)));
            final int prediction = res.getInt(8);
            build.setTitle("Your score for this week is " + String.valueOf(prediction) + ". How accurate do you feel this is?");
            final String[] choices = {"Far too high", "Too high", "Slightly high", "Accurate", "Slightly low", "Too low", "Far too low"};
            choice = choices[0];        // to deal with the fact that even though the default display shows "very accurate" being selected, it is in fact null at this stage until an actual click
            build.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    choice = choices[i];
                }
            });
            editor.putString("score", String.valueOf(prediction));
            editor.apply();
            build.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Cursor res2 = myDb.getDataToClassify();
                    while (res2.moveToNext()) {
                        String data = String.valueOf(res2.getInt(0)) + "," + String.valueOf(res2.getInt(1)) + "," + String.valueOf(res2.getFloat(2)) + "," + String.valueOf(res2.getInt(3)) + "," + String.valueOf(res2.getInt(4)) + "," + String.valueOf(res2.getFloat(5)) + "," + String.valueOf(res2.getFloat(6)) + "," + String.valueOf(res2.getFloat(7));
                        try {
                            Log.v("Writing this: ", data + " " + "to text file");
                            addnewinfo(choice, prediction, data);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Unable to record feedback", Toast.LENGTH_LONG).show();
                            Log.v("newFeedback:", "not written to file");
                        }
                    }
                    scoreView = (TextView) findViewById(R.id.score);
                    scoreView.setText(String.valueOf(prediction));
                    denominator = (TextView) findViewById(R.id.denominator);
                    denominator.setVisibility(View.VISIBLE);
                    moreinfo.setVisibility(View.VISIBLE);
                    scoreView.setTextSize(145);
                    circle.setVisibility(View.INVISIBLE);
                    setProgressColours(prediction);
                    waysinfo.setVisibility(View.VISIBLE);
                    scoreMsg.setVisibility(View.VISIBLE);

                    if (privacySwitch.isChecked()) {
                        String filteredresponse = dfFilter(choices, choice);
                        //textViewtmp.setText("You chose " + filteredresponse);
                        myDb.insertFeedback(newscore, filteredresponse);
                    } else {
                        //textViewtmp.setText("You chose " + choice);
                        myDb.insertFeedback(score, choice);
                    }

                }
            });
            build.show();
        }
    }

    public String dfFilter(String[] choices, String realfeedback) {
        Random random = new Random();
        int index = random.nextInt(4);
        if (seventythirty().equals("True")) {
            return realfeedback;
        }
        else {
            return choices[index];
        }
    }

    public static String seventythirty() {  //for when you want the user's true feedback c.70% of the time
        Random random = new Random();
        String[] array = new String[10]; // set up String array of 7 "True"s and 3 "False"s
        for (int i = 0; i < 7; i++) {
            array[i] = "True";
        }
        for (int i = 7; i < 10; i++) {
            array[i] = "False";
        }
        int index = random.nextInt(10); //randomly generate an index for the array
        Log.v("returning: ", array[index]);
        return array[index]; //return (randomly) one of the strings in the array; has a 70% chance of returning "True"
    }

    /*@Override
    protected void onNewIntent(Intent intent) { //not currently in use but left here for illustration (see javaClassify() below)
        if (!intent.getStringExtra("origin").equals(null)) {
            if (intent.getStringExtra("origin").equals("alarm")) {
                try {
                    javaClassify();
                } catch (FileNotFoundException f) {
                    Toast.makeText(MainActivity.this, "No data to classify", Toast.LENGTH_LONG).show();
                }
            }
        }
    }*/

    public void viewHistory(View view) {
        if (countedSteps == null || isMyServiceRunning(TheService.class) == false) {
            Toast.makeText(MainActivity.this, "Start monitoring in order to view stats!", Toast.LENGTH_LONG).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            String message ="Start monitoring in order to view stats!";
            dialog.setMessage(message);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();


            return;
        }
        if (myDb.isdbempty()) {
            //Toast.makeText(MainActivity.this, "No scores logged yet! Scores will be available from the end of the first week.", Toast.LENGTH_LONG).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            String message ="No scores logged yet! Scores will be available from the end of the first week.";
            dialog.setMessage(message);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
            return;
        }
        Intent intent = new Intent(this, ScoreHistory.class);
        startActivity(intent);
    }

    public void insertDummyData(View view) throws FileNotFoundException {
        final Handler handler = new Handler();
        final Context con = this;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                myDb.insertData(80000, 8, 2500, 18, 1, 30, 20000, 2000);
                Intent toAlarm = new Intent("ALARM_INTENT");
                LocalBroadcastManager.getInstance(con).sendBroadcast(toAlarm);
                editor.putString("score", "-1"); //this should be in the insertdata method
                editor.apply();
            }
        }, 5000);
    }

    public void moreDetail(View view) { //code for the dialog box that will pop up if users wants a little more detail on where they can improve their scores
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        Cursor res = myDb.getLastLine();
        while (res.moveToNext()) {
            String message = "";
            int stepcount = res.getInt(0);
            int calltextcount = res.getInt(1) + res.getInt(3);
            float socialapptime = res.getFloat(6);
            if (stepcount < 70000) { //these can be adjusted depending on how low you want activity to be before you tell users to do better
                message += "It doesn’t look like you were that physically active last week. As little as moderate exercise such as a light jog or even a long walk a couple of times a week can greatly improve mental wellbeing. Give it a try! (But not in a thunderstorm). ";
            }
            if (calltextcount < 20 && socialapptime < (float) 10000) {
                if (message.length() > 0) {
                    message += "Also, it ";
                }
                else {
                    message += "It ";
                }
                message += "doesn't look like you were particularly talkative last week. This might be because you do all your socialising in person, and prefer a face-to-face to a phone-to-ear conversation. But not reaching out, or replying to texts and calls, is a potential indicator that a person is not as happy as usual. ";
            }
            if (socialapptime > (float) 25200) {
                message += "You're spending a LOT of time on your social media apps. Studies have shown that if you spend too much time browsing through them, you're more likely to feel depression and social isolation. 30 minutes a day or less is considered relatively healthy (but even that sounds like a lot, right?). ";
            }
            if (message.equals("")) {
                message = "You're actually doing pretty well! You're getting a good amount of exercise, staying in contact with people but not overdoing social media use. If you're still not feeling great, try adding an extra exercise routine, learning a new skill, spending more time with friends, or just treating yourself to a thing or two that you enjoy.";
            }
            dialog.setMessage(message);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
        }
    }

    private String loadAssetTextAsString(Context context, String name) { //used to convert training set file to string (see method below)
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.v("loadAssetText: ", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("loadAssetText: ", "Error closing asset " + name);
                }
            }
        }

        return null;
    }

    public void writeStringAsFile(final String fileContents, String fileName) { //writes the training set string produced by method above to internal storage
        try {
            FileWriter out = new FileWriter(new File(this.getFilesDir(), fileName));
            out.write(fileContents);
            out.close();
            Log.v("written: ", "success");
        } catch (IOException e) {
            Log.d("writeStringAsFile", "Error writing file to internal storage");
        }
    }

    public void javaClassify() throws FileNotFoundException { //NB this method is not currently in use but is left here to illustrate a previous implementation iteration
        String filepath = this.getFilesDir() + "/" + "welldata_java_vsn.csv";
        List<JavaClassifier.Instance> instances = JavaClassifier.readDataSet(filepath);
        Log.v("number of lines:", String.valueOf(instances.size()));
        //x = new double[] {74032,6,1680,60,5,216,19110,1680};
        Cursor res = myDb.getDataToClassify();
        res.moveToFirst();
        x = new double[] {res.getInt(0), res.getInt(1), res.getFloat(2), res.getInt(3), res.getInt(4), res.getFloat(5), res.getFloat(6), res.getFloat(7)};
        String s = "";
        for (int n = 0; n<x.length; n++) {
            s += String.valueOf(x[n]) + ",";
        }
        Log.v("classifying: ", s);
        mean = JavaClassifier.meancalculator(instances);
        stdev = JavaClassifier.standarddeviationcalc(instances);
        x = JavaClassifier.inputstandardise(x, mean, stdev);
        String test = "";
        String test2 = "";
        for (int i = 0; i<8; i++) {
            test += String.valueOf(JavaClassifier.meancalculator(instances)[i]) + ", ";
            test2 += String.valueOf(JavaClassifier.standarddeviationcalc(instances)[i]) + ", ";
        }
        Log.d("dummy","Sample means: " + test);
        Log.d("dummy", "Sample st devs: " + test2);
        /*for (int i = 0; i < instances.size(); i++) {
            instances.get(i).x = JavaClassifier.inputstandardise(instances.get(i).x, instances);
        }*/
        task task1 = new task();
        task1.execute(instances); //a background task would be started using ASyncTask in order to keep the UI responsive while model training is occurring
    }


    private class task extends AsyncTask<List<JavaClassifier.Instance>, Integer, JavaClassifier> { //also not currently in use but left for illustration as above

        @Override
        protected JavaClassifier doInBackground(List<JavaClassifier.Instance>... lists) {
            List<JavaClassifier.Instance> instances = lists[0];
            Log.v("Does logging work", "in here");
            for (int i = 0; i < instances.size(); i++) {
                instances.get(i).x = JavaClassifier.inputstandardise(instances.get(i).x, mean, stdev);
                //publishProgress((int) (i / (float) instances.size()) * 100);
                if (i%100 == 0) {
                    Log.v("at least we're", " iterating: " + String.valueOf(i));
                }
            }
            Log.v("is this done?", "hopefully");
            JavaClassifier logistic = new JavaClassifier(10);
            logistic.train(instances); // might have to put a second background task here...
            Log.v("is this done too?", "hopefully");
            return logistic;
        }

        @Override
        protected void onPostExecute(JavaClassifier c) {
            super.onPostExecute(c);
            double[] result = c.classify(x);
            score = JavaClassifier.score(result);
            Log.v("Score is: ", String.valueOf(score));
             // to insert the new week's score after new week's data is inserted, we have to update the score value in the new line of the db
            int week = (int) myDb.getThisWeekNumber();
            Log.v("week is: ", String.valueOf(week));
            myDb.insertScore(String.valueOf(week), score);
            editor.putString("score", String.valueOf(score));
            editor.apply();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showFeedBack();
                }
            }, 1500);
        }
    }

    private int newscore;
    public void addnewinfo(String feedback, int score, String data) throws Exception{
        //
        //this stupid shit could be improved
        //
        //
        //



        //int newscore = 0;
        try {
            String filepath = this.getFilesDir() + "/" + "welldata_java_vsn.csv";
            FileWriter fw = new FileWriter(filepath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            switch(feedback) {
                case "Far too high":
                    if ((score - 4) <= 0) {
                        bw.write("\r\n"+ String.valueOf(newscore) + "," + data);
                        break;
                    }
                    else {
                        newscore = score - 4;
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                case "Too high":
                    if ((score - 3) <= 0) {
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                    else {
                        newscore = score - 3;
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                case "Slightly high":
                    if ((score - 1) <= 0) {
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                    else {
                        newscore = score - 1;
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                case "Accurate":
                    bw.write("\r\n" + String.valueOf(score) + "," + data);
                case "Slightly low":
                    if ((score + 1) >= 10) {
                        bw.write("\r\n" + "10" + "," + data);
                        break;
                    }
                    else {
                        newscore = score + 1;
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                case "Too low":
                    if ((score + 3) >= 10) {
                        bw.write("\r\n" + "10" + "," + data);
                        break;
                    }
                    else {
                        newscore = score + 3;
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
                case "Far too low":
                    if ((score + 4) >= 10) {
                        bw.write("\r\n" + "10" + "," + data);
                        break;
                    }
                    else {
                        newscore = score + 4;
                        bw.write("\r\n" + String.valueOf(newscore) + "," + data);
                        break;
                    }
            }
            bw.close();
        } catch (IOException e){
            Log.v("fileWriter:", "could not append new line");
        }
    }

    public void setProgressColours(int score) {
        scoreView.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (score == 10) {
            scoreView.setTextSize(120);
            scoreView.setText("10");
            scoreView.setTextScaleX(0.85f);
        }
        scoreView.setText(String.valueOf(score));
        progress.setVisibility(View.VISIBLE);
        progress2.setVisibility(View.VISIBLE);
        progress2.setProgress(100);
        progress.setProgress(score*10);
        if (score == 1 || score == 2) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorNHSDarkBlue)));
            progress2.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorNHSDarkBlueTint)));
            scoreView.setTextColor(ContextCompat.getColor(this, R.color.colorNHSDarkBlue));
        }
        if (score == 3 || score == 4) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
            progress2.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimaryDarkTint)));
            scoreView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        if (score == 5 || score == 6) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorNHSOrange)));
            progress2.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorNHSOrangeTint)));
            scoreView.setTextColor(ContextCompat.getColor(this, R.color.colorNHSOrange));
        }
        if (score == 7 || score == 8) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorNHSGreen)));
            progress2.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorNHSGreenTint)));
            scoreView.setTextColor(ContextCompat.getColor(this, R.color.colorNHSGreen));
        }
        if (score == 9 || score == 10) {
            progress.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreen)));
            progress2.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorLightGreenTint)));
            scoreView.setTextColor(ContextCompat.getColor(this, R.color.colorLightGreen));
        }
    }

    public void waysDetail(View view) { //code for the dialog box that will pop up if users want a reminder of what the 5 ways are
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String message = "The 5 Ways to Wellbeing are a set of steps that we can all take to improve our mental wellbeing. They are as follows: \n\nConnect - with people around you, including friends and family \nBe Active - take a walk, go cycling, go to the gym, etc... \nKeep Learning - for example, pick up a hobby or look up a recipe \nGive To Others - anything from a small act of kindness like a smile, to something like volunteering \nBe Mindful - appreciate your experiences and the world around you, and be more aware of your thoughts and feelings \n\nYour weekly score rates the extent to which you have followed these steps during the week, based on your phone usage. \n\n This app tracks the following: steps taken per week, outgoing calls and texts per week, quantity of photos and videos taken per week, and time spent in calls, using Google searches, and on social media apps.";
        dialog.setMessage(message);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
    }

    public void privacyDetail(View view) { //code for the dialog box that will pop up if users want to know what the switch does
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String message = "If this filter switch is on, your feedback on your score predictions will be statistically deniable at an individual level - in other words, it will be completely anonymous!";
        dialog.setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog.show();
    }


    private void makePdf() {

        //CREATE POP UP MADE NOW NEED NO FILE TO CREATE POPUP


        Cursor res = myDb.getLastLine();//decide whether put cursor outside
        res.moveToFirst();
        Cursor score = myDb.getScore();
        Cursor feedback = myDb.getFeedback();
        Cursor fm = myDb.getFeedbackMessage();
        feedback.moveToFirst();
        fm.moveToFirst();
        score.moveToFirst();
        TemplatePDF templatePDF = new TemplatePDF(getApplicationContext());
        templatePDF.openDocument();

        //???????????????????????????????????????????
        //using mydb or preference date: sneding most uptodate data or one (this) week data?????????

        //add check db empty or not

        //templatePDF.addTitle(new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(System.currentTimeMillis()));
        templatePDF.addEmphasizedSubject("Week Number:",String.valueOf(myDb.getThisWeekNumber()));
        //templatePDF.addEmphasizedSubject("Latest score:",preferences.getString("score", ""));

        templatePDF.addEmphasizedSubject("Score:",String.valueOf(res.getInt(8)));
        templatePDF.addEmphasizedSubject("Feedback Message:",fm.getString(0));

        //templatePDF.addEmphasizedSubject("Feedback:",String.valueOf(feedback.getInt(0)));

        // templatePDF.addEmphasizedSubject("Feedback Score:",feedback.getString(1));
        //templatePDF.addSubject("Calls made:",String.valueOf(preferences.getInt("callcount", 0)));


        //temporary use:
        //debug the original db feenback code to get rid of
        int feedbackScore = 0;
        int scoreTemp = res.getInt(8);
        switch(fm.getString(0)) {
            case "Far too high":
                if ((scoreTemp - 4) <= 0) {
                    break;
                }
                else {
                    feedbackScore = scoreTemp -4;
                    break;
                }
            case "Too high":
                if ((scoreTemp - 3) <= 0) {
                    break;
                }
                else {
                    feedbackScore = scoreTemp - 3;
                    break;
                }
            case "Slightly high":
                if ((scoreTemp - 1) <= 0) {
                    break;
                }
                else {
                    feedbackScore = scoreTemp - 1;
                    break;
                }
            case "Accurate":
                feedbackScore=scoreTemp;
            case "Slightly low":
                if ((scoreTemp + 1) >= 10) {
                    feedbackScore=10;
                    break;
                }
                else {
                    feedbackScore = scoreTemp + 1;
                    break;
                }
            case "Too low":
                if ((scoreTemp + 3) >= 10) {
                    feedbackScore=10;
                    break;
                }
                else {
                    feedbackScore = scoreTemp + 3;
                    break;
                }
            case "Far too low":
                if ((scoreTemp + 4) >= 10) {
                    feedbackScore=10;
                    break;
                }
                else {
                    feedbackScore = scoreTemp + 4;
                    break;
                }
        }
        templatePDF.addEmphasizedSubject("Feedback:",String.valueOf(feedbackScore));
        templatePDF.addEmphasizedSubject("Error Rate:", String.valueOf(abs(score.getInt(0)- feedbackScore)*100/score.getInt(0) + "%"));
        //templatePDF.addEmphasizedSubject("Error Rate:", String.valueOf(abs(score.getInt(0)-feedback.getInt(0))/score.getInt(0)*100 + "%"));
        templatePDF.addSubTitle("User Behaviors:");
        templatePDF.addSubject("Step Count:","  "+String.valueOf(res.getInt(0)));
        templatePDF.addSubject("Calls Count:","  "+String.valueOf(res.getInt(1)));
        templatePDF.addSubject("Calls time:","  "+ String.valueOf(Math.round(res.getInt(2)/60.0))+"  min");
        templatePDF.addSubject("Messages Count:","  "+String.valueOf(res.getInt(3)));
        templatePDF.addSubject("Viewed images and videos count:","  "+String.valueOf(res.getInt(4))+"  min");
        templatePDF.addSubject("Camera Time:","  "+String.valueOf(res.getInt(5)/60.0)+"  min");
        templatePDF.addSubject("Social Time:","  "+String.valueOf(res.getInt(6)/60.0)+"  min");
        templatePDF.addSubject("Browser Time:","  "+String.valueOf(res.getInt(7)/60.0)+"  min");
        templatePDF.closeDocu();
        Toast.makeText(this,String.valueOf(score.getInt(0)), Toast.LENGTH_SHORT).show();


        ShowCreatePDFPopup(templatePDF.getFileName());

    }

    private void ShowCreatePDFPopup(final String filename) {
        epicDialog.setContentView(R.layout.epic_popup_pdf_create);
        closePopup = (ImageView) epicDialog.findViewById(R.id.closePopup);
        btnShare = (Button) epicDialog.findViewById(R.id.sharePDF);
        thefilename = (TextView) epicDialog.findViewById(R.id.fileAddress);
        pdfimg = (ImageView)epicDialog.findViewById(R.id.pdfimg);

        thefilename.setText(filename);
        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();

            }
        });

        pdfimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPdf();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePdf(filename);
            }
        });

        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();
    }

    private void viewPdf(){
       TemplatePDF.viewPdf();
    }
    private void sharePdf(String filename){
        Uri Path = FileProvider.getUriForFile(this,"com.example.jakesetton.myfirstapp.provider",new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"PDF/"+filename));

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM,Path);
        shareIntent.setType("application/pdf");
        //shareIntent.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(shareIntent,"share"));

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case STORAGE_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    makePdf();
                }
                else {
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }


}
