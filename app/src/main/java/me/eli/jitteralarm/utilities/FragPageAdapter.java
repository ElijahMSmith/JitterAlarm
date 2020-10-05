package me.eli.jitteralarm.utilities;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import me.eli.jitteralarm.fragments.CurrentAlarms;
import me.eli.jitteralarm.fragments.NewAlarm;

public class FragPageAdapter extends FragmentPagerAdapter {

    int totalTabs;

    public FragPageAdapter(FragmentManager fm, int totalTabs) {
        super(fm);
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CurrentAlarms();
            case 1:
                return new NewAlarm();
        }

        return null;
    }

    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return "Current Alarms";
            case 1:
                return "New Alarm";
        }
        return "";
    }
}
