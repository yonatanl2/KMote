package com.levyinc.android.kodimote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yonatan on 11/10/16.
 */

public class SlidingTabActivity extends android.support.v4.app.Fragment {

    public static ViewPager viewPager;
    private TabLayout tabLayout;

    FragmentAdapterClass fragmentPagerAdapter;

    View rootView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.remote_tab_viewer, container, false);


        viewPager = (ViewPager) rootView.findViewById(R.id.remote_page_viewer);
        tabLayout = (TabLayout) rootView.findViewById(R.id.remote_tab_layout);


        String[] tabs = getResources().getStringArray(R.array.tabs);

        tabLayout.addTab(tabLayout.newTab().setText(tabs[0]));
        tabLayout.addTab(tabLayout.newTab().setText(tabs[1]));
        tabLayout.addTab(tabLayout.newTab().setText(tabs[2]));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentPagerAdapter = new FragmentAdapterClass(fragmentManager, tabLayout.getTabCount());


        viewPager.setAdapter(fragmentPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.setCurrentItem(1);
}

    @Override
    public void onStop() {
        super.onStop();
        ButtonActions.buttonActionsHandler.removeCallbacksAndMessages(null);
    }
}



