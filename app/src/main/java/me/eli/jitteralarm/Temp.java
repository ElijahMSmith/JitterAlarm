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

public class Temp extends AppCompatActivity {

    private CurrentAlarms currentAlarmsFrag;

    private SwitchMaterial sundaySwitch;
    private SwitchMaterial mondaySwitch;
    private SwitchMaterial tuesdaySwitch;
    private SwitchMaterial wednesdaySwitch;
    private SwitchMaterial thursdaySwitch;
    private SwitchMaterial fridaySwitch;
    private SwitchMaterial saturdaySwitch;

    private EditText editAlarmName;
    private EditText editAlarmTime;
    private EditText editAlarmOffset;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        db = new DatabaseHelper(getApplicationContext());

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager= findViewById(R.id.pager);

        tabLayout.addTab(tabLayout.newTab().setText("Current Alarms"));
        tabLayout.addTab(tabLayout.newTab().setText("New Alarm"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final FragPageAdapter fragPageAdapter = new FragPageAdapter(getSupportFragmentManager());
        fragPageAdapter.addFragment(new CurrentAlarms(db));
        fragPageAdapter.addFragment(new NewAlarm());
        viewPager.setAdapter(fragPageAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        currentAlarmsFrag = (CurrentAlarms) fragPageAdapter.getItem(0);
    }

    //Creates a new alarm based on the info on the screen.
    //If the alarm already exists in the database (checked by alarm name), don't add it and tell them what happened.
    //If the alarm doesn't already exist, create a new AlarmInfo object and submit that to the database.
    //Finish by clearing all the data in the fields.
    public void createAlarm(View v){

        //TODO: CHECK FOR VALID INPUTS, CONVERT TIME CHECKS TO TIME PICKERS?

        if(sundaySwitch == null)
            initializeViews();

        boolean[] toggles = getSwitchData(); //{sundaySwitch.isChecked(), ...., saturdaySwitch.isChecked()}
        String[] blanks = getFormData(); //{alarmName, alarmTime, alarmOffset}
        AlarmInfo newAlarm = new AlarmInfo(blanks[0], blanks[1], blanks[2], toggles);

        if(!db.alarmExistsInDB(newAlarm.getAlarmName())){
            db.addAlarm(newAlarm);

            /*

            TODO: Start the alarm before adding it to the database

             */

            Toast.makeText(getApplicationContext(), "Successfully Added!", Toast.LENGTH_SHORT).show();
            currentAlarmsFrag.updateAdapter();
            clearData(v);
        } else {
            //Send a message to the user to let them know that already exists!
            //Eventually should be a dialog, will just send a Toast for now
            Toast.makeText(getApplicationContext(), "That Alarm Name Already Exists!", Toast.LENGTH_LONG).show();
        }
    }

    protected boolean[] getSwitchData(){
        return new boolean[]{sundaySwitch.isChecked(), mondaySwitch.isChecked(),
                tuesdaySwitch.isChecked(), wednesdaySwitch.isChecked(),
                thursdaySwitch.isChecked(), fridaySwitch.isChecked(), saturdaySwitch.isChecked()};
    }

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