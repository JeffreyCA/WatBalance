package com.cg.watbalance;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cg.watbalance.preferences.Connection;
import com.cg.watbalance.preferences.ConnectionDetails;
import com.cg.watbalance.preferences.Encryption;
import com.cg.watbalance.service.Service;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.apache.commons.lang3.text.WordUtils;
import org.threeten.bp.LocalDateTime;

import java.net.InetAddress;
import java.util.Calendar;

import ca.jeffrey.watcard.WatAccount;

public class login extends AppCompatActivity {

    EditText IDNum;
    EditText pinNum;
    Connection myConn;
    ConnectionDetails myConnDet;
    Button mySaveButton;
    TextView forgotPIN;
    Encryption myEncryption;
    SharedPreferences myPreferences;
    SharedPreferences.Editor myPrefEditor;

    private boolean internetAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AndroidThreeTen.init(this);

        myEncryption = new Encryption(getApplicationContext());

        //Variable Declaration
        IDNum = (EditText) findViewById(R.id.IDNum);
        pinNum = (EditText) findViewById(R.id.pinNum);
        mySaveButton = (Button) findViewById(R.id.button);
        forgotPIN = (TextView) findViewById(R.id.forgotPIN);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        int versionCode = myPreferences.getInt("versionCode", 1);
        boolean login = myPreferences.getBoolean("login", false);

        if (versionCode == BuildConfig.VERSION_CODE && login) {
            startRepeat();

            //Go to Launch Screen
            Intent myIntent = new Intent(getApplicationContext(), balanceScreen.class);
            startActivity(myIntent);
            finish();
        }

        forgotPIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String resetURL = "https://watcard.uwaterloo.ca/OneWeb/Account/ResetPin";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(resetURL));
                startActivity(i);
            }
        });
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

    public void onButtonClick(View v) {
        final String id = IDNum.getText().toString();
        final String pin = pinNum.getText().toString();
        final ProgressDialog progress = new ProgressDialog(login.this);

        class EstablishConnection extends AsyncTask<String, Void, WatAccount> {
            @Override
            protected WatAccount doInBackground(String... params) {
                internetAvailable = isInternetAvailable();
                if (!internetAvailable) {
                    return null;
                }

                try {
                    myConnDet = new ConnectionDetails(id, pin);
                } catch (IllegalArgumentException e) {
                    return null;
                }

                return myConnDet.getAccount();
            }

            @Override
            protected void onPostExecute(final WatAccount result) {
                myConn = new Connection(myConnDet, getApplicationContext()) {
                    @Override
                    public void onComplete() {
                        Log.d("LOGIN", "SUCCESS");
                        String encryptedPIN = myEncryption.encryptPIN(pinNum.getText().toString());
                        String FullName = result.getName();
                        String TempFirstName = FullName.split(",")[1].trim();
                        String TempLastName = FullName.split(",")[0].trim();
                        String FirstName = WordUtils.capitalizeFully(TempFirstName.substring(0, TempFirstName.length()));
                        String LastName = WordUtils.capitalizeFully(TempLastName);

                        myPrefEditor = myPreferences.edit();
                        myPrefEditor.putString("Name", FirstName + " " + LastName);
                        myPrefEditor.putString("IDNum", IDNum.getText().toString());
                        myPrefEditor.putString("PinNum", encryptedPIN);
                        myPrefEditor.putInt("versionCode", BuildConfig.VERSION_CODE);
                        myPrefEditor.putBoolean("login", true);
                        myPrefEditor.apply();

                        startRepeat();
                        setTermEnd();

                        Intent myIntent = new Intent(login.this, balanceScreen.class);
                        startActivity(myIntent);
                        finish();
                    }

                    @Override
                    public void beforeConnect() {
                    }

                    @Override
                    public void onConnectionError() {
                        Toast.makeText(getApplicationContext(), "Connection Error!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onIncorrectLogin() {
                        Toast.makeText(getApplicationContext(), "Incorrect Login Information!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponseReceive() {
                    }
                };

                if (!internetAvailable) {
                    myConn.onConnectionError();
                } else if (result == null) {
                    myConn.onIncorrectLogin();
                } else {
                    myConn.getData();
                }
                progress.dismiss();
            }

            @Override
            protected void onPreExecute() {
                progress.setTitle("Loading");
                progress.setMessage("Logging in...");
                progress.setCancelable(false);
                progress.show();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
            }
        }

        new EstablishConnection().execute();
    }

    public void startRepeat() {
        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent newPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, Service.class), 0);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, newPendingIntent);
    }

    public void setTermEnd() {
        int month = LocalDateTime.now().getMonthValue();
        int year = LocalDateTime.now().getYear();
        int day = LocalDateTime.now().getDayOfMonth();

        switch (month) {
            case 1:
            case 2:
            case 3: {
                myPrefEditor.putString("termEnd", year + ".04.20");
                break;
            }
            case 4: {
                if (day > 20) {
                    myPrefEditor.putString("termEnd", year + ".08.20");
                } else {
                    myPrefEditor.putString("termEnd", year + ".04.20");
                }
                break;
            }
            case 5:
            case 6:
            case 7: {
                myPrefEditor.putString("termEnd", year + ".08.20");
                break;
            }
            case 8: {
                if (day > 20) {
                    myPrefEditor.putString("termEnd", year + ".12.20");
                } else {
                    myPrefEditor.putString("termEnd", year + ".08.20");
                }
                break;
            }
            case 9:
            case 10:
            case 11: {
                myPrefEditor.putString("termEnd", year + ".12.20");
                break;
            }
            case 12: {
                if (day > 20) {
                    myPrefEditor.putString("termEnd", (year + 1) + ".04.20");
                } else {
                    myPrefEditor.putString("termEnd", year + ".12.20");
                }
                break;
            }
        }
        myPrefEditor.apply();
    }
}
