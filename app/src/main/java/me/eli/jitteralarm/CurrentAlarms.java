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
    private Context context;

    //Store the DB helper created in the activity and the SupportFragmentManager also retrieved from the activity
    public CurrentAlarms(DatabaseHelper db, FragmentManager supportFragManager){
        this.db = db;
        this.supportFragManager = supportFragManager;
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
                    stringBuilder.append("Sunday, ");
                    break;
                case 1:
                    stringBuilder.append("Monday, ");
                    break;
                case 2:
                    stringBuilder.append("Tuesday, ");
                    break;
                case 3:
                    stringBuilder.append("Wednesday, ");
                    break;
                case 4:
                    stringBuilder.append("Thursday, ");
                    break;
                case 5:
                    stringBuilder.append("Friday, ");
                    break;
                case 6:
                    stringBuilder.append("Saturday, ");
                    break;
            }
        }

        //Will either be >2 or will be 0 (no days added, which should be filtered out eventually. TODO)
        if(stringBuilder.length() > 2)
            stringBuilder.setLength(stringBuilder.length() - 2);
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
}