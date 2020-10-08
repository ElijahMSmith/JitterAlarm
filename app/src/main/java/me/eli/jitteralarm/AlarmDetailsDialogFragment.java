package me.eli.jitteralarm;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Objects;

import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.AlarmsAdapter;
import me.eli.jitteralarm.utilities.DatabaseHelper;

public class AlarmDetailsDialogFragment extends DialogFragment {

    private AlarmInfo originalState;
    private DatabaseHelper db;
    private AlarmsAdapter alarmsAdapter;

    public AlarmDetailsDialogFragment(AlarmInfo toEditAlarm, DatabaseHelper db, AlarmsAdapter alarmsAdapter){
        originalState = toEditAlarm;
        this.db = db;
        this.alarmsAdapter = alarmsAdapter;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.custom_dialog, null);

        //Get ref to components
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

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(rootView)
        .setMessage(R.string.editAlarm)
        .setPositiveButton(R.string.updateAlarm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO: Take alarm with previous name, remove it, add one with all details here
                    //TODO: Still need to check for alarm validity

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

                if(withEdits.isIdenticalTo(originalState)){
                    //No changes made, don't submit to db
                    dialog.dismiss();
                    Toast.makeText(getContext(), "No changes were made!", Toast.LENGTH_SHORT).show();
                } else {
                    //Take out originalState from db
                    //Insert withEdits to db
                    //Update alarmsAdapter
                    db.deleteAlarm(originalState);
                    db.addAlarm(withEdits);
                    alarmsAdapter.updateAlarmSet();
                }

                dialog.dismiss();
            }
        })
        .setNeutralButton(R.string.cancelChanges, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                //Do nothing more!
            }
        })
        .setNegativeButton(R.string.deleteAlarm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO: Dismiss this dialog, build another one with just a confirmation message and cancel/delete buttons


                //TODO: RESUME FROM HERE. Need to figure out what I'm going to do about confirmations.
                //Could pass a reference to Temp class in constructor, then after dismissing this dialog (no action),
                    //Have a method in temp that I call from here create the confirmation dialog (pass it the originalState alarm)
                    //That dialog will have cancel/delete only, cancelling does nothing, deleting removes originalState from db.
                    //Make sure to present which alarm they are deleting (not the one they are editing)


                //TODO: CREATE DIALOG IN TEMP WHEN EDIT BUTTON IN ADAPTER LISTING IS PRESSED
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
