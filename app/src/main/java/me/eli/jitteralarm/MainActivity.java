package me.eli.jitteralarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

import me.eli.jitteralarm.receivers.AlarmReceiver;
import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.DatabaseHelper;
import me.eli.jitteralarm.utilities.FragPageAdapter;

public class MainActivity extends AppCompatActivity {

    //Reference to fragment with updating alarm list
    //No reference to New Alarm frag needed at this time
    private CurrentAlarms currentAlarmsFrag;

    //Quick reference to switches on New Alarm fragment
    private SwitchMaterial sundaySwitch;
    private SwitchMaterial mondaySwitch;
    private SwitchMaterial tuesdaySwitch;
    private SwitchMaterial wednesdaySwitch;
    private SwitchMaterial thursdaySwitch;
    private SwitchMaterial fridaySwitch;
    private SwitchMaterial saturdaySwitch;

    //Quick access to text fields on New Alarm fragment
    private EditText editAlarmName;
    private EditText editAlarmTime;
    private EditText editAlarmOffset;

    //Access to database
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(getApplicationContext());

        //Set up our tab layout
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager= findViewById(R.id.pager);
        tabLayout.addTab(tabLayout.newTab().setText("Current Alarms"));
        tabLayout.addTab(tabLayout.newTab().setText("New Alarm"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //Set up page adapter to hold and navigate between fragments
        final FragPageAdapter fragPageAdapter = new FragPageAdapter(getSupportFragmentManager());
        fragPageAdapter.addFragment(new CurrentAlarms(db, getSupportFragmentManager(), this));
        fragPageAdapter.addFragment(new NewAlarm());
        viewPager.setAdapter(fragPageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //Set up tab navigation
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            //Unused methods at this time
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        //Gets the reference to the Current Alarms fragment once it's been created
        currentAlarmsFrag = (CurrentAlarms) fragPageAdapter.getItem(0);
    }

    //Creates a new alarm based on the info on the New Alarm fragment.
    //If the alarm already exists in the database (checked by alarm name), we don't add it again.
    //If the alarm doesn't already exist, we create a new AlarmInfo object and submit that to the database.
    //Finish by clearing all the data in the fields.
    public void createAlarm(View v){
        //If we haven't already gotten a reference to the switches, do it here
        //They're guaranteed to be available now
        //The only reason I have this here (I know it looks hacky) was because
            // in the several locations I put the code to get switch references,
            // each one couldn't find the switches and returned null.
            // Instead, we grab them once we need them for the first time.
        if(sundaySwitch == null)
            initializeViews();

        //Create a new alarm from the information on the form
        boolean[] toggles = getSwitchData(); //{sundaySwitch.isChecked(), ...., saturdaySwitch.isChecked()}
        String[] blanks = getFormData(); //{alarmName, alarmTime, alarmOffset}
        boolean valid = currentAlarmsFrag.validateAlarm(blanks[0], blanks[1], blanks[2], true);

        //Created successfully
        if(valid){
            //Setup alarm and start it running
            AlarmInfo newAlarm = new AlarmInfo(blanks[0], blanks[1], blanks[2], toggles);
            startAlarm(newAlarm);

            //Now we can add the alarm to the db and clear the form
            db.addAlarm(newAlarm);
            Toast.makeText(getApplicationContext(), "Successfully Added!", Toast.LENGTH_SHORT).show();
            currentAlarmsFrag.updateAdapter();
            clearData(v);
        }
        //If the alarm was invalid for some reason, we already sent out our explanation and are finished
    }

    //Starts running a provided alarm, as long as it isn't set as dormant (not suppose to run, no days specified)
    public void startAlarm(AlarmInfo alarmToStart){

        //Creates new offset alarm structure
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("name", alarmToStart.getAlarmName()); //All the identifying information we need. Receiver can look up alarm to reset it.
        int requestCode = alarmNameHash(alarmToStart.getAlarmName());
        alarmIntent.putExtra("requestCode", requestCode); //All the identifying information we need. Receiver can look up alarm to reset it.
        //This request code is guaranteed to be unique for each alarm, no alarms will override each other
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmToStart.getCalendarFromNextTriggerDate().getTimeInMillis(), pendingIntent); //Set alarm to run at exact time of next trigger date

        //Log successfully set alarms for testing purposes
        Log.d("test", "-------------------------------------------");
        Log.d("test", "Started alarm '" + alarmToStart.toString() + "' with request code '" + requestCode + "' that will next trigger at " + alarmToStart.getNextTriggerDate());
        Log.d("test", "-------------------------------------------");

    }

    //Computes a request code (hash) unique to this alarm name
    private int alarmNameHash(String alarmName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        if(sp.contains(alarmName)) //If we've already defined a request code for this alarm name, use that
            return sp.getInt(alarmName, -1);

        //Otherwise, we need to generate a new request code
        SharedPreferences.Editor editor = sp.edit();

        //Generate hash value
        int hash = 0;
        for(char c : alarmName.toCharArray()){
            int add = (int) Math.pow((int)c, 2);
            hash += add;
            if(hash < 0)
                hash = hash - Integer.MIN_VALUE; //If we overflow, return to positive numbers by setting to difference with int min value
        }

        int power = 0;
        //While this has value is already used, keep adding using quadratic probing until we reach a unique hash
        while(sp.getBoolean(String.valueOf(hash), false)){
            hash += (int) Math.pow(2, power++);
            if(hash < 0)
                hash = hash - Integer.MIN_VALUE; //If we overflow, return to positive numbers by setting to difference with int min value
        }

        //Mark this hash as used
        editor.putBoolean(String.valueOf(hash), true);
        editor.putInt(alarmName, hash);
        editor.apply();

        //Return the hash for this alarm name
        return hash;
    }

    //Returns boolean values of the switch for each day
    protected boolean[] getSwitchData(){
        return new boolean[]{sundaySwitch.isChecked(), mondaySwitch.isChecked(),
                tuesdaySwitch.isChecked(), wednesdaySwitch.isChecked(),
                thursdaySwitch.isChecked(), fridaySwitch.isChecked(), saturdaySwitch.isChecked()};
    }

    //Returns all the strings in the text fields
    protected String[] getFormData(){
        return new String[]{editAlarmName.getText().toString(), editAlarmTime.getText().toString(), editAlarmOffset.getText().toString()};
    }

    //Sets all blanks on the screen to null and switches to off
    public void clearData(View v) {
        if(sundaySwitch == null)
            initializeViews();

        editAlarmName.setText("");
        editAlarmTime.setText("");
        editAlarmOffset.setText("");

        sundaySwitch.setChecked(false);
        mondaySwitch.setChecked(false);
        tuesdaySwitch.setChecked(false);
        wednesdaySwitch.setChecked(false);
        thursdaySwitch.setChecked(false);
        fridaySwitch.setChecked(false);
        saturdaySwitch.setChecked(false);
    }

    //Because of weird fragment attaching way later than onCreate,
        // the only workaround I could find to getting references was to do it on button presses (if it hadn't been done yet before)
        // I know it looks weird, but so is Android sometimes.
    private void initializeViews(){
        sundaySwitch = findViewById(R.id.sundaySwitch);
        mondaySwitch = findViewById(R.id.mondaySwitch);
        tuesdaySwitch = findViewById(R.id.tuesdaySwitch);
        wednesdaySwitch = findViewById(R.id.wednesdaySwitch);
        thursdaySwitch = findViewById(R.id.thursdaySwitch);
        fridaySwitch = findViewById(R.id.fridaySwitch);
        saturdaySwitch = findViewById(R.id.saturdaySwitch);

        editAlarmName = findViewById(R.id.editAlarmName);
        editAlarmTime = findViewById(R.id.editAlarmTime);
        editAlarmOffset = findViewById(R.id.editOffset);
    }

    //Called to cancel a running alarm that has been updated with new details
    public void cancelAlarm(AlarmInfo oldAlarm) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        //Store and remove from sp the originalState alarm's request code
        int requestCode = sp.getInt(oldAlarm.getAlarmName(), -1);
        sp.edit().remove(oldAlarm.getAlarmName()).apply();

        //Set up intent and pending intent to match alarm when it was set
        Intent cancelAlarm = new Intent(this, AlarmReceiver.class);
        cancelAlarm.putExtra("name", oldAlarm.getAlarmName());
        cancelAlarm.putExtra("requestCode", requestCode);

        //Cancel it
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, cancelAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        //For testing purposes
        Log.d("test", "-------------------------------------------");
        Log.d("test", "Canceled alarm '" + oldAlarm.getAlarmName() + "' with request code '" + requestCode + "' that was set to trigger at " + oldAlarm.getNextTriggerDate());
        Log.d("test", "-------------------------------------------");
    }
}