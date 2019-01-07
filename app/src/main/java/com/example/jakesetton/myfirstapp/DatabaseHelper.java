package com.example.jakesetton.myfirstapp;

import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "User_data.db";
    public static final String TABLE_NAME = "Weekly_data";
    public static final String TABLE_NAME_2 = "Weekly_Feedback";
    public static final String COL_1 = "WEEK_NUM";
    public static final String COL_2 = "STEPS_COUNTED";
    public static final String COL_3 = "CALLS_COUNT";
    public static final String COL_4 = "CALL_TIME";
    public static final String COL_5 = "TEXTS_COUNT";
    public static final String COL_6 = "IMG_VIDEO_COUNT";
    public static final String COL_7 = "CAMERA_TIME";
    public static final String COL_8 = "SOCIAL_TIME";
    public static final String COL_9 = "BROWSER_TIME";
    public static final String COL_10 = "SCORE";

    public static final String COL_1_2 = "WEEK_NUM_2";
    public static final String COL_2_2 = "SCORE";
    public static final String COL_3_2 = "FEEDBACK";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (WEEK_NUM INTEGER PRIMARY KEY AUTOINCREMENT, STEPS_COUNTED INTEGER, CALLS_COUNT INTEGER, CALL_TIME FLOAT, TEXTS_COUNT INTEGER, IMG_VIDEO_COUNT INTEGER, CAMERA_TIME FLOAT, SOCIAL_TIME FLOAT, BROWSER_TIME FLOAT, SCORE INTEGER)");
        db.execSQL("CREATE TABLE " + TABLE_NAME_2 + " (WEEK_NUM_2 INTEGER PRIMARY KEY AUTOINCREMENT, SCORE INTEGER, FEEDBACK TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_2);
        onCreate(db);
    }

    public boolean insertData(int stepscount, int callscount, float calltime, int textcount, int mediacount, float cameratime, float socialtime, float browsertime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, stepscount);
        contentValues.put(COL_3, callscount);
        contentValues.put(COL_4, calltime);
        contentValues.put(COL_5, textcount);
        contentValues.put(COL_6, mediacount);
        contentValues.put(COL_7, cameratime);
        contentValues.put(COL_8, socialtime);
        contentValues.put(COL_9, browsertime);
        //contentValues.put(COL_10, score);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }

    public boolean insertFeedback(int score, String feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2_2, score);
        contentValues.put(COL_3_2, feedback);
        long result = db.insert(TABLE_NAME_2, null, contentValues);
        if (result == -1) {
            return false;
        }
        else {
            return true;
        }
    }



    public boolean insertScore(String week, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_10, score);
        db.update(TABLE_NAME, contentValues, "WEEK_NUM = ?", new String[] {week});
        return true;
    }

    public boolean insertScore2(String week, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET SCORE = " + score + " WHERE WEEK_NUM = " + week);
        return true;
    }

    public long getThisWeekNumber() {
        SQLiteDatabase db = this.getWritableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        db.close();
        return count;
    }

    public Cursor getFeedback(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + COL_2_2 + " FROM " + TABLE_NAME_2, null);
        return res;
    }

    public Cursor getFeedbackMessage(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + COL_3_2 + " FROM " + TABLE_NAME_2, null);
        return res;
    }



    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT SUM(" + COL_2 + "), SUM(" + COL_3 + "), " + COL_4 + ", SUM(" + COL_5 + "), " + COL_6 + ", " + COL_7 + ", " + COL_8 + ", " + COL_9 + " FROM " + TABLE_NAME + " ORDER BY " + COL_1 + " DESC LIMIT 1;", null);
        return res;
    }

    public Cursor getAllEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + COL_2 + ", " + COL_3 + ", " + COL_4 + ", " + COL_5 + ", " + COL_6 + ", " + COL_7 + ", " + COL_8 + ", " + COL_9 + " FROM " + TABLE_NAME + ";", null);
        return res;
    }

    public Cursor getScore() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + COL_10 + " FROM " + TABLE_NAME, null);
        return res;
    }

    public Cursor getLastLine() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + COL_2 + ", " + COL_3 + ", " + COL_4 + ", " + COL_5 + ", " + COL_6 + ", " + COL_7 + ", " + COL_8 + ", " + COL_9 + ", " + COL_10 + " FROM " + TABLE_NAME + " ORDER BY " + COL_1 + " DESC LIMIT 1;", null);
        return res;
    }

    public Cursor getDataToClassify() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT " + COL_2 + ", " + COL_3 + ", " + COL_4 + ", " + COL_5 + ", " + COL_6 + ", " + COL_7 + ", " + COL_8 + ", " + COL_9 + " FROM " + TABLE_NAME + " ORDER BY " + COL_1 + " DESC LIMIT 1;", null);
        return res;
    }

    public boolean isdbempty(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        if (res != null) {
            res.moveToFirst();
            if (res.getInt (0) == 0) {
                return true;
            }
            else {
                return false;
            }
        }
        return true;
    }
}
