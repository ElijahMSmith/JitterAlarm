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

import me.eli.jitteralarm.R;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<AlarmInfo> alarmSet;

    // Pass in the contact array into the constructor
    public AlarmsAdapter(List<AlarmInfo> alarmSet) {
        this.alarmSet = alarmSet;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public AlarmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View alarmView = inflater.inflate(R.layout.adapter_listing, parent, false);

        // Return a new holder instance
        return new ViewHolder(alarmView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(AlarmsAdapter.ViewHolder holder, int position) {
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
                /*

                TODO: ADD ACTION TO EDIT BUTTON

                 */
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

            listedAlarmName = (TextView) itemView.findViewById(R.id.listedAlarmName);
            listedAlarmTime = (TextView) itemView.findViewById(R.id.listedAlarmTime);
            editListedAlarmButton = (Button) itemView.findViewById(R.id.editListedAlarmButton);
        }
    }

}