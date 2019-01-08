package com.example.jakesetton.myfirstapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ScoreHistory extends AppCompatActivity {

    DatabaseHelper myDb;
    List<String> scorelist;
    static LineChart chart;
    static BarChart stepChart;
    static BarChart usageChart;
    LineDataSet set;
    LineData lineData;
    static int counter;

    static Button scoreButton;
    static Button usageButton;
    static Button stepButton;

    static TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);
        chart = findViewById(R.id.chart1);
        stepChart = findViewById(R.id.chart2);
        usageChart = findViewById(R.id.chart3);
        stepChart.setVisibility(View.INVISIBLE);
        usageChart.setVisibility(View.INVISIBLE);
        chart.setVisibility(View.VISIBLE);

        scoreButton = findViewById(R.id.scoregraphbutton);
        usageButton = findViewById(R.id.usagegraphbutton);
        stepButton = findViewById(R.id.stepgraphbutton);

        title = findViewById(R.id.graphTitle);
        title.setText("Your Scores So Far");

        scoreButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        myDb = new DatabaseHelper(this);
        if (myDb.isdbempty()) {
            return;
        }

        //----- Creating line chart of user's scores over time -----------
        List<String> scorelist = new ArrayList<>();
        Cursor res = myDb.getScore();
        while (res.moveToNext()) {
            int s = res.getInt(0);
            if (String.valueOf(s) != null) {
                scorelist.add(String.valueOf(s));
            }
        }
        String[] scores = new String[scorelist.size()];
        for (int i = scores.length-1; i >= 0;  i--) {
            scores[i] = "Week " + String.valueOf(i+1) + ": " + scorelist.get(i); //can add extra predictions or extra graphs
        }
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < scores.length; i++) {
            entries.add(new Entry((float) (i+1), Float.parseFloat(scorelist.get(i))));
        }
        set = new LineDataSet(entries,"Overall Weekly Score");
        //set.setColor... .setValueTextColor
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10);
        set.setLineWidth(4);

        List<ILineDataSet> list = new ArrayList<ILineDataSet>();
        list.add(set);
        Log.v("Size =", String.valueOf(set.getValues().size()));
        lineData = new LineData(list);
        if (lineData == null || chart == null) {
            Log.v("It's null on!", "Wow");
        }
        Log.v("linedata data", String.valueOf(lineData.getDataSetCount()));

        setTitle("Your weekly stats");
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMaximum(10);
        yAxis.setAxisMinimum(0);
        yAxis.setGranularity(1);
        yAxis.setDrawGridLines(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1);
        if (scorelist.size() < 20) {
            xAxis.setLabelCount(scorelist.size() + 1);
        }
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Legend legend = chart.getLegend();
        //chart.getDescription().setText("Your welfare scores so far");
        chart.getDescription().setEnabled(false);
        xAxis.setDrawGridLines(false);
        YAxis axis2 = chart.getAxisRight();
        axis2.setEnabled(false);
        chart.setData(lineData);
        //---------------------------------------------------------
        //Creating Barcharts of 1) steps taken and 2) time spent on the phone and on social media apps



        List<BarEntry> steplist = new ArrayList<>();
        List<BarEntry> phonelist = new ArrayList<>();
        List<BarEntry> sociallist = new ArrayList<>();
        counter = 1;
        Cursor c = myDb.getAllEntries();
        while (c.moveToNext()) {
            steplist.add(new BarEntry(counter, c.getInt(0)));
            phonelist.add(new BarEntry(counter, (c.getFloat(2)/60)));
            sociallist.add(new BarEntry(counter, (c.getFloat(6)/60)));
            counter++;
        }

        BarDataSet stepSet = new BarDataSet(steplist, "Steps taken in week");
        BarDataSet phoneSet = new BarDataSet(phonelist, "Time chatting on phone (mins)");
        BarDataSet socialSet = new BarDataSet(sociallist, "Time on social media (mins)");

        stepSet.setColor(ResourcesCompat.getColor(getResources(), R.color.colorLightGreen, null));
        //stepSet.setDrawValues(false);


        socialSet.setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        phoneSet.setColor(ResourcesCompat.getColor(getResources(), R.color.colorNHSOrange, null));

        BarData stepData = new BarData(stepSet);
        //stepData.setBarWidth...
        XAxis stepXAxis = stepChart.getXAxis();
        stepXAxis.setGranularity(1);
        stepXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        stepXAxis.setAxisMinimum(0);
        stepXAxis.setDrawGridLines(false);
        if (steplist.size() < 20) {
            stepXAxis.setLabelCount(steplist.size());
        }
        stepXAxis.setLabelCount(steplist.size());

        YAxis stepYAxis = stepChart.getAxisLeft();
        stepYAxis.setAxisMinimum(0);
        stepYAxis.setDrawGridLines(false);
        stepYAxis.setGranularity(20000f);
        stepYAxis.setDrawLabels(true);

        stepChart.setData(stepData);
        stepChart.setFitBars(true);
        stepChart.getDescription().setEnabled(false);
        stepChart.getAxisRight().setEnabled(false);

        BarData usageData = new BarData(phoneSet, socialSet);
        usageData.setBarWidth(0.45f);
        //usageData.setBarWidth...
        XAxis usageXAxis = usageChart.getXAxis();
        YAxis usageYAxis = usageChart.getAxisLeft();
        usageYAxis.setDrawGridLines(false);
        usageXAxis.setGranularityEnabled(true);
        if (sociallist.size() < 20) {
            usageXAxis.setLabelCount(sociallist.size());
        }

        //usageXAxis.setGranularity(1);
        usageXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        usageXAxis.setAxisMinimum(1);
        usageXAxis.setAxisMaximum(sociallist.size() + 1);
        usageXAxis.setXOffset(0.1f);
        usageXAxis.setDrawGridLines(false);
        Legend leg = usageChart.getLegend();
        usageXAxis.setCenterAxisLabels(true);
        usageChart.setData(usageData);
        usageChart.groupBars(1, 0.1f, 0f);
        usageChart.getDescription().setEnabled(false);
        YAxis axis4 = usageChart.getAxisRight();
        axis4.setEnabled(false);

    }

    public void viewSteps(View view) { //method for button to view steps per week chart
        stepChart.setVisibility(View.VISIBLE);
        usageChart.setVisibility(View.INVISIBLE);
        chart.setVisibility(View.INVISIBLE);
        stepButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        usageButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        scoreButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        stepButton.setElevation(1);
        usageButton.setElevation(0);
        scoreButton.setElevation(0);
        title.setText("Total Weekly Steps Taken");
    }

    public void viewUsage(View view) { //method for button to view usage stats per week chart
        stepChart.setVisibility(View.INVISIBLE);
        usageChart.setVisibility(View.VISIBLE);
        chart.setVisibility(View.INVISIBLE);
        stepButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        usageButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        scoreButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        stepButton.setElevation(0);
        usageButton.setElevation(1);
        scoreButton.setElevation(0);
        title.setText("Weekly Phone Usage Stats");
    }


    public void viewProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void viewSetting(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void viewScore(View view) { //method for button to view scores chart
        stepChart.setVisibility(View.INVISIBLE);
        usageChart.setVisibility(View.INVISIBLE);
        chart.setVisibility(View.VISIBLE);
        stepButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        usageButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        scoreButton.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        stepButton.setElevation(0);
        usageButton.setElevation(0);
        scoreButton.setElevation(1);
        title.setText("Wellbeing Scores So Far");
    }

    public void viewMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    public void viewShare(View view) {
        Intent intent = new Intent(this, ShareActivity.class);
        startActivity(intent);

    }



    public void viewLive(View view) {
        /*if (!main.isMyServiceRunning(TheService.class)) {
            Toast.makeText(ScoreHistory.this, "Start monitoring in order to view stats!", Toast.LENGTH_LONG).show();
            return;
        }*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("history", "liveweek");
        startActivity(intent);
    }




}
