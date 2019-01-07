package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.List;

import me.everything.providers.android.browser.Bookmark;
import me.everything.providers.android.browser.BrowserProvider;
import me.everything.providers.android.browser.Search;
import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.telephony.Mms;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class TheService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Sensor accel;
    private Sensor peddetect;
    private Sensor pedcount;
    private int numSteps;
    public String startmessage = "Service on!!!";

    private final static int NOTIFICATION_ID = 95;

    public static int oldstepcount;
    public static int stepcount;
    public static int newstepcount;
    public static int curdetectedsteps;
    static double[] x;
    static int score;

    //making sure the service is running
    boolean servicestatus;
    static NotificationManager notificationManager;

    Intent intent;

    boolean setForeground;

    private static final String TAG = "StepService";
    public static final String BROADCAST_ACTION = "com.example.jakesetton.myfirstapp.mybroadcast";

    private final Handler handler = new Handler();

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    int counter = 0;

    SharedPreferences preferences2;
    SharedPreferences.Editor editor2;

    //---- Setup for providers
    Context context;
    //---

    String alarmcheck;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    static double[] mean;
    static double[] stdev;
    DatabaseHelper myDb;

    public void onCreate() {
        super.onCreate();
        //this code draws on an earlier GitHub implementation of a pedometer service; see the references for details
        context = getApplicationContext();
        myDb = new DatabaseHelper(context);
        LocalBroadcastManager.getInstance(context).registerReceiver(stepReceiver, new IntentFilter("ALARM_INTENT"));
        preferences = context.getSharedPreferences("MyPreferences", context.MODE_PRIVATE);
        editor = preferences.edit();
        alarmcheck = preferences.getString("alarmstatus", "");
        if (alarmcheck.equals("on") == false) {
            Log.d("No alarm set", "!");
            onDestroy();
            return;
        }


        intent = new Intent(BROADCAST_ACTION);
        preferences2 = getApplicationContext().getSharedPreferences("MyPreferences", getApplicationContext().MODE_PRIVATE);
        editor2 = preferences2.edit();
        stepcount = 0;
        oldstepcount = preferences2.getInt("oldstep", 0);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Service", "Start");
        context = getApplicationContext();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startMyOwnForeground();
        }
        else {
            showNotification();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Log.d("Service", "gotaccel");
            accel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            //Toast.makeText(this, "Your device has no accelerometer!", Toast.LENGTH_LONG).show();
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            peddetect = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            Log.d("Service", "gotdetector");
            pedcount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        } else {
            Toast.makeText(this, "Your device has no pedometer!", Toast.LENGTH_LONG).show();
        }

        //methods for adding up steps and getting accelerometer values
        mSensorManager.registerListener(this, accel, 0);
        mSensorManager.registerListener(this, peddetect, 0);
        mSensorManager.registerListener(this, pedcount, 0);
        servicestatus = false;


        handler.removeCallbacks(updateBroadcastData);
        handler.post(updateBroadcastData);

        return START_STICKY;
    }

    public void onDestroy() {
        Log.v("Service", "Stop" + stepcount + curdetectedsteps);;

        servicestatus = true;

        dismissNotification();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(stepReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            LiveSensors.stepstaken++;
            int detectSteps = (int) event.values[0];
            curdetectedsteps += detectSteps;
        }
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int countSteps = (int) event.values[0];
            if (stepcount == 0) {
                stepcount = (int) event.values[0];
            }
            newstepcount = countSteps - stepcount;
        }
    }

    private Runnable updateBroadcastData = new Runnable() {
        public void run() {
            if (!servicestatus) {
                // Call the method that broadcasts data to the Activity
                broadcastSensorValue();
                editor2.putInt("stepcount", newstepcount + oldstepcount);
                editor2.apply();
                // Call "handler.postDelayed" again, after a specified delay.
                handler.postDelayed(this, 2000);
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void broadcastSensorValue() {
        intent.putExtra("Counted_Step_Int", newstepcount);
        intent.putExtra("Counted_Step", String.valueOf(newstepcount + oldstepcount));
        intent.putExtra("Detected_Step_Int", curdetectedsteps);
        intent.putExtra("Detected_Step", String.valueOf(curdetectedsteps));
        sendBroadcast(intent);
    }

    private void showNotification() { //update method if need be
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("Pedometer");
        notificationBuilder.setContentText("Pedometer is running in the background.");
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setColor(Color.parseColor("#6600cc"));
        int colorLED = Color.argb(255, 0, 255, 0);
        notificationBuilder.setLights(colorLED, 500, 500);
        // To  make sure that the Notification LED is triggered.
        notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        notificationBuilder.setOngoing(true);

        //Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
        notificationBuilder.setContentIntent(resultPendingIntent);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //notificationManager.notify(0, notificationBuilder.build());

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.jakesetton.myfirstapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.GREEN);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;

        notificationManager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Pedometer session is running in the background.")
                .setColor(Color.parseColor("#6600cc"))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(NOTIFICATION_ID, notification);

    }



    private void dismissNotification() {

        notificationManager.cancel(NOTIFICATION_ID);
    }

    private BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                final Context c = context;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run () {
                        try {
                            Log.v("Hit", "registered");
                            //javaClassify();

                            String filepath = c.getFilesDir() + "/" + "welldata_java_vsn.csv";
                            List<JavaClassifier.Instance> instances = JavaClassifier.readDataSet(filepath);
                            //x = new double[] {25144,0,0,0,0,36,42000,0};
                            Cursor res = myDb.getDataToClassify();
                            res.moveToFirst();
                            x = new double[] {res.getInt(0), res.getInt(1), res.getFloat(2), res.getInt(3), res.getInt(4), res.getFloat(5), res.getFloat(6), res.getFloat(7)};
                            mean = JavaClassifier.meancalculator(instances);
                            stdev = JavaClassifier.standarddeviationcalc(instances);
                            x = JavaClassifier.inputstandardise(x, mean, stdev);
                            for (int i = 0; i < instances.size(); i++) {
                                instances.get(i).x = JavaClassifier.inputstandardise(instances.get(i).x, mean, stdev);
                                //publishProgress((int) (i / (float) instances.size()) * 100);
                                if (i%100 == 0) {
                                    Log.v("at least we're", " iterating: " + String.valueOf(i));
                                }
                            }
                            Log.v("is this done?", "hopefully");
                            JavaClassifier logistic = new JavaClassifier(10);
                            logistic.train(instances);
                            double[] result = logistic.classify(x);
                            score = JavaClassifier.score(result);
                            Log.v("Score is: ", String.valueOf(score));
                            // to insert the new week's score after new week's data is inserted, we have to update the score value in the new line of the db
                            int week = (int) myDb.getThisWeekNumber();
                            Log.v("week is: ", String.valueOf(week));
                            myDb.insertScore(String.valueOf(week), score);
                            editor.putString("score", String.valueOf(score));
                            editor.apply();
                            Intent openmainpage = new Intent(getApplicationContext(), MainActivity.class);//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                            openmainpage.putExtra("origin", "alarm");
                            openmainpage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            c.startActivity(openmainpage); //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

                        }
                        catch (FileNotFoundException f) {
                            Log.v("onReceive L.B.M: ", "unable to find file");
                        }
                    }
                }, 3000);
        }
    };

}