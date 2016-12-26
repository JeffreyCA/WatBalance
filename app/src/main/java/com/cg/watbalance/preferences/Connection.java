package com.cg.watbalance.preferences;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cg.watbalance.data.BalanceData;
import com.cg.watbalance.data.OutletData;
import com.cg.watbalance.data.transaction.TransactionData;

public abstract class Connection {
    ConnectionDetails myConnDetails;
    RequestQueue queue;
    Context myContext;

    BalanceData myBalData;
    TransactionData myTransData;
    OutletData myOutletData;
    String outletResponse;
    String buildingResponse;

    public Connection(ConnectionDetails newConnDetails, Context context) {
        myContext = context;
        myConnDetails = newConnDetails;
        queue = Volley.newRequestQueue(context);
    }

    public void getData() {
        Log.d("CONNECTION", "ESTABLISHED");

        beforeConnect();

        createBalanceRequest();
        createTransHistoryRequest();

        queue.add(createMenuRequest());
        queue.add(createOutletRequest());
        queue.add(createBuildingRequest());
    }

    private void createBalanceRequest() {
        myBalData = new BalanceData();
        myBalData.setBalanceData(myConnDetails.getAccount());
        onDataReceive();
    }


    private void createTransHistoryRequest() {
        myTransData = new TransactionData();
        myTransData.setTransList(myConnDetails.getAccount());
        onDataReceive();
    }

    private StringRequest createMenuRequest() {
        return new StringRequest(Request.Method.GET, myConnDetails.getFoodURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        myOutletData = new OutletData();
                        myOutletData.setOutletData(response);
                        onDataReceive();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    private StringRequest createOutletRequest() {
        return new StringRequest(Request.Method.GET, myConnDetails.getOutletURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        outletResponse = response;
                        onDataReceive();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    private StringRequest createBuildingRequest() {
        return new StringRequest(Request.Method.GET, myConnDetails.getBuildingURL(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        buildingResponse = response;
                        onDataReceive();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    private void onDataReceive() {
        if (myBalData != null
                && myTransData != null && myOutletData != null && outletResponse != null && buildingResponse != null
                ) {
            Log.d("DATA", "RECEIVED");
            myBalData.setDailyBalance(myTransData, myContext);
            myOutletData.setOutletStatus(outletResponse);
            // myTransData.setBuildingTitle(buildingResponse);

            FileManager myFM = new FileManager(myContext);
            myFM.openFileOutput("myBalData");
            myFM.writeData(myBalData);
            myFM.closeFileOutput();

            myFM.openFileOutput("myTransData");
            myFM.writeData(myTransData);
            myFM.closeFileOutput();

            myFM.openFileOutput("myOutletData");
            myFM.writeData(myOutletData);
            myFM.closeFileOutput();

            onComplete();
        }
    }

    public abstract void onComplete();

    public abstract void onResponseReceive();

    public abstract void beforeConnect();

    public abstract void onConnectionError();

    public abstract void onIncorrectLogin();
}
