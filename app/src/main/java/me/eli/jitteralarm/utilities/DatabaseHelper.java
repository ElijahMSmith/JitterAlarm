package me.eli.jitteralarm.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Collections;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Store constants holding info about this database
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "alarms.db";
    private static final String TABLE_NAME = "Alarms_Table";
    private static final String ALARM_NAME = "Alarm_Name";
    private static final String ALARM_TIME = "Alarm_Time";
    private static final String ALARM_OFFSET = "Alarm_Offset";
    private static final String TRIGGER_DAYS = "Trigger_Days";
    private static final String NEXT_TRIGGER_DATE = "Next_Trigger_Date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Creates the database for the first time when the app is installed
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + "("
                + ALARM_NAME + " TEXT, "
                + ALARM_TIME + " TEXT, "
                + ALARM_OFFSET + " TEXT, "
                + NEXT_TRIGGER_DATE + " TEXT, "
                + TRIGGER_DAYS + " TEXT"
                + ");";
        db.execSQL(query);
    }

    //If we update to a new DATABASE_VERSION, replaces old database with new database
    //Likely will not come into use with this application
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Inserts this alarm into the database, assuming we've already checked to make sure DB doesn't contain it.
    public void addAlarm(AlarmInfo alarm) {
        //Set up a ContentValues object to store a table listing for this alarm's properties
        ContentValues values = new ContentValues();
        values.put(ALARM_NAME, alarm.getAlarmName());
        values.put(ALARM_TIME, alarm.getAlarmTime());
        values.put(ALARM_OFFSET, alarm.getOffsetTime());
        values.put(NEXT_TRIGGER_DATE, alarm.getNextTriggerDate());
        values.put(TRIGGER_DAYS, alarm.getTriggerString());

        //Insert this object into the database
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    //Searches database for the alarm with the given name, returning it if found or null otherwise.
    public AlarmInfo getAlarmInfo(String alarmName){
        SQLiteDatabase db = getReadableDatabase();
        AlarmInfo retrievedAlarm = null;

        //Makes query for possible matching alarms (only one alarm should ever match if we're doing things right)
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ALARM_NAME + " = '" + alarmName + "'", null);

        //If this alarm was found, convert into AlarmInfo form
        if(cursor.moveToFirst()){
            String retrievedName = cursor.getString(cursor.getColumnIndex(ALARM_NAME));
            String retrievedTime = cursor.getString(cursor.getColumnIndex(ALARM_TIME));
            String retrievedOffset = cursor.getString(cursor.getColumnIndex(ALARM_OFFSET));
            String retrievedNextTriggerDate = cursor.getString(cursor.getColumnIndex(NEXT_TRIGGER_DATE));
            String retrievedTriggerDays = cursor.getString(cursor.getColumnIndex(TRIGGER_DAYS));

            //Create alarm from this information
            retrievedAlarm = new AlarmInfo(retrievedName, retrievedTime, retrievedOffset, retrievedNextTriggerDate, retrievedTriggerDays);
        }

        //Return the information about this alarm, or null if we didn't find any matches
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

        //Queries to check if the alarm exists in the database, returning true if there is any match is found
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ALARM_NAME + " = '" + alarmName + "'", null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
    }

    //Returns an ArrayList containing all alarms in the database
    public ArrayList<AlarmInfo> getAllAlarms(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<AlarmInfo> retrievedAlarms = new ArrayList<>();

        //Obtain all table rows, each containing data for an alarm.
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);

        //For each row, create an AlarmInfo object and add it to the list
        while(cursor.moveToNext()){
            String retrievedName = cursor.getString(cursor.getColumnIndex(ALARM_NAME));
            String retrievedTime = cursor.getString(cursor.getColumnIndex(ALARM_TIME));
            String retrievedOffset = cursor.getString(cursor.getColumnIndex(ALARM_OFFSET));
            String retrievedNextTriggerDate = cursor.getString(cursor.getColumnIndex(NEXT_TRIGGER_DATE));
            String retrievedTriggerDays = cursor.getString(cursor.getColumnIndex(TRIGGER_DAYS));

            AlarmInfo found = new AlarmInfo(retrievedName, retrievedTime, retrievedOffset, retrievedNextTriggerDate, retrievedTriggerDays);
            retrievedAlarms.add(found);
        }

        //Return our full list
        cursor.close();
        //Sort our list (comparator specified in AlarmInfo sorts by name alphabetically)
        Collections.sort(retrievedAlarms);
        //Return our sorted collection
        return retrievedAlarms;
    }

}
