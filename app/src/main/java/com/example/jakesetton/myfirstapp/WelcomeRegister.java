package com.example.jakesetton.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeRegister extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_register);
    }
    public void nextscreen(View view){
        Intent intent = new Intent(this, AboutPage.class);
        startActivity(intent);
    }
}
