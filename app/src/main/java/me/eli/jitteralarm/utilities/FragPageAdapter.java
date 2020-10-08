package me.eli.jitteralarm.utilities;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragList = new ArrayList<>();

    public FragPageAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment){
        fragList.add(fragment);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }

    // this counts total number of tabs
    @Override
    public int getCount() {
        return fragList.size();
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
