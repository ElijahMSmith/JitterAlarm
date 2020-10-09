package me.eli.jitteralarm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;

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
        fragPageAdapter.addFragment(new CurrentAlarms(db, getSupportFragmentManager()));
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

        //TODO: CHECK FOR VALID INPUTS, CONVERT TIME CHECKS TO TIME PICKERS?

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
        AlarmInfo newAlarm = new AlarmInfo(blanks[0], blanks[1], blanks[2], toggles);

        //If the alarm is NOT already in the DB, add it and refresh the adapter, then clear the form
        if(!db.alarmExistsInDB(newAlarm.getAlarmName())){
            db.addAlarm(newAlarm);

            /*

            TODO: Start the alarm before adding it to the database

             */

            Toast.makeText(getApplicationContext(), "Successfully Added!", Toast.LENGTH_SHORT).show();
            currentAlarmsFrag.updateAdapter();
            clearData(v);
        } else {
            //Otherwise, the word IS already in the db
            //Send a message to the user to let them know that already exists
            Toast.makeText(getApplicationContext(), "That Alarm Name Already Exists!", Toast.LENGTH_LONG).show();
        }
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

}