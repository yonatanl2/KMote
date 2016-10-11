package com.levyinc.android.kodimote;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentAdapterClass extends FragmentStatePagerAdapter {

    int TabCount;

    public FragmentAdapterClass(FragmentManager fragmentManager, int CountTabs) {

        super(fragmentManager);

        this.TabCount = CountTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                Main2Activity tab1 = new Main2Activity();
                return tab1;

            case 1:
                ExtendedControlActivity tab2 = new ExtendedControlActivity();
                return tab2;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return TabCount;
    }
}