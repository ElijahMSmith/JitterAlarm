package me.eli.jitteralarm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.eli.jitteralarm.R;
import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.AlarmsAdapter;
import me.eli.jitteralarm.utilities.DatabaseHelper;

public class CurrentAlarms extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Context context;
    private ArrayList<AlarmInfo> alarmData;
    private DatabaseHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current_alarms, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        helper = new DatabaseHelper(rootView.getContext());

        // Initialize with all existing alarms
        alarmData = helper.getAllAlarms();

        // Create adapter passing in existing alarms
        AlarmsAdapter adapter = new AlarmsAdapter(alarmData);

        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);

        // Set layout manager to position the items
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        this.context = context;
    }
}