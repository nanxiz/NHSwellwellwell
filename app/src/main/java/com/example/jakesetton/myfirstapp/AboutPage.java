package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;

public class AboutPage extends TutorialActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        setTitle("Welcome to WellWellWell!");


/*
        addFragment(new Step.Builder().setTitle("This is header")
                .setContent("This is content")
                .setBackgroundColor(Color.parseColor("#FF0957")) // int background color
                //.setDrawable(R.drawable.ss_1) // int top drawable
                .setSummary("This is summary")
                .build());

 */

 /*
        // Permission Step
        addFragment(new PermissionStep.Builder().setTitle(getString(R.string.permission_title))
                .setContent(getString(R.string.permission_detail))
                .setBackgroundColor(Color.parseColor("#FF0957"))
                .setDrawable(R.drawable.ss_1)
                .setSummary(getString(R.string.continue_and_learn))
                .setPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .build());
*/


    }
    public void infoscreen(View view){
        Intent intent = new Intent(this, MonitoringInfo.class);
        startActivity(intent);
    }
}
