package com.cg.watbalance.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import com.cg.watbalance.data.transaction.TransactionData;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import ca.jeffrey.watcard.WatAccount;

public class BalanceData implements Serializable {
    private float MP = 0;
    private float FD = 0;
    private float Other = 0;
    private float Total = 0;
    private float dailyBalance = 0;
    private float todaySpent = 0;
    private DateTime Date;
    private boolean DatePassed = false;

    public void setBalanceData(WatAccount myAccount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.CANADA);

        try {
            MP = numberFormat.parse(String.valueOf(myAccount.getMealBalance())).floatValue();
            FD = numberFormat.parse(String.valueOf(myAccount.getFlexBalance())).floatValue();
            Other = numberFormat.parse(String.valueOf(myAccount.getOtherBalance())).floatValue();
            Total = numberFormat.parse(String.valueOf(myAccount.getTotalBalance())).floatValue();
            Date = DateTime.now();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDailyBalance(TransactionData myTransData, Context context) {
        todaySpent = 0;
        ArrayList<TransactionData.Transaction> myTransList = myTransData.getTransList();

        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int DailyBalConfig = Integer.parseInt(myPreferences.getString("dailyBalanceChoice", "1"));

        DateTimeFormatter myFormat = DateTimeFormat.forPattern("yyyy.MM.dd");

        DateTime endOfTerm = myFormat.parseDateTime(myPreferences.getString("termEnd", DateTime.now().toString(myFormat)));
        DateTime today = DateTime.now().withTimeAtStartOfDay();

        if (endOfTerm.isBefore(today)) {
            DatePassed = true;
        } else {
            int daysToTermEnd = Days.daysBetween(today, endOfTerm).getDays();
            switch (DailyBalConfig) {
                case 2: {
                    for (int i = 0; i < myTransList.size(); i++) {
                        boolean isToday = myTransList.get(i).getDate().withTimeAtStartOfDay().equals(today);
                        boolean isMealPlan = (myTransList.get(i).getType() == 0);
                        if (isToday && isMealPlan) {
                            todaySpent += myTransList.get(i).getAmount();
                        }
                    }
                    dailyBalance = (MP - todaySpent) / daysToTermEnd;
                    break;
                }
                case 3: {
                    for (int i = 0; i < myTransList.size(); i++) {
                        boolean isToday = myTransList.get(i).getDate().withTimeAtStartOfDay().equals(today);
                        boolean isFlexDollar = (myTransList.get(i).getType() == 1);
                        if (isToday && isFlexDollar) {
                            todaySpent += myTransList.get(i).getAmount();
                        }
                    }
                    dailyBalance = (FD - todaySpent) / daysToTermEnd;
                    break;
                }
                default: {
                    for (int i = 0; i < myTransList.size(); i++) {
                        boolean isToday = myTransList.get(i).getDate().withTimeAtStartOfDay().equals(today);
                        if (isToday) {
                            todaySpent += myTransList.get(i).getAmount();
                        }
                    }
                    dailyBalance = (Total - todaySpent) / daysToTermEnd;
                    break;
                }
            }
        }
    }

    public String getMPString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(MP);
    }

    public String getFDString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(FD);
    }

    public String getOtherString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(Other);
    }

    public String getTotalString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(Total);
    }

    public String getDailyBalanceString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(dailyBalance);
    }

    public String getTodaySpentString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(todaySpent);
    }

    public String getTodayLeftString() {
        return NumberFormat.getCurrencyInstance(Locale.CANADA).format(dailyBalance + todaySpent);
    }

    public Boolean getDatePassed() {
        return DatePassed;
    }
    public String getDateString() {
        String txt = DateUtils.getRelativeTimeSpanString(Date.getMillis()).toString();
        if (txt.equals("0 minutes ago")) {
            return "Now";
        } else {
            return txt;
        }
    }

}
