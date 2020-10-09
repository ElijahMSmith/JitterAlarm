package me.eli.jitteralarm.utilities;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragPageAdapter extends FragmentPagerAdapter {

    //Holds both of our fragment pages
    private List<Fragment> fragList = new ArrayList<>();

    public FragPageAdapter(FragmentManager fm) {
        super(fm);
    }

    //Adds some new fragment to the FragPageAdapter
    public void addFragment(Fragment fragment){
        fragList.add(fragment);
    }

    //Get the fragment at a certain tab position. Used to get reference from activity.
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }

    //Return the total number of tabs in the FragPageAdapter
    @Override
    public int getCount() {
        return fragList.size();
    }

    //Creates the tab label for the each fragment page
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
