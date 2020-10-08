package me.eli.jitteralarm.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "alarms.db";
    private static final String TABLE_NAME = "Alarms_Table";
    private static final String ALARM_NAME = "Alarm_Name";
    private static final String ALARM_TIME = "Alarm_Time";
    private static final String ALARM_OFFSET = "Alarm_Offset";
    private static final String TRIGGER_DAYS = "Trigger_Days";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "("
                + ALARM_NAME + " TEXT, "
                + ALARM_TIME + " TEXT, "
                + ALARM_OFFSET + " TEXT, "
                + TRIGGER_DAYS + " Text"
                + ");";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    /*

    TODO: THE FOUR FOLLOWING METHODS ARE CURRENTLY UNTESTED

     */


    //Inserts this alarm into the database, assuming we've already checked to make sure DB doesn't contain it.
    public void addAlarm(AlarmInfo alarm) {
        ContentValues values = new ContentValues();
        values.put(ALARM_NAME, alarm.getAlarmName());
        values.put(ALARM_TIME, alarm.getAlarmTime());
        values.put(ALARM_OFFSET, alarm.getOffsetTime());
        values.put(TRIGGER_DAYS, alarm.getTriggerString());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    //Searches database for the alarm with the given name, returning its details if found or null otherwise.
    public AlarmInfo getAlarmInfo(String alarmName){
        SQLiteDatabase db = getReadableDatabase();
        AlarmInfo retrievedAlarm = null;

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ALARM_NAME + " = '" + alarmName + "'", null);

        if(cursor.moveToFirst()){
            String retrievedName = cursor.getString(cursor.getColumnIndex(ALARM_NAME));
            String retrievedTime = cursor.getString(cursor.getColumnIndex(ALARM_TIME));
            String retrievedOffset = cursor.getString(cursor.getColumnIndex(ALARM_OFFSET));
            String retrievedTriggerDays = cursor.getString(cursor.getColumnIndex(TRIGGER_DAYS));

            retrievedAlarm = new AlarmInfo(retrievedName, retrievedTime, retrievedOffset, retrievedTriggerDays);
        }

        cursor.close();
        return retrievedAlarm;
    }

    //Searches for and deletes the provide alarm from the database if it is present. Otherwise, does nothing.
    public void deleteAlarm(AlarmInfo alarm){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + ALARM_NAME + " = '" + alarm.getAlarmName() + "'");
    }

    //Searches database to check if alarm with the given name exists already, returning true if so, false otherwise.
    public boolean alarmExistsInDB(String alarmName){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ALARM_NAME + " = '" + alarmName + "'", null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    public ArrayList<AlarmInfo> getAllAlarms(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<AlarmInfo> retrievedAlarms = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);

        while(cursor.moveToNext()){
            String retrievedName = cursor.getString(cursor.getColumnIndex(ALARM_NAME));
            String retrievedTime = cursor.getString(cursor.getColumnIndex(ALARM_TIME));
            String retrievedOffset = cursor.getString(cursor.getColumnIndex(ALARM_OFFSET));
            String retrievedTriggerDays = cursor.getString(cursor.getColumnIndex(TRIGGER_DAYS));

            AlarmInfo found = new AlarmInfo(retrievedName, retrievedTime, retrievedOffset, retrievedTriggerDays);
            retrievedAlarms.add(found);
        }

        cursor.close();
        return retrievedAlarms;
    }

}
