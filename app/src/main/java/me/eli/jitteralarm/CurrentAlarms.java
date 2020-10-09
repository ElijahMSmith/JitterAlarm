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

    private RecyclerView recycler;
    private AlarmsAdapter adapter;
    private DatabaseHelper db;
    private FragmentManager supportFragManager;
    private Context context;

    public CurrentAlarms(DatabaseHelper db, FragmentManager supportFragManager){
        this.db = db;
        this.supportFragManager = supportFragManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current_alarms, container, false);

        recycler = rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());

        // Set layout manager to position the items
        recycler.setLayoutManager(layoutManager);

        // Create adapter passing in existing alarms
        adapter = new AlarmsAdapter(db, this);

        // Attach the adapter to the recyclerview to populate items
        recycler.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        this.context = context;
    }

    public void openEditDialog(AlarmInfo alarmToEdit){
        DialogFragment editAlarmFragment = new AlarmDetailsDialogFragment(alarmToEdit, db, this);
        editAlarmFragment.show(supportFragManager, "editAlarmInformation");
    }

    @SuppressLint("InflateParams")
    protected void confirmDeletion(final AlarmInfo alarmToDelete){

        //Have a method in temp that I call from here create the confirmation dialog (pass it the originalState alarm)
        //That dialog will have cancel/delete only, cancelling does nothing, deleting removes originalState from db.
        //Make sure to present which alarm they are deleting (not the one they are editing)

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogRootView = inflater.inflate(R.layout.custom_confirmation_dialog, null);
        builder.setView(dialogRootView)
        .setTitle("CONFIRM DELETION OF THIS ALARM")
        // Add action buttons
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

        ((TextView)dialogRootView.findViewById(R.id.confirmTitle)).setText(alarmToDelete.getAlarmName());
        ((TextView)dialogRootView.findViewById(R.id.confirmTime)).setText(alarmToDelete.getAlarmTime());
        ((TextView)dialogRootView.findViewById(R.id.confirmOffset)).setText(alarmToDelete.getOffsetTime());

        //Convert trigger days into textview form
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

        ((TextView)dialogRootView.findViewById(R.id.confirmTriggers)).setText(stringBuilder.toString());

        AlertDialog confirmAlert = builder.create();
        confirmAlert.show();
    }


    public void updateAdapter(){
        adapter.updateAlarmSet();
    }

    public void deleteAlarm(AlarmInfo alarm){
        //Deletes alarm from the list and from the db, position it was in is returned (-1 if not in list)
        int position = adapter.removeAlarm(alarm);
        if(position >= 0){
            recycler.removeViewAt(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, adapter.getAlarmSetSize());
        }

    }
}