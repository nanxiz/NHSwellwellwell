package com.example.jakesetton.myfirstapp;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Button;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.assertTrue;
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class PermissionsTest {

    private First activity;
    @Test
    public void usageStatsClickTester(){ //checking an intent is launched from the 'View Score History' button

        activity = Robolectric.setupActivity(First.class);
        Button launcher = (Button) activity.findViewById(R.id.statsenable);
        launcher.performClick();
        Intent expectedIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent actualIntent = shadowActivity.getNextStartedActivity();
        assertTrue(actualIntent.filterEquals(expectedIntent));
    }
}