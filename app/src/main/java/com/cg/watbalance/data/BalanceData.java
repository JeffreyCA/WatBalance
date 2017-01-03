package com.cg.watbalance.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.cg.watbalance.data.transaction.TransactionData;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import ca.jeffrey.watcard.WatAccount;
import ca.jeffrey.watcard.WatTransaction;

public class BalanceData implements Serializable {
    private float MP = 0;
    private float FD = 0;
    private float Other = 0;
    private float Total = 0;
    private float dailyBalance = 0;
    private float todaySpent = 0;
    private LocalDateTime Date;
    private boolean DatePassed = false;

    public void setBalanceData(final WatAccount myAccount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.CANADA);

        try {
            MP = numberFormat.parse(String.valueOf(myAccount.getMealBalance())).floatValue();
            FD = numberFormat.parse(String.valueOf(myAccount.getFlexBalance())).floatValue();
            Other = numberFormat.parse(String.valueOf(myAccount.getOtherBalance())).floatValue();
            Total = numberFormat.parse(String.valueOf(myAccount.getTotalBalance())).floatValue();
            Date = LocalDateTime.now();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDailyBalance(TransactionData myTransData, Context context) {
        todaySpent = 0;

        List<WatTransaction> myTransList = myTransData.getTransList();

        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int DailyBalConfig = Integer.parseInt(myPreferences.getString("dailyBalanceChoice", "1"));

        DateTimeFormatter myFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        Log.i("END_OF_TERM", myPreferences.getString("termEnd", LocalDateTime.now().format(myFormat)));
        LocalDate endOfTerm = LocalDate.parse(myPreferences.getString("termEnd", LocalDateTime.now().format(myFormat)), myFormat);
        LocalDate today = LocalDateTime.now().toLocalDate();

        if (endOfTerm.isBefore(today)) {
            DatePassed = true;
        } else {
            long daysToTermEnd = ChronoUnit.DAYS.between(today, endOfTerm);
            switch (DailyBalConfig) {
                case 2: {
                    for (int i = 0; i < myTransList.size(); i++) {
                        boolean isToday = myTransList.get(i).getDateTime().toLocalDate().isEqual(today);
                        boolean isMealPlan = !myTransList.get(i).isFlex();
                        if (isToday && isMealPlan) {
                            todaySpent += myTransList.get(i).getAmount();
                        }
                    }
                    dailyBalance = (MP - todaySpent) / daysToTermEnd;
                    break;
                }
                case 3: {
                    for (int i = 0; i < myTransList.size(); i++) {
                        boolean isToday = myTransList.get(i).getDateTime().toLocalDate().isEqual(today);
                        boolean isFlexDollar = myTransList.get(i).isFlex();

                        if (isToday && isFlexDollar) {
                            todaySpent += myTransList.get(i).getAmount();
                        }
                    }
                    dailyBalance = (FD - todaySpent) / daysToTermEnd;
                    break;
                }
                default: {
                    for (int i = 0; i < myTransList.size(); i++) {
                        boolean isToday = myTransList.get(i).getDateTime().toLocalDate().isEqual(today);
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
        String txt = DateUtils.getRelativeTimeSpanString(Date.atZone(ZoneId. // Cannot use this
                systemDefault()).toInstant().toEpochMilli()).toString();
        Log.i("DATE_STRING", txt);
        if (txt.equals("0 minutes ago")) {
            return "Now";
        } else {
            return txt;
        }
    }
}
