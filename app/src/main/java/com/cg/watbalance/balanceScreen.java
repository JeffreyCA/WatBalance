package com.cg.watbalance;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cg.watbalance.data.BalanceData;
import com.cg.watbalance.preferences.FileManager;
import com.cg.watbalance.preferences.Preferences;
import com.cg.watbalance.service.Service;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class balanceScreen extends AppCompatActivity {
    public static int navItemIndex = 0;
    TextView updateText;
    Runnable runnable;

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER", "NEW BALANCE DATA");
            updateView((BalanceData) intent.getSerializableExtra("myBalData"));
            updateTime((BalanceData) intent.getSerializableExtra("myBalData"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateText.removeCallbacks(runnable);
                Snackbar.make(findViewById(R.id.rootView), "Refreshing...", Snackbar.LENGTH_LONG).show();
                sendBroadcast(new Intent(getApplicationContext(), Service.class));
            }
        });

        setUpNavigationView(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        TextView name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.Name);
        TextView id = (TextView) navigationView.getHeaderView(0).findViewById(R.id.IDText);

        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        name.setText(myPreferences.getString("Name", "User"));
        id.setText("ID# " + myPreferences.getString("IDNum", "Unknown"));

        updateText = (TextView) findViewById(R.id.updateText);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        }

        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidThreeTen.init(this);

        FileManager myFM = new FileManager(this);
        myFM.openFileInput("myBalData");
        BalanceData myBalData = (BalanceData) myFM.readData();
        myFM.closeFileInput();

        updateView(myBalData);
        // updateTime(myBalData);

        IntentFilter myFilter = new IntentFilter("com.cg.WatBalance.newData");
        registerReceiver(myReceiver, myFilter);

        updateText.removeCallbacks(runnable);
        Snackbar.make(findViewById(R.id.rootView), "Refreshing...", Snackbar.LENGTH_LONG).show();
        sendBroadcast(new Intent(getApplicationContext(), Service.class));
    }

    private void setUpNavigationView(final Context c) {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Intent myIntent;
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_balance:
                        navItemIndex = 0;
                        break;
                    case R.id.nav_transactions:
                        myIntent = new Intent(c, transactionScreen.class);
                        startActivity(myIntent);
                        finish();
                        navItemIndex = 1;
                        break;
                    case R.id.nav_outlets:
                        myIntent = new Intent(c, outletScreen.class);
                        startActivity(myIntent);
                        finish();
                        navItemIndex = 2;
                        break;
                    case R.id.nav_settings:
                        myIntent = new Intent(c, Preferences.class);
                        startActivity(myIntent);
                        navItemIndex = 3;
                        break;
                    case R.id.nav_about:
                        Dialog dialog = new Dialog(c);
                        dialog.setContentView(R.layout.about_dialog);
                        dialog.setTitle("About");
                        dialog.show();
                        navItemIndex = 4;
                        break;
                    default:
                        navItemIndex = 0;
                }

                menuItem.setChecked(false);
                drawer.closeDrawers();
                return true;
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    Context c = this;

    public void updateTime(final BalanceData myBalData) {
        final TextView updateText = (TextView) findViewById(R.id.updateText);
        runnable = new Runnable() {
            @Override
            public void run() {
                updateText.setText(myBalData.getDateString());
                updateText.postDelayed(runnable, 60000);
                FileManager myFM = new FileManager(c);
                myFM.openFileOutput("myBalData");
                myFM.writeData(myBalData);
                myFM.closeFileOutput();
            }
        };

        updateText.postDelayed(runnable, 60000);
    }

    public void updateView(BalanceData myBalData) {
        TextView total = (TextView) findViewById(R.id.Total);
        total.setText(myBalData.getTotalString());

        TextView mp = (TextView) findViewById(R.id.mealPlanData);
        mp.setText(myBalData.getMPString());

        TextView fd = (TextView) findViewById(R.id.flexDollarsData);
        fd.setText(myBalData.getFDString());

        TextView other = (TextView) findViewById(R.id.otherData);
        other.setText(myBalData.getOtherString());

        TextView dailyTot = (TextView) findViewById(R.id.dayBalance);
        TextView todaySpent = (TextView) findViewById(R.id.todaySpent);
        TextView todayLeft = (TextView) findViewById(R.id.todayLeft);

        TextView updateText = (TextView) findViewById(R.id.updateText);
        updateText.setText(myBalData.getDateString());

        RelativeLayout todaySpentRow = (RelativeLayout) findViewById(R.id.spentTodayRow);
        RelativeLayout todayLeftRow = (RelativeLayout) findViewById(R.id.todayLeftRow);
        RelativeLayout datePassed = (RelativeLayout) findViewById(R.id.datePassed);

        if (myBalData.getDatePassed()) {
            dailyTot.setVisibility(View.GONE);
            todaySpentRow.setVisibility(View.GONE);
            todayLeftRow.setVisibility(View.GONE);
            datePassed.setVisibility(View.VISIBLE);
        } else {
            dailyTot.setVisibility(View.VISIBLE);
            todaySpentRow.setVisibility(View.VISIBLE);
            todayLeftRow.setVisibility(View.VISIBLE);
            datePassed.setVisibility(View.GONE);

            dailyTot.setText(myBalData.getDailyBalanceString());
            todaySpent.setText(myBalData.getTodaySpentString());
            todayLeft.setText(myBalData.getTodayLeftString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
        updateText.removeCallbacks(runnable);
    }
}
