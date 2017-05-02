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
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.cg.watbalance.data.transaction.TransactionData;
import com.cg.watbalance.data.transaction.TransactionListAdapter;
import com.cg.watbalance.preferences.FileManager;
import com.cg.watbalance.preferences.Preferences;
import com.cg.watbalance.service.Service;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.apache.commons.lang3.text.WordUtils;
import org.threeten.bp.LocalDateTime;

import lecho.lib.hellocharts.view.LineChartView;

public class transactionScreen extends AppCompatActivity {
    public static int navItemIndex = 1;
    FloatingActionButton fab;

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER", "NEW TRANSACTION DATA");
            updateView((TransactionData) intent.getSerializableExtra("myTransData"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_LONG).show();
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
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidThreeTen.init(this);

        FileManager myFM = new FileManager(this);
        myFM.openFileInput("myTransData");
        TransactionData myTransData = (TransactionData) myFM.readData();
        myFM.closeFileInput();

        updateView(myTransData);
        IntentFilter myFilter = new IntentFilter("com.cg.WatBalance.newData");
        registerReceiver(myReceiver, myFilter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        } else {
            Intent myIntent = new Intent(this, balanceScreen.class);
            startActivity(myIntent);
            finish();
        }
        super.onBackPressed();
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
                        myIntent = new Intent(c, balanceScreen.class);
                        startActivity(myIntent);
                        navItemIndex = 0;
                        finish();
                        break;
                    case R.id.nav_transactions:
                        navItemIndex = 1;
                        drawer.closeDrawers();
                        break;
                    case R.id.nav_outlets:
                        myIntent = new Intent(c, outletScreen.class);
                        startActivity(myIntent);
                        navItemIndex = 2;
                        finish();
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

    public void updateView(TransactionData myTransData) {
        LineChartView transChart = (LineChartView) findViewById(R.id.transChart);
        transChart.setLineChartData(myTransData.makeTransChartData());
        transChart.setZoomEnabled(false);
        transChart.setValueSelectionEnabled(true);

        TextView month = (TextView) findViewById(R.id.month);
        month.setText(WordUtils.capitalizeFully(LocalDateTime.now().getMonth().name()));

        ListView transList = (ListView) findViewById(R.id.transList);
        transList.setAdapter(new TransactionListAdapter(getApplicationContext(), myTransData.getTransList()));
        transList.setEmptyView(findViewById(R.id.empty_transactions));

        // Hide fab on scroll so balances are not blocked
        transList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int initialY = fab.getScrollY();

                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    fab.animate().cancel();
                    fab.animate().translationYBy(250);
                } else {
                    fab.animate().cancel();
                    fab.animate().translationY(initialY);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }
}
