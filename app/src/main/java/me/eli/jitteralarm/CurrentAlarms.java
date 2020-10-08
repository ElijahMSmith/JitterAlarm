package me.eli.jitteralarm;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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

    private AlarmsAdapter adapter;
    private DatabaseHelper db;

    public CurrentAlarms(DatabaseHelper db){
        this.db = db;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current_alarms, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());

        // Set layout manager to position the items
        recyclerView.setLayoutManager(layoutManager);

        // Create adapter passing in existing alarms
        adapter = new AlarmsAdapter(db, getContext());

        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
    }

    public void updateAdapter(){
        adapter.updateAlarmSet();
    }
}