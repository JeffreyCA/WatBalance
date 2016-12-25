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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cg.watbalance.data.MenuListAdapter;
import com.cg.watbalance.data.OutletData;
import com.cg.watbalance.preferences.FileManager;
import com.cg.watbalance.preferences.Preferences;
import com.cg.watbalance.service.Service;

public class outletScreen extends AppCompatActivity {
    public static int navItemIndex = 2;

    ListView lunchListView;
    ListView dinnerListView;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("RECEIVER", "NEW OUTLET DATA");
            updateView((OutletData) intent.getSerializableExtra("myOutletData"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        lunchListView = (ListView) findViewById(R.id.lunch);
        dinnerListView = (ListView) findViewById(R.id.dinner);
    }

    @Override
    public void onResume() {
        super.onResume();
        FileManager myFM = new FileManager(this);
        myFM.openFileInput("myOutletData");
        OutletData myOutletData = (OutletData) myFM.readData();
        myFM.closeFileInput();

        updateView(myOutletData);
        IntentFilter myFilter = new IntentFilter("com.cg.WatBalance.newData");
        registerReceiver(myReceiver, myFilter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
        }
        else {
            Intent myIntent = new Intent(this, balanceScreen.class);
            startActivity(myIntent);
            finish();
        }

        finish();
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
                        myIntent = new Intent(c, transactionScreen.class);
                        startActivity(myIntent);
                        navItemIndex = 1;
                        finish();
                        break;
                    case R.id.nav_outlets:
                        navItemIndex = 2;
                        drawer.closeDrawers();
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

    public void switchScreen(int id) {
        if (id == R.id.nav_balance) {
            Intent myIntent = new Intent(this, balanceScreen.class);
            startActivity(myIntent);
            finish();
        } else if (id == R.id.nav_transactions) {
            Intent myIntent = new Intent(this, transactionScreen.class);
            startActivity(myIntent);
            finish();
        } else if (id == R.id.nav_settings) {
            Intent myIntent = new Intent(this, Preferences.class);
            startActivity(myIntent);
        } else if (id == R.id.nav_about) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.about_dialog);
            dialog.setTitle("WatBalance");
            dialog.show();
        }
    }

    public void updateView(OutletData myOutletData) {
        final OutletData.Menu REVMenu = myOutletData.findMenu(7);
        final Context myContext = this;

        if (REVMenu != null) {
            TextView outletName = (TextView) findViewById(R.id.outletName1);
            outletName.setText(REVMenu.getOutletName());

            TextView outletStatus = (TextView) findViewById(R.id.outletStatus1);
            boolean isREVOpen = REVMenu.getOpen();
            if (isREVOpen) {
                outletStatus.setText("Open Now");
            } else {
                outletStatus.setText("Closed");
                outletStatus.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            }

            LinearLayout REV = (LinearLayout) findViewById(R.id.outlet1);
            REV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunchListView.setAdapter(new MenuListAdapter(myContext, REVMenu.getLunch().getFoodList()));
                    dinnerListView.setAdapter(new MenuListAdapter(myContext, REVMenu.getDinner().getFoodList()));

                    lunchListView.setEmptyView(findViewById(R.id.empty_lunch));
                    dinnerListView.setEmptyView(findViewById(R.id.empty_dinner));
                }
            });

        }

        final OutletData.Menu V1Menu = myOutletData.findMenu(5);

        if (V1Menu != null) {
            TextView outletName = (TextView) findViewById(R.id.outletName2);
            outletName.setText(V1Menu.getOutletName());

            TextView outletStatus = (TextView) findViewById(R.id.outletStatus2);
            boolean isV1Open = V1Menu.getOpen();
            if (isV1Open) {
                outletStatus.setText("Open Now");
            } else {
                outletStatus.setText("Closed");
                outletStatus.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            }

            LinearLayout V1 = (LinearLayout) findViewById(R.id.outlet2);

            V1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunchListView.setAdapter(new MenuListAdapter(myContext, V1Menu.getLunch().getFoodList()));
                    dinnerListView.setAdapter(new MenuListAdapter(myContext, V1Menu.getDinner().getFoodList()));

                    lunchListView.setEmptyView(findViewById(R.id.empty_lunch));
                    dinnerListView.setEmptyView(findViewById(R.id.empty_dinner));
                }
            });

            lunchListView.setAdapter(new MenuListAdapter(myContext, V1Menu.getLunch().getFoodList()));
            dinnerListView.setAdapter(new MenuListAdapter(myContext, V1Menu.getDinner().getFoodList()));

            lunchListView.setEmptyView(findViewById(R.id.empty_lunch));
            dinnerListView.setEmptyView(findViewById(R.id.empty_dinner));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
    }
}
