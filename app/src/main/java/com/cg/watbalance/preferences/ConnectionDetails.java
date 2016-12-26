package com.cg.watbalance.preferences;

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

    public ConnectionDetails(final String newIDNum, final String newPinNum){
        myIDNum = newIDNum;
        myPinNum = newPinNum;

            myAccount = new WatAccount(newIDNum, newPinNum);
            myAccount.login();
            myAccount.loadBalances();
            myAccount.loadPersonalInfo();

    }

    public WatAccount getAccount() {
        return myAccount;
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
