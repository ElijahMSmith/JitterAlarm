package me.eli.jitteralarm;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.DatabaseHelper;

public class AlarmDetailsDialogFragment extends DialogFragment {

    //Storing original alarm, db access, and CurrentAlarms fragment to return to later
    private AlarmInfo originalState;
    private DatabaseHelper db;
    private CurrentAlarms alarmsList;

    public AlarmDetailsDialogFragment(AlarmInfo alarmToEdit, DatabaseHelper db, CurrentAlarms alarmsList){
        originalState = alarmToEdit;
        this.db = db;
        this.alarmsList = alarmsList;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Set up fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.custom_dialog, null);

        //Get reference to components
        final SwitchMaterial dfSundaySwitch = rootView.findViewById(R.id.dfSundaySwitch);
        final SwitchMaterial dfMondaySwitch = rootView.findViewById(R.id.dfMondaySwitch);
        final SwitchMaterial dfTuesdaySwitch = rootView.findViewById(R.id.dfTuesdaySwitch);
        final SwitchMaterial dfWednesdaySwitch = rootView.findViewById(R.id.dfWednesdaySwitch);
        final SwitchMaterial dfThursdaySwitch = rootView.findViewById(R.id.dfThursdaySwitch);
        final SwitchMaterial dfFridaySwitch = rootView.findViewById(R.id.dfFridaySwitch);
        final SwitchMaterial dfSaturdaySwitch = rootView.findViewById(R.id.dfSaturdaySwitch);
        final EditText dfNameInput = rootView.findViewById(R.id.dfNameInput);
        final EditText dfTimeInput = rootView.findViewById(R.id.dfTimeInput);
        final EditText dfOffsetInput = rootView.findViewById(R.id.dfOffsetInput);

        //Set components with originalState data
        boolean[] originalTriggers = originalState.getTriggerArray();
        dfSundaySwitch.setChecked(originalTriggers[0]);
        dfMondaySwitch.setChecked(originalTriggers[1]);
        dfTuesdaySwitch.setChecked(originalTriggers[2]);
        dfWednesdaySwitch.setChecked(originalTriggers[3]);
        dfThursdaySwitch.setChecked(originalTriggers[4]);
        dfFridaySwitch.setChecked(originalTriggers[5]);
        dfSaturdaySwitch.setChecked(originalTriggers[6]);

        dfNameInput.setText(originalState.getAlarmName());
        dfTimeInput.setText(originalState.getAlarmTime());
        dfOffsetInput.setText(originalState.getOffsetTime());

        //Set DialogBuilder to hold our complete layout
        builder.setView(rootView)
        .setMessage(R.string.editAlarm) //Set properties of dialog
        .setPositiveButton(R.string.updateAlarm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //TODO: Still need to check for alarm validity

                //Take all data for this alarm, changed or not, and compile it into an AlarmInfo object
                boolean[] newTriggers = {dfSundaySwitch.isChecked(),
                                        dfMondaySwitch.isChecked(),
                                        dfTuesdaySwitch.isChecked(),
                                        dfWednesdaySwitch.isChecked(),
                                        dfThursdaySwitch.isChecked(),
                                        dfFridaySwitch.isChecked(),
                                        dfSaturdaySwitch.isChecked()};
                AlarmInfo withEdits = new AlarmInfo(dfNameInput.getText().toString(),
                                                    dfTimeInput.getText().toString(),
                                                    dfOffsetInput.getText().toString(),
                                                    newTriggers);

                //If the new alarm is identical to the original alarm, nothing is changed
                if(withEdits.isIdenticalTo(originalState)){
                    //No changes made, don't submit to db
                    dialog.dismiss();
                    Toast.makeText(getContext(), "No changes were made!", Toast.LENGTH_SHORT).show();
                } else { //Otherwise, need to replace old alarm with new one

                    //TODO: When implementing alarms running, will need to cancel the original alarm (which should be running)

                    //Take out originalState from db
                    alarmsList.deleteAlarm(originalState);

                    //Add new form to the database
                    db.addAlarm(withEdits);
                    //Refresh the adapter
                    alarmsList.updateAdapter();
                    Toast.makeText(rootView.getContext(), "Alarm Updated", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss(); //Now we exit the dialog
            }
        })
        .setNeutralButton(R.string.cancelChanges, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //If they don't want to proceed, cancel out of the dialog
                dialog.cancel();
            }
        })
        .setNegativeButton(R.string.deleteAlarm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //User wants to delete originalState alarm. Exit dialog and return to CurrentAlarms fragment to delete
                dialog.dismiss();
                alarmsList.confirmDeletion(originalState);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
