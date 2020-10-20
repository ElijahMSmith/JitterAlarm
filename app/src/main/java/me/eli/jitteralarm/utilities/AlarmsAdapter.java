package me.eli.jitteralarm.utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.eli.jitteralarm.CurrentAlarms;
import me.eli.jitteralarm.R;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {

    //Stores the full set of alarms in our system, db access, and a reference to return to CurrentAlarms
    private List<AlarmInfo> alarmSet;
    private DatabaseHelper helper;
    private CurrentAlarms alarmsListFrag;

    //Pass in our references
    public AlarmsAdapter(DatabaseHelper helper, CurrentAlarms alarmsListFrag) {
        this.alarmsListFrag = alarmsListFrag;
        this.helper = helper;

        //Initialize alarmSet with all existing alarms
        alarmSet = helper.getAllAlarms();

        /*
        //For bug testing

        Log.d("test", "Logging retrieved alarms");
        for(AlarmInfo alarm : alarmSet){
            Log.d("test", alarm.toString());
        }
         */
    }

    @NonNull
    @Override
    public AlarmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        //Inflate our unique adapter listing layout
        View alarmView = inflater.inflate(R.layout.adapter_listing, parent, false);

        // Return a new holder instance for this listing
        return new ViewHolder(alarmView);
    }

    //Sets views in this ViewHolder to show data for the alarm it's representing
    @Override
    public void onBindViewHolder(AlarmsAdapter.ViewHolder holder, final int position) {
        //Get the AlarmInfo for the particular position
        AlarmInfo alarm = alarmSet.get(position);

        //Set this ViewHolder to show alarm details
        TextView alarmName = holder.listedAlarmName;
        alarmName.setText(alarm.getAlarmName());
        TextView alarmTime = holder.listedAlarmTime;
        alarmTime.setText(alarm.getAlarmTime());
        Button editButton = holder.editListedAlarmButton;
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set button to send us to the edit dialog AlarmDetailsDialogFragment through CurrentAlarms fragment method

                //Updates stored alarms in the adapter to reflect their current nextTriggerDate
                    //This allows us to pass a fully up-to-date alarm object to our editing dialog
                    //This action doesn't change anything for the user's side. The exact nextTriggerDate
                    //is never shown to them (right now), but this allows debug logging to display proper info
                    //Alarms that trigger and reset with a new randomly chosen offset don't update the adapter by default
                    //since there's no guarantee the app will be running. This way, we can always be sure it's up-to-date when we need it.
                updateAlarmSet();
                alarmsListFrag.openEditDialog(alarmSet.get(position));
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return alarmSet.size();
    }

    //Our Unique ViewHolder for each RecyclerView listing
    public static class ViewHolder extends RecyclerView.ViewHolder {
        //Important components of each ViewHolder
        public TextView listedAlarmName;
        public TextView listedAlarmTime;
        public Button editListedAlarmButton;

        //Links layout elements to fields here
        public ViewHolder(View itemView) {
            super(itemView);
            listedAlarmName = itemView.findViewById(R.id.listedAlarmName);
            listedAlarmTime = itemView.findViewById(R.id.listedAlarmTime);
            editListedAlarmButton = itemView.findViewById(R.id.editListedAlarmButton);
        }
    }

    //Updates alarmSet list to reflect whatever change was made to the database
    public void updateAlarmSet(){
        //Retrieve the up-to-date set of alarms from our db and assign that to our alarmSet reference
        alarmSet = helper.getAllAlarms(); //Our out of date alarmSet can now be garbage collected
        notifyDataSetChanged(); //Update to reflect the changes in our adapter
    }


    //The following was an elaborate scheme of mine to try to be clever. Merge the two lists without adding duplicates, thereby saving space.
    //Update: I was not being clever. We already have two lists we've created and there's no getting around that. Nor did it really work as intended anyways (but we could have fixed that).
    //I can just set my alarmSet to the database alarmSet since that's the up to date version.
    //We're not adding any new alarms to either list, and again one list is garbage collected at the end.
    //Below is being kept temporarily as a testament to stupidity and how one moment of inspiration can make you look brilliant and idiotic at the same time.

    /*
    //Keeps alarmSet up to date with database of all alarms
    //Takes in a list retrievedSet of all alarms found in the database and merges them into alarmSet without duplicates
    private void combineToAlarmSet(ArrayList<AlarmInfo> retrievedSet){
        int alarmSetPosition = 0, retrievedSetPosition = 0; //Start our iteration at the front of both lists

        //Run until we've compared all alarms in the alarmSet
        while(alarmSetPosition < alarmSet.size()){
            if(retrievedSetPosition < retrievedSet.size()){ //Still alarms in retrievedSet to compare with
                //Get the alarm at respective indices for both lists
                AlarmInfo rsAlarm = retrievedSet.get(retrievedSetPosition);
                AlarmInfo asAlarm = alarmSet.get(alarmSetPosition);
                //Compares the alarms to figure out their order in the alarmSet
                int compVal = rsAlarm.compareTo(asAlarm);

                if(compVal < 0){
                    //Alarm from retrievedSet is smaller, needs to be inserted in front of asAlarm
                    //Insert, move one through both
                    alarmSet.add(alarmSetPosition, asAlarm);
                    alarmSetPosition++;
                    retrievedSetPosition++;
                } else if(compVal == 0){
                    //Same alarm, skip both
                    alarmSetPosition++;
                    retrievedSetPosition++;
                } else {
                    //rsAlarm doesn't fit in front of asAlarm, keep looking through alarmSet without inserting rsAlarm yet
                    alarmSetPosition++;
                }
            }
        }

        //We've finished looking through the alarmset, but there might still be alarms left to add from the retrievedSet
        //We need to add all these remaining alarms to the alarmSet
        for(;retrievedSetPosition < retrievedSet.size(); retrievedSetPosition++){
            alarmSet.add(retrievedSet.get(retrievedSetPosition++)); //Adds then moves indexes after operation finishes
        }
    }
    */

    //Deletes the alarm from the database and from the alarmSet, returning the position the alarm occupied in the alarmSet if found (-1 if not found)
    public int removeAlarm(AlarmInfo alarm){
        helper.deleteAlarm(alarm);
        int position = alarmSet.indexOf(alarm);
        alarmSet.remove(alarm);
        return position;
    }

    //Returns the size of the alarmSet
    public int getAlarmSetSize(){
        return alarmSet.size();
    }

}