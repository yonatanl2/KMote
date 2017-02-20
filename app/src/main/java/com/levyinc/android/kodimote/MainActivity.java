package com.levyinc.android.kodimote;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (Main2Activity.getWebSocketStatus()) {
                Main2Activity.webSocketEndpoint.volumeAction("volumedown");
            } else {
                ButtonActions.volumeAction("volumedown");
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (Main2Activity.getWebSocketStatus()) {
                Main2Activity.webSocketEndpoint.volumeAction("volumeup");
            } else {
                ButtonActions.volumeAction("volumeup");
            }
            return true;
        } else {
            System.out.println(keyCode + "event" + event + event.getKeyCharacterMap());
            System.out.println(fragmentManager.findFragmentByTag("nav-main"));
            /*if (fragmentManager.findFragmentByTag("nav-main") != null) {
              ButtonActions.setText(event.getDisplayLabel());
            }*/
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            return true;
        } else
            return super.onKeyUp(keyCode, event);
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().replace(R.id.content_frame, new SlidingTabActivity(), "nav-main").commitAllowingStateLoss();
            }
        }, 5);
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setCheckedItem(R.id.nav_main);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.nav_main:
                        fragmentManager.beginTransaction().replace(R.id.content_frame, new SlidingTabActivity(), "nav-main").commit();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fragmentManager.beginTransaction().replace(R.id.content_frame, new SlidingTabActivity()).commit();
                                toolbar.setElevation(0);
                            }
                        }, 300);

                        return true;

                    case R.id.nav_settings:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fragmentManager.beginTransaction().replace(R.id.content_frame, new SettingsActivity(), "settings").commit();
                                toolbar.setElevation(5);
                                toolbar.setTitle("Settings");

                            }
                        }, 300);
                    default:
                        return true;

                }
            }
        });
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.shutdown):
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.powerButton("shutdown");
                } else {
                    ButtonActions.powerButton("shutdown");
                }
                break;
            case (R.id.reboot):
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.powerButton("reboot");
                } else {
                    ButtonActions.powerButton("reboot");
                }
                break;
            case(R.id.fullscreen):
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.toggleFullScreen();
                } else {
                    ButtonActions.toggleFullScreen();
                }
                break;
            case(R.id.clear_audio):
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.clearAudioLibrary();
                } else {
                    ButtonActions.clearAudioLibrary();
                }
                break;
            case(R.id.clear_video):
                if (Main2Activity.getWebSocketStatus()) {
                    Main2Activity.webSocketEndpoint.clearVideoLibrary();
                } else {
                    ButtonActions.clearVideoLibrary();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ButtonActions.stopAsynchTask();
    }
}
