package me.eli.jitteralarm.utilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.eli.jitteralarm.CurrentAlarms;
import me.eli.jitteralarm.R;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<AlarmInfo> alarmSet;
    private DatabaseHelper helper;
    private CurrentAlarms alarmsListFrag;

    // Pass in the contact array into the constructor
    public AlarmsAdapter(DatabaseHelper helper, CurrentAlarms alarmsListFrag) {
        // Initialize with all existing alarms
        this.alarmsListFrag = alarmsListFrag;
        this.helper = helper;
        alarmSet = helper.getAllAlarms();
        Log.d("test", "Logging retrieved alarms");
        for(AlarmInfo alarm : alarmSet){
            Log.d("test", alarm.toString());
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public AlarmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        helper = new DatabaseHelper(context);

        // Inflate the custom layout
        View alarmView = inflater.inflate(R.layout.adapter_listing, parent, false);

        // Return a new holder instance
        return new ViewHolder(alarmView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(AlarmsAdapter.ViewHolder holder, final int position) {
        // Get the data model based on position
        AlarmInfo alarm = alarmSet.get(position);

        // Set item views based on your views and data model
        TextView alarmName = holder.listedAlarmName;
        alarmName.setText(alarm.getAlarmName());
        TextView alarmTime = holder.listedAlarmTime;
        alarmTime.setText(alarm.getAlarmTime());
        Button editButton = holder.editListedAlarmButton;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmsListFrag.openEditDialog(alarmSet.get(position));
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return alarmSet.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView listedAlarmName;
        public TextView listedAlarmTime;
        public Button editListedAlarmButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            listedAlarmName = itemView.findViewById(R.id.listedAlarmName);
            listedAlarmTime = itemView.findViewById(R.id.listedAlarmTime);
            editListedAlarmButton = itemView.findViewById(R.id.editListedAlarmButton);
        }
    }

    public void updateAlarmSet(){
        ArrayList<AlarmInfo> dbAlarms = helper.getAllAlarms();
        combineToAlarmSet(dbAlarms);
        notifyDataSetChanged();
    }

    private void combineToAlarmSet(ArrayList<AlarmInfo> retrievedSet){
        int alarmSetPosition = 0, retrievedSetPosition = 0, iterations = Math.max(alarmSet.size(), retrievedSet.size());

        //Runs until we've compared all alarms in the alarmSet
        while(alarmSetPosition < alarmSet.size()){
            if(retrievedSetPosition < retrievedSet.size()){ //Still alarms in retrievedSet to compare with
                AlarmInfo rsAlarm = retrievedSet.get(retrievedSetPosition);
                AlarmInfo asAlarm = alarmSet.get(alarmSetPosition);
                int compVal = rsAlarm.compareTo(asAlarm);

                if(compVal < 0){
                    //rsAlarm is smaller, needs to be inserted in front of asAlarm
                    //Insert, move one through both
                    alarmSet.add(alarmSetPosition, asAlarm);
                    alarmSetPosition++;
                    retrievedSetPosition++;
                } else if(compVal == 0){
                    //Same alarm, skip both
                    alarmSetPosition++;
                    retrievedSetPosition++;
                } else {
                    //rsAlarm doesn't fit in front of asAlarm, move one through alarmSet
                    alarmSetPosition++;
                }
            }
        }

        //More left in retrieved data set, add all remaining to alarmSet
        if(retrievedSetPosition < retrievedSet.size()){
            for(;retrievedSetPosition < retrievedSet.size(); retrievedSetPosition++){
                alarmSet.add(retrievedSet.get(retrievedSetPosition++)); //Adds then moves indexes after operation finishes
            }
        }
    }

    //Delete alarm from db, alarm list, and return position in the alarmSet for further action
    public int removeAlarm(AlarmInfo alarm){
        helper.deleteAlarm(alarm);
        int position = alarmSet.indexOf(alarm);
        alarmSet.remove(alarm);
        return position;
    }

    public int getAlarmSetSize(){
        return alarmSet.size();
    }

}