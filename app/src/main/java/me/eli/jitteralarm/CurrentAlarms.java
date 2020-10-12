package me.eli.jitteralarm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.eli.jitteralarm.R;
import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.AlarmsAdapter;
import me.eli.jitteralarm.utilities.DatabaseHelper;

public class CurrentAlarms extends Fragment {

    //All references we're going to need later
    private RecyclerView recycler;
    private AlarmsAdapter adapter;
    private DatabaseHelper db;
    private FragmentManager supportFragManager;
    private MainActivity mainActivity;
    private Context context;

    //Store the DB helper created in the activity and the SupportFragmentManager also retrieved from the activity
    public CurrentAlarms(DatabaseHelper db, FragmentManager supportFragManager, MainActivity mainActivity){
        this.db = db;
        this.supportFragManager = supportFragManager;
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Create the fragment
        View rootView = inflater.inflate(R.layout.fragment_current_alarms, container, false);

        //Set up the recycler view that will hold alarms
        recycler = rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());

        // Set layout manager to position the items
        recycler.setLayoutManager(layoutManager);

        // Create adapter, passing in database access and a reference back here
        adapter = new AlarmsAdapter(db, this);

        // Attach the adapter to the recyclerview to populate items
        recycler.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        this.context = context; //Store context for later
    }

    //Called by edit button in an adapter listing with the alarm to be edited
    //Opens an AlertDialog that allows user to change values of that alarm then submit, cancel, or delete them.
    public void openEditDialog(AlarmInfo alarmToEdit){
        DialogFragment editAlarmFragment = new AlarmDetailsDialogFragment(alarmToEdit, db, this);
        editAlarmFragment.show(supportFragManager, "editAlarmInformation");
    }

    //Opens another dialog to confirm the user wants to delete the alarm (from the edit dialog)
    //May be removed at a later date if it's more of a waste than I expect it will be.
    @SuppressLint("InflateParams")
    protected void confirmDeletion(final AlarmInfo alarmToDelete){

        //Creates a new alert dialog to display alarm information (immutable)
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        View dialogRootView = inflater.inflate(R.layout.custom_confirmation_dialog, null);
        builder.setView(dialogRootView) //Set up dialog from here on
        .setTitle("CONFIRM DELETION OF THIS ALARM")
        .setPositiveButton(R.string.confirmDeletion, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Delete from everything
                deleteAlarm(alarmToDelete);
                dialog.dismiss();
                Toast.makeText(context, "Alarm Deleted", Toast.LENGTH_SHORT).show();
            }
        })
        .setNegativeButton(R.string.cancelChanges, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel(); //No changes!
            }
        });

        //Set views in our layout to be alarm values
        ((TextView)dialogRootView.findViewById(R.id.confirmTitle)).setText(alarmToDelete.getAlarmName());
        ((TextView)dialogRootView.findViewById(R.id.confirmTime)).setText(alarmToDelete.getAlarmTime());
        ((TextView)dialogRootView.findViewById(R.id.confirmOffset)).setText(alarmToDelete.getOffsetTime());

        //Convert trigger days into the form a TextView can display
        boolean[] triggers = alarmToDelete.getTriggerArray();
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < triggers.length; i++){
            switch(i){
                case 0:
                    stringBuilder.append(!triggers[i] ? "" : "Sunday, ");
                    break;
                case 1:
                    stringBuilder.append(!triggers[i] ? "" : "Monday, ");
                    break;
                case 2:
                    stringBuilder.append(!triggers[i] ? "" : "Tuesday, ");
                    break;
                case 3:
                    stringBuilder.append(!triggers[i] ? "" : "Wednesday, ");
                    break;
                case 4:
                    stringBuilder.append(!triggers[i] ? "" : "Thursday, ");
                    break;
                case 5:
                    stringBuilder.append(!triggers[i] ? "" : "Friday, ");
                    break;
                case 6:
                    stringBuilder.append(!triggers[i] ? "" : "Saturday, ");
                    break;
            }
        }

        //Will either be >2 (some amount of days with trailing comma and space) or will be 0 (if dormant alarm with no days set, empty string)
        if(stringBuilder.length() > 2)
            stringBuilder.setLength(stringBuilder.length() - 2);
        else //If dormant alarm, add that info instead
                stringBuilder.append("This alarm is currently not running on any days.");

        //Last one
        ((TextView)dialogRootView.findViewById(R.id.confirmTriggers)).setText(stringBuilder.toString());

        //Create and show dialog
        AlertDialog confirmAlert = builder.create();
        confirmAlert.show();
    }

    //Called from everywhere, forces the adapter to refresh its contents any time a change is made from somewhere else
    public void updateAdapter(){
        adapter.updateAlarmSet();
    }

    //Delete an alarm from the system, removing it from both the database and from the adapter.
    public void deleteAlarm(AlarmInfo alarm){
        //Deletes alarm from the list and from the db, position it was in is returned (-1 if not in list)
        int position = adapter.removeAlarm(alarm);

        //If the alarm WAS in the alarm set, remove it from the entire RecyclerView as well
        if(position >= 0){
            recycler.removeViewAt(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, adapter.getAlarmSetSize());
        }

    }

    //Validates inputs to check if they are all valid.
    //If everything is acceptable, return true
    //If something is amiss, return false and print to the user what's wrong.
    //submittingNew is true if alarm is being created for the first time
        //submittingNew is false if the user is editing an alarm and we want to validate their new details
    public boolean validateAlarm(String alarmName, String alarmTime, String alarmOffset, boolean submittingNew){
        alarmName = alarmName.trim();
        alarmTime = alarmTime.trim();
        alarmOffset = alarmOffset.trim();

        //Checks alarmName against existing db alarms
        //If we're submitting for the first time, we want to check for existing alarms of same name
        //If we're editing this alarm, of course the original is going to exist, so we skip this step
        if(submittingNew && db.alarmExistsInDB(alarmName)){
            Toast.makeText(context, "This alarm already exists!", Toast.LENGTH_LONG).show();
            return false;
        }

        //Validate alarmTime
        String[] splitTime = alarmTime.split("[: ]");
        if(splitTime.length != 3){
            Toast.makeText(context, "Please use the format HH:MM AM/PM for alarm time", Toast.LENGTH_LONG).show();
            return false;
        }
        int hours, minutes;
        try{
            hours = Integer.parseInt(splitTime[0]);
            minutes = Integer.parseInt(splitTime[1]);
        } catch(NumberFormatException E){
            Toast.makeText(context, "Numeric inputs expected for alarm time", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(hours < 1 || hours > 12){
            Toast.makeText(context, "Alarm time hour invalid. Accepted values: 1-12", Toast.LENGTH_LONG).show();
            return false;
        }
        if(minutes < 0 || minutes >= 60){
            Toast.makeText(context, "Alarm time minutes invalid. Accepted values: 0-59", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!(splitTime[2].equalsIgnoreCase("am") || splitTime[2].equalsIgnoreCase("pm"))){
            Toast.makeText(context, "Please specify AM/PM alarm time", Toast.LENGTH_LONG).show();
            return false;
        }

        //Validate alarmOffset
        splitTime = alarmOffset.split(":");
        if(splitTime.length != 3){
            Toast.makeText(context, "Please use the format HH:MM:SS for alarm offset", Toast.LENGTH_LONG).show();
            return false;
        }
        int offsetHours, offsetMinutes, offsetSeconds;
        try{
            offsetHours = Integer.parseInt(splitTime[0]);
            offsetMinutes = Integer.parseInt(splitTime[1]);
            offsetSeconds = Integer.parseInt(splitTime[2]);
        } catch(NumberFormatException E){
            Toast.makeText(context, "Numeric inputs expected for alarm offset", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(offsetHours < 0 || offsetHours > 24){
            Toast.makeText(context, "Alarm offset hour invalid. Accepted values: 0-24", Toast.LENGTH_LONG).show();
            return false;
        }
        if(offsetMinutes < 0 || offsetMinutes >= 60){
            Toast.makeText(context, "Alarm offset minutes invalid. Accepted values: 0-59", Toast.LENGTH_LONG).show();
            return false;
        }
        if(offsetSeconds < 0 || offsetSeconds >= 60){
            Toast.makeText(context, "Alarm offset seconds invalid. Accepted values: 0-59", Toast.LENGTH_LONG).show();
            return false;
        }

        //We're not going to validate the trigger array. If they want to leave every day off, it effectively disables the alarm, which is fine with us.
        //Also not validating nextTriggerDate since that can only be valid if the rest of the info is valid

        //Return our valid alarm
        return true;
    }

    //Used by AlarmDetailsDialogFragment to set the updated version of an alarm
    public void startAlarm(AlarmInfo editedAlarm){
        mainActivity.startAlarm(editedAlarm);
    }
}