package me.eli.jitteralarm.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import me.eli.jitteralarm.R;
import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.DatabaseHelper;

public class NewAlarm extends Fragment implements View.OnClickListener {

    private EditText editAlarmName;
    private EditText editAlarmTime;
    private EditText editAlarmOffset;

    private SwitchMaterial sundaySwitch;
    private SwitchMaterial mondaySwitch;
    private SwitchMaterial tuesdaySwitch;
    private SwitchMaterial wednesdaySwitch;
    private SwitchMaterial thursdaySwitch;
    private SwitchMaterial fridaySwitch;
    private SwitchMaterial saturdaySwitch;

    private Context context;
    private DatabaseHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_alarm, container, false);
        editAlarmName = rootView.findViewById(R.id.editAlarmName);
        editAlarmTime = rootView.findViewById(R.id.editAlarmTime);
        editAlarmOffset = rootView.findViewById(R.id.editOffset);

        sundaySwitch = rootView.findViewById(R.id.sundaySwitch);
        mondaySwitch = rootView.findViewById(R.id.mondaySwitch);
        tuesdaySwitch = rootView.findViewById(R.id.tuesdaySwitch);
        wednesdaySwitch = rootView.findViewById(R.id.wednesdaySwitch);
        thursdaySwitch = rootView.findViewById(R.id.thursdaySwitch);
        fridaySwitch = rootView.findViewById(R.id.fridaySwitch);
        saturdaySwitch = rootView.findViewById(R.id.saturdaySwitch);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        helper = new DatabaseHelper(context);
    }

    //Sets all blanks on the screen to null and switches to off
    public void clearData() {
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

    //Creates a new alarm based on the info on the screen.
    //If the alarm already exists in the database (checked by alarm name), don't add it and tell them what happened.
    //If the alarm doesn't already exist, create a new AlarmInfo object and submit that to the database.
        //Finish by clearing all the data in the fields.
    public void createAlarm(){
        boolean[] toggles = new boolean[]{sundaySwitch.isChecked(), mondaySwitch.isChecked(), tuesdaySwitch.isChecked(), wednesdaySwitch.isChecked(), thursdaySwitch.isChecked(), fridaySwitch.isChecked(), saturdaySwitch.isChecked()};
        AlarmInfo newAlarm = new AlarmInfo(editAlarmName.getText().toString(), editAlarmTime.getText().toString(), editAlarmOffset.getText().toString(), toggles);
        if(!helper.alarmExistsInDB(newAlarm.getAlarmName())){
            helper.addAlarm(newAlarm);

            /*

            TODO: Start the alarm before adding it to the database

             */

            Toast.makeText(context, "Successfully Added!", Toast.LENGTH_SHORT).show();
            clearData();
        } else {
            //Send a message to the user to let them know that already exists!
            //Eventually should be a dialog, will just send a Toast for now
            Toast.makeText(context, "That Alarm Already Exists!", Toast.LENGTH_LONG).show();
        }
    }

    //TODO: These buttons don't function, probably because the buttons are still hooked up to Main for some reason

    //Checks for button presses and performs appropriate actions
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearDataButton:
                clearData();
                break;
            case R.id.setAlarmButton:
                createAlarm();
                break;
        }
    }
}