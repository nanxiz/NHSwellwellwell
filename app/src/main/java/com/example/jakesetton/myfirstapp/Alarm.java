package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.everything.providers.android.browser.BrowserProvider;
import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.android.media.Image;
import me.everything.providers.android.media.MediaProvider;
import me.everything.providers.android.media.Video;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class Alarm extends BroadcastReceiver {

    String alarmcheck;


    DatabaseHelper myDb;
    MediaProvider mediaProvider;
    TelephonyProvider telephonyProvider;
    BrowserProvider browserProvider;
    CallsProvider callsProvider;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    //Accessing SMS and MMS sent text (for "Connect")
    private List<Sms> smses;
    String countedMessages;

    int zero;

    //Accessing call logs (for "Give", "Connect")
    private Data<Call> calls;
    String countedCalls;

    Context context;

    float[] inputs;
    private TensorFlowClassifier classifier;
    private float[] results;

    static String stepps = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.v("Alarm triggered", "now");
        classifier = new TensorFlowClassifier(this.context);
        preferences = context.getSharedPreferences("MyPreferences", context.MODE_PRIVATE);
        editor = preferences.edit();

        alarmcheck = preferences.getString("alarmstatus", "");
        Log.v("alarmcheck is: ", alarmcheck);

        if (alarmcheck.equals("on") == false) {
            Log.d("No alarm set", "!");
            return;
        }
        alarmcheck = preferences.getString("alarmstatus", "");
        Log.v("alarmcheck is: ", alarmcheck);
        if (alarmcheck.equals("on") == false) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent newintent = new Intent(context, Alarm.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    newintent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
            Log.v("Alarm cancelled:", "Done");

            return;
        }

        long startedtime = preferences.getLong("alarmtime", 0);
        Date intervaldate = new Date(startedtime);
        Log.v("onReceive called:", "Alarm is now " + intervaldate.toString()); //making sure the next scheduled score prediction is correct
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //dealing with cases where the alarm is triggered by BOOT_COMPLETED     (which is every time the phone is switched on, so not necessarily just at the end of every week...)
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Date currentdate = new Date(System.currentTimeMillis());
            ///etc.
            if ((currentdate.compareTo(intervaldate) < 0)) {  // if the phone is turned on before the next alarm, no data insertion/classification has been missed.
                Intent newintent = new Intent(context, Alarm.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        newintent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setRepeating(AlarmManager.RTC, startedtime, 1000*60*60*24*7, pendingIntent); //just re-establish the alarm for the scheduled time as it is, because it's in the future
                Log.v("Alarm will go off at: ", String.valueOf(intervaldate));
                return;
            }
            else if ((currentdate.compareTo(intervaldate) > 0)) { //if the phone is turned on after the alarm was supposed to go off, we've missed a data insertion

                Log.d("Update missed from", intervaldate.toString());

                //Move interval forward by a week and reschedule alarm

                Calendar d = Calendar.getInstance();
                d.setTime(intervaldate);
                d.add(Calendar.MINUTE, 60*24*7);
                long newtime = d.getTimeInMillis();
                Date newdate = new Date(newtime);
                //Log.v("miss, new long is:", String.valueOf(newtime));
                Intent newintent = new Intent(context, Alarm.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        newintent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setRepeating(AlarmManager.RTC, newtime, 1000*60*60*24*7, pendingIntent);
                Log.d("missed, alarm set for:", String.valueOf(newdate));
            }
        }

        myDb = new DatabaseHelper(context);
        String appstats = "";


        float socialapptime = 0;
        float cameratime = 0;
        float browsertime = 0;
        float calltime = 0;

        //-----------------------Code for collecting statistics about app usage -----------------------

        final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar beginCal = Calendar.getInstance(); //creates a calendar set for the present moment
        beginCal.add((Calendar.MINUTE), -60*24*7); //we're collecting stats from just the past week, so this is the start of the range
        beginCal.add((Calendar.SECOND), 10);

        Calendar endCal = Calendar.getInstance();
        Log.v("Date for begin and end:",beginCal.getTime().toString() + " " + endCal.getTime().toString());

        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginCal.getTimeInMillis(), endCal.getTimeInMillis());
        Log.v("List in full:", queryUsageStats.toString());
        for (UsageStats app : queryUsageStats) {
            if (app.getPackageName() == null || app.getTotalTimeInForeground() == 0) {
                continue;
            }
            if (app.getPackageName().contains("messaging") || app.getPackageName().contains("nstagram") || app.getPackageName().contains(".orca") || app.getPackageName().contains("snapchat") || app.getPackageName().contains("whatsapp")) {
                socialapptime += ((float) (app.getTotalTimeInForeground() / 1000));
            }
            if (app.getPackageName().contains("amera")) { //camera apps
                cameratime += ((float) (app.getTotalTimeInForeground() / 1000));
            }
            if (app.getPackageName().contains("dialer") || app.getPackageName().contains("phone")) {
                calltime += ((float) (app.getTotalTimeInForeground() / 1000));
            }
            if (app.getPackageName().contains("quicksearchbox")) { //can add chrome here too
                Log.v("logging: ", app.getPackageName());
                browsertime  = ((float) (app.getTotalTimeInForeground() / 1000));
            }

            //add code in here to do with getting browser usage info
            appstats += ((app.getPackageName() + ": " + String.valueOf((float) ((app.getTotalTimeInForeground() / 1000))))) + " | "; //this is in seconds
        }

        if (appstats != null) {
            Log.v("App stats:", appstats);
            Log.v("Social app stats:", String.valueOf(socialapptime));
            Log.v("Camera app stats:", String.valueOf(cameratime));
            Log.v("Call time: ", String.valueOf(calltime));
            Log.v("Broswer time:", String.valueOf(browsertime));
        }
        //-------------------------------------------------------------

        //---------- Getting image and video info ---------------------
        int imagecount = 0;
        int videocount = 0;
        mediaProvider = new MediaProvider(context);
        List<Image> imagelist = mediaProvider.getImages(MediaProvider.Storage.EXTERNAL).getList();
        List<Video> videolist = mediaProvider.getVideos(MediaProvider.Storage.EXTERNAL).getList();
        Log.v("Media list: ", imagelist + " " + videolist);
        for (Image image : imagelist) {
            String dateadded = image.toString().split(", ")[3].substring(10);
            Log.v("Added =:", dateadded);
            Date date = new Date(Long.parseLong(dateadded)*1000);
            Calendar cal = Calendar.getInstance();
            cal.add((Calendar.MINUTE), -60*24*7);
            Date thisweek = cal.getTime();
            if (thisweek.compareTo(date) < 0) {
                imagecount++;
            }
            else {
                continue;
            }

        }
        Log.v("Imagecount: ", String.valueOf(imagecount));

        for (Video video : videolist) {
            String dateadded = video.toString().split(", ")[7].substring(10);
            Log.v("Added =:", dateadded);
            Date date = new Date(Long.parseLong(dateadded)*1000);
            Calendar cal = Calendar.getInstance();
            cal.add((Calendar.MINUTE), -60*24*7);
            Date thisweek = cal.getTime();
            if (thisweek.compareTo(date) < 0) {
                videocount++;
            }
            else {
                continue;
            }

        }
        //--------getting message and call info---------------------------------------
        int mediacount = imagecount + videocount;
        telephonyProvider = new TelephonyProvider(context); //activity.getApplicationContext()
        browserProvider = new BrowserProvider(context);
        callsProvider = new CallsProvider(context);

        smses = telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList();

        String messageContent = smses.toString();
        countedMessages = String.valueOf(smses.size());

        String messagefiltered = "";
        String [] array = messageContent.split("bod");
        for (int i = 0; i<array.length; i++) {
            if (array[i].startsWith("y=")) {
                int errorindex = array[i].indexOf(", errorCode");
                messagefiltered += (array[i].substring(2, errorindex) + " || ");
            }
        }

        Log.v("SMS full text: ", messagefiltered);  // <<<<<<< IMPORTANT: this is the string containing the isolated content of all sent SMSs that can be fed into a sentiment analysis model should the latter come to fruition

        calls = callsProvider.getCalls();
        countedCalls = String.valueOf(calls.getList().size()); //this returns all the calls in the phone, but we only want the ones since the last database insertion, hence..

        int totalmsgcount = preferences.getInt("messagecount", 0); //call and text counts at the last insertion - these values are updated at the end of the alarm code below once insertion is done
        int totalcallcount = preferences.getInt("callcount", 0);

        countedMessages = String.valueOf(Integer.parseInt(countedMessages) - totalmsgcount);
        countedCalls = String.valueOf(Integer.parseInt(countedCalls) - totalcallcount);

        Log.v("Here are the outputs: ", stepps + countedCalls + " " + String.valueOf(calltime) + " " + countedMessages + " " + String.valueOf(mediacount) + " " + String.valueOf(cameratime) + " " + String.valueOf(socialapptime)  + " " + String.valueOf(browsertime));
        //results = classifier.predictWelfare(data);
        int score = 0; //= (int) results;

        Cursor res = myDb.getAllData();

        stepps = String.valueOf(preferences.getInt("stepcount", 0));

        if (stepps == null) {
            MainActivity activity = new MainActivity();
            stepps = activity.countedSteps;
        }
        Log.v("Steps is now:", stepps);



        if (res.getCount() == 0) {
            inputs =  new float[] {Float.parseFloat(stepps), Float.parseFloat(countedCalls), calltime, Float.parseFloat(countedMessages), (float) mediacount, cameratime, socialapptime, browsertime};
            results = classifier.predictWelfare(inputstandardise(inputs));
            String s = "";
            float highest = -1;
            for (int i=0; i<results.length; i++) {
                if (results[i] > highest) {
                    highest = results[i];
                    score = i;
                }
            }
            boolean isInserted = myDb.insertData(Integer.parseInt(stepps), Integer.parseInt(countedCalls), calltime, Integer.parseInt(countedMessages), mediacount, cameratime, socialapptime, browsertime); //or modify method here and in db to insert tensorflow-generated score
            Calendar c = Calendar.getInstance();
            c.setTime(intervaldate);
            c.add(Calendar.MINUTE, 60*7*24);
            long newinterval = c.getTimeInMillis();
            Log.v("1stinsert, alarm now", c.getTime().toString());
            editor.putLong("alarmtime", newinterval);
            smses = telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList();
            editor.putInt("messagecount", smses.size());
            calls = callsProvider.getCalls();
            editor.putInt("callcount", calls.getList().size());
            editor.apply();
        }
        else {

            while (res.moveToNext()) {
                int oldstepcount = res.getInt(0);
                int oldcallcount = res.getInt(1);
                int oldtextcount = res.getInt(3);

                Log.v("Ints and floats", String.valueOf(oldstepcount) + " " + String.valueOf(oldcallcount) + " " + String.valueOf(oldtextcount));


                if (stepps == null) {
                    int old = preferences.getInt("oldstep", 0);
                    stepps = String.valueOf(old);
                }
                Log.v("Steps is now: ", stepps);
                Log.v("Res.getInt(0) is: ", String.valueOf(oldstepcount));
                int newstepcount = Integer.parseInt(stepps) - oldstepcount;
                int newcallcount = Integer.parseInt(countedCalls) - oldcallcount;
                int newtextcount = Integer.parseInt(countedMessages) - oldtextcount;


                inputs =  new float[] {(float) (newstepcount), (float) newcallcount, calltime, (float) newtextcount, (float) mediacount, cameratime, socialapptime, browsertime}; //or modify method here and in db to insert tensorflow-generated score
                for (int i = 0; i<inputs.length; i++) {
                    Log.v("inputs:", String.valueOf(i) + ":" + String.valueOf(inputs[i]));
                }
                float[] standard = inputstandardise(inputs);
                results = classifier.predictWelfare(standard);      //this is the TF classifier doing its predictions
                String s = "";
                float highest = -1;
                for (int i=0; i<results.length; i++) {
                    if (results[i] > highest) {
                        highest = results[i];
                        score = i;
                    }
                }

                boolean isInserted = myDb.insertData(newstepcount, newcallcount, calltime, newtextcount, mediacount, cameratime, socialapptime, browsertime);
                if (isInserted == true) {
                    Toast.makeText(context, "Data Inserted - Score Prediction in Progress!", Toast.LENGTH_LONG).show();
                }
                Calendar c = Calendar.getInstance();
                c.setTime(intervaldate);
                c.add(Calendar.MINUTE, 60*24*7);
                Log.v("insertdone, alarm now", c.getTime().toString());
                long newinterval = c.getTimeInMillis();
                editor.putLong("alarmtime", newinterval);
                smses = telephonyProvider.getSms(TelephonyProvider.Filter.SENT).getList();
                editor.putInt("messagecount", smses.size());
                calls = callsProvider.getCalls();
                editor.putInt("callcount", calls.getList().size());
                editor.apply();
            }
        }
        //firing up the classifier
        Intent toAlarm = new Intent("ALARM_INTENT");
        LocalBroadcastManager.getInstance(context).sendBroadcast(toAlarm);

    }
    public static float[] inputstandardise(float[] f) {            //this is very long-winded, but is because there is no easy-to-implement standardisation function in TF
        f[0] = ((float) (f[0] - 62979)/ (float) 24589.1687);        //mean and st.dev values calculated from original dataset
        f[1] = ((float) (f[1] - 5.9595)/ (float) 4.2198);
        f[2] = ((float) (f[2] - 1386)/ (float) 1046.6346);
        f[3] = ((float) (f[3] - 60)/ (float) 42.4264);
        f[4] = ((float) (f[4] - 4.4935)/ (float) 2.8759);
        f[5] = ((float) (f[5] - 198.612)/ (float) 103.5881);
        f[6] = ((float) (f[6] - 23307.48)/ (float) 10025.5787);
        f[7] = ((float) (f[7] - 2165.645)/ (float) 1789.5679);
        return f;
    }


}

