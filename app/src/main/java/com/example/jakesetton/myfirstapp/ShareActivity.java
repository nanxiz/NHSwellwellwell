package com.example.jakesetton.myfirstapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static java.lang.Math.abs;

public class ShareActivity extends AppCompatActivity {
    DatabaseHelper myDb;
    Button createPdf;
    Dialog epicDialog;
    TextView titleTv,thefilename;
    TextView latestScore;
    ImageView closePopup, pdfimg;

    String[] TO = {"your_guardian@gmail.com"};
    String[] CC = {"next_of_kin@gmail.com"};

    Button btnSharePDF;
    Button btnSaveLocal;

    private static final int STORAGE_CODE = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);


        myDb = new DatabaseHelper(this);
        //classifier = new TensorFlowClassifier(this);
        latestScore = (TextView) findViewById(R.id.latestScoreToShare);



        if (myDb.isdbempty()){
            latestScore.setText("No Score Yet");
        }
        else
        {
            Cursor scoreT = myDb.getScore();
            scoreT.moveToFirst();
        latestScore.setText( String.valueOf(scoreT.getInt(0)));}


        //Toast.makeText(this,String.valueOf(scoreT.getInt(0)), Toast.LENGTH_SHORT).show();



        createPdf = findViewById(R.id.btnSharePDF);

        createPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //BASICALLY CHECKING FOR PERMISSION ISSUED OR NOT DUE TO VARIOUS OS SYSTEMS
                if (!myDb.isdbempty()) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            //no permission granted so need to request it
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions, STORAGE_CODE);
                        } else {
                            //permission granted
                            makePdf();

                        }

                    } else {
                        makePdf();
                    }
                }else{Toast.makeText(getApplicationContext(),"No weekly scores to share yet! Please wait.", Toast.LENGTH_LONG).show();
                }

            }
        });


        epicDialog=new Dialog(this);
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
        //using mydb or preference date: sending most uptodate data or one (this) week data?????????

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
        //this is due to original code's fking bug:
        //the feedback string could be written in to db however he failed to write correct feedback score
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
        //Toast.makeText(this,String.valueOf(score.getInt(0)), Toast.LENGTH_SHORT).show();


        ShowCreatePDFPopup(templatePDF.getFileName());

    }

    private void ShowCreatePDFPopup(final String filename) {
        epicDialog.setContentView(R.layout.epic_popup_pdf_create);
        closePopup = (ImageView) epicDialog.findViewById(R.id.closePopup);
        btnSharePDF = (Button) epicDialog.findViewById(R.id.sharePDF);
        btnSaveLocal = (Button) epicDialog.findViewById(R.id.saveToLocal);
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

        btnSharePDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePdf(filename);
            }
        });

        btnSaveLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),filename+"\nSaved to Downloads", Toast.LENGTH_SHORT).show();

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
        shareIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        shareIntent.putExtra(Intent.EXTRA_CC, CC);


        shareIntent.setType("application/pdf");
        //shareIntent.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(shareIntent,"share"));

    }



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
}
