package com.cg.watbalance.preferences;

import org.joda.time.DateTime;

import ca.jeffrey.watcard.WatAccount;

public class ConnectionDetails {
    final String uWaterlooURL = "https://watcard.uwaterloo.ca/OneWeb/Scripts/OneWeb.exe?";
    final String APIURL = "https://api.uwaterloo.ca/v2/";
    final String APIKey = "?key=907f2381ac84737b6bfe0e41d159fbee";
    final String FoodURL = APIURL + "foodservices/menu.json" + APIKey;
    final String OutletURL = APIURL + "foodservices/locations.json" + APIKey;
    final String BuildingURL = APIURL + "buildings/list.json" + APIKey;
    private String myIDNum = null;
    private String myPinNum = null;
    private String myBalanceURL;

    private WatAccount myAccount;

    public ConnectionDetails(final String newIDNum, final String newPinNum) {
        myIDNum = newIDNum;
        myPinNum = newPinNum;

        Thread t = new Thread(new Runnable (){
            @Override
            public void run() {
                myAccount = new WatAccount(newIDNum, newPinNum);
                myAccount.login();
                myAccount.loadBalances();
                myAccount.loadPersonalInfo();
            }
        });

        t.start();

        try {
            t.join();
        }
        catch (Exception E) {}
    }


    public String getBalanceURL() {
        return myBalanceURL;
    }

    public WatAccount getAccount() {
        return myAccount;
    }

    public String getTransactionURL() {
        DateTime myDate = DateTime.now();
        int month = myDate.getMonthOfYear();
        int lastDayOfMonth = myDate.dayOfMonth().getMaximumValue();
        int year = myDate.getYear();

        return uWaterlooURL + "acnt_1=" + myIDNum + "&acnt_2=" + myPinNum + "&DBDATE=" + month + "%2F1%2F" + year + "&DEDATE=" + month + "%2F" + lastDayOfMonth + "%2F" + year + "&PASS=PASS&STATUS=HIST";
    }

    public String getFoodURL() {
        return FoodURL;
    }

    public String getOutletURL() {
        return OutletURL;
    }

    public String getBuildingURL() {
        return BuildingURL;
    }
}