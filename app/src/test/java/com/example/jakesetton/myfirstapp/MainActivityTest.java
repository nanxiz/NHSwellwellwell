package com.example.jakesetton.myfirstapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)

public class MainActivityTest {

    private MainActivity activity;
    Context context = RuntimeEnvironment.application;

    @Test
    public void testStartMonitor(){
        activity = Robolectric.setupActivity(MainActivity.class);
        Button startButton = (Button) activity.findViewById(R.id.startmonitoring);
        assertNotNull("Button was not found!", startButton);
        startButton.performClick();
        Intent expectedIntent = new Intent(activity, TheService.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent actualIntent = shadowActivity.getNextStartedActivity();
        assertTrue(actualIntent.filterEquals(expectedIntent));
        //assertTrue("Pedometer service is not working!", activity.isMyServiceRunning(TheService.class)); // pedometer status

        Button stopButton = (Button) activity.findViewById(R.id.stopmonitoring);
        assertEquals("No stop monitoring button appears", View.VISIBLE, stopButton.getVisibility());  //activity appearance change
        assertEquals("Start button still overlaying stop button", View.INVISIBLE, startButton.getVisibility());
    }

    @Test                           //can just add this code at the end of the startMonitor() one?
    public void testStopMonitor(){ //corresponding test for if the user decides to stop monitoring
        activity = Robolectric.setupActivity(MainActivity.class);
        Button stopButton = (Button) activity.findViewById(R.id.startmonitoring);
        assertNotNull("Button was not found!", stopButton);

        Button startButton = (Button) activity.findViewById(R.id.startmonitoring);
        startButton.performClick();
        stopButton.performClick();

        assertFalse("Pedometer service still running!", activity.isMyServiceRunning(TheService.class));

        assertEquals("Stop monitoring button still visible", View.INVISIBLE, stopButton.getVisibility());
        assertEquals("Start button still invisible", View.VISIBLE, startButton.getVisibility());

    }


    @Test
    public void liveSensorsOnClick(){ //checking an intent is launched from the 'View Live Stats' button
        activity = Robolectric.setupActivity(MainActivity.class);
        Button startButton = (Button) activity.findViewById(R.id.startmonitoring);
        startButton.performClick();
        Button liveWeekButton = (Button) activity.findViewById(R.id.home);
        liveWeekButton.performClick();
        Intent expectedIntent = new Intent(activity, LiveSensors.class);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent actualIntent = shadowActivity.getNextStartedActivity();
        assertTrue(actualIntent.filterEquals(expectedIntent));

    }

    @Test
    public void scoreHistoryOnClick(){ //checking an intent is launched from the 'View Score History' button
        activity = Robolectric.setupActivity(MainActivity.class);
        Button startButton = (Button) activity.findViewById(R.id.startmonitoring);
        startButton.performClick();

        Button liveWeekButton = (Button) activity.findViewById(R.id.button2);
        liveWeekButton.performClick();
        Intent expectedIntent = new Intent(activity, ScoreHistory.class);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent actualIntent = shadowActivity.getNextStartedActivity();
        assertTrue(actualIntent.filterEquals(expectedIntent));
    }

}
