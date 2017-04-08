package com.cg.watbalance.preferences;

import ca.jeffrey.watcard.WatAccount;

public class ConnectionDetails {
    final String APIURL = "https://api.uwaterloo.ca/v2/";
    final String APIKey = "?key=907f2381ac84737b6bfe0e41d159fbee";
    final String FoodURL = APIURL + "foodservices/menu.json" + APIKey;
    final String OutletURL = APIURL + "foodservices/locations.json" + APIKey;
    final String BuildingURL = APIURL + "buildings/list.json" + APIKey;
    private String myIDNum = null;
    private String myPinNum = null;

    private WatAccount myAccount;

    public ConnectionDetails(String newIDNum, String newPinNum) throws IllegalArgumentException {
        myIDNum = newIDNum;
        myPinNum = newPinNum;

        myAccount = new WatAccount(newIDNum, newPinNum);

        if (myAccount.login() != -1) {
            myAccount.loadBalances();
            myAccount.loadPersonalInfo();
        }
        else {
            throw new IllegalArgumentException();
        }
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
