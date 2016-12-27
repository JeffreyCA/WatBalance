package com.cg.watbalance.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.cg.watbalance.R;
import com.cg.watbalance.balanceScreen;
import com.cg.watbalance.data.BalanceData;
import com.cg.watbalance.data.OutletData;
import com.cg.watbalance.data.transaction.TransactionData;
import com.cg.watbalance.preferences.Connection;
import com.cg.watbalance.preferences.ConnectionDetails;
import com.cg.watbalance.preferences.Encryption;
import com.cg.watbalance.preferences.FileManager;

import java.net.InetAddress;

import ca.jeffrey.watcard.WatAccount;

public class Service extends BroadcastReceiver {
    Connection myConn;
    ConnectionDetails myConnDet;
    private boolean internetAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SERVICE", "REQUEST RECEIVED");
        initializeService(context);
    }

    // Check if internet is available
    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("watcard.uwaterloo.ca");
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public void initializeService(final Context context) {
        class EstablishConnection extends AsyncTask<String, Void, WatAccount> {
            @Override
            protected WatAccount doInBackground(String... strings) {
                Encryption myEncryption = new Encryption(context);
                SharedPreferences myLoginPref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

                internetAvailable = isInternetAvailable();

                if (internetAvailable) {
                    try {
                        myConnDet = new ConnectionDetails(myLoginPref.getString("IDNum", "00000000"),
                                myEncryption.decryptPIN(myLoginPref.getString("PinNum", "0000")));
                    }
                    catch (IllegalArgumentException e) {
                        return null;
                    }

                    return myConnDet.getAccount();
                }
                else {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final WatAccount result) {
                myConn = new Connection(myConnDet, context) {
                    @Override
                    public void onComplete() {
                        FileManager myFM = new FileManager(context);
                        myFM.openFileInput("myBalData");
                        BalanceData myBalData = (BalanceData) myFM.readData();
                        myFM.closeFileInput();

                        myFM.openFileInput("myTransData");
                        TransactionData myTransData = (TransactionData) myFM.readData();
                        myFM.closeFileInput();

                        myFM.openFileInput("myOutletData");
                        OutletData myOutletData = (OutletData) myFM.readData();
                        myFM.closeFileInput();

                        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                        if (myPreferences.getBoolean("dailyNotification", true)) {
                            createNotification(myBalData, context);
                        }

                        Intent intent = new Intent("com.cg.WatBalance.newData")
                                .putExtra("myBalData", myBalData)
                                .putExtra("myTransData", myTransData)
                                .putExtra("myOutletData", myOutletData);
                        context.sendBroadcast(intent);
                    }

                    @Override
                    public void beforeConnect() {
                    }

                    @Override
                    public void onConnectionError() {
                        Toast.makeText(context.getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onIncorrectLogin() {
                    }

                    @Override
                    public void onResponseReceive() {
                    }
                };

                if (!internetAvailable) {
                    myConn.onConnectionError();
                }
                else if (result == null) {
                    myConn.onIncorrectLogin();
                }
                else {
                    myConn.getData();
                }
            }
        }
        new EstablishConnection().execute();
    }

    public void createNotification(BalanceData myBalData, Context myContext) {
        Log.d("NOTIFICATION", "CREATE");
        NotificationManager notificationManager = (NotificationManager) myContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
        PendingIntent openApp = PendingIntent.getActivity(myContext, 0, new Intent(myContext, balanceScreen.class), 0);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(myContext.getApplicationContext())
                .setSmallIcon(R.drawable.ic_local_atm_24dp)
                .setContentTitle("WatBalance")
                .setContentText("You have " + myBalData.getTotalString() + " in your WatCard")
                .setContentIntent(openApp)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET);
        notificationManager.notify(1, notificationBuilder.build());
    }
}
