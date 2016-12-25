package com.cg.watbalance.data.transaction;

import android.graphics.Color;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.jeffrey.watcard.WatAccount;
import ca.jeffrey.watcard.WatTransaction;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;

public class TransactionData implements Serializable {
    private List<WatTransaction> myTransList;

    public List<WatTransaction> getTransList() {
        return myTransList;
    }

    public void setTransList(final WatAccount myAccount) {
        Thread t = new Thread(new Runnable (){
            @Override
            public void run() {
                myTransList = myAccount.getLastDaysTransactions(60, false);
            }
        });

        t.start();

        try {
            t.join();
        }
        catch (Exception E) {

        }

    }

    public void setBuildingTitle(String response) {
        try {
            JSONArray buildingArray = new JSONObject(response).getJSONArray("data");
            for (int i = 0; i < myTransList.size(); i++) {
                for (int j = 0; j < buildingArray.length(); j++) {
                    String buildingCode = myTransList.get(i).getTerminal().split("-")[0];
                    if (buildingCode.equals(buildingArray.getJSONObject(j).getString("building_code"))) {
                        myTransList.get(i).setTerminal(buildingArray.getJSONObject(j).getString("building_name"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<PointValue> makeDayPointValues() {
        List<PointValue> myPointList = new ArrayList<>();

        if(myTransList.size() == 0) return myPointList;

        WatTransaction firstTrans = myTransList.get(0);
        LocalDateTime lastDate = firstTrans.getDateTime();
        PointValue myPoint = new PointValue((float) lastDate.getDayOfMonth(), -firstTrans.getAmount());

        for (int i = 1; i < myTransList.size(); i++) {
            WatTransaction currentTrans = myTransList.get(i);
            if (!lastDate.truncatedTo(ChronoUnit.DAYS).isEqual(currentTrans.getDateTime().truncatedTo(ChronoUnit.DAYS))) {
                myPoint.setLabel(NumberFormat.getCurrencyInstance(Locale.CANADA).format(myPoint.getY()));
                myPointList.add(myPoint);
                lastDate = currentTrans.getDateTime();
                myPoint = new PointValue(lastDate.getDayOfMonth(), -currentTrans.getAmount());
            } else {
                myPoint.set(myPoint.getX(), myPoint.getY() + -currentTrans.getAmount());
            }
        }
        myPoint.setLabel(NumberFormat.getCurrencyInstance(Locale.CANADA).format(myPoint.getY()));
        myPointList.add(myPoint);
        return myPointList;
    }

    public LineChartData makeTransChartData() {
        List<PointValue> myPoints = makeDayPointValues();

        Line line = new Line(myPoints).setColor(Color.parseColor("#F44336")).setCubic(false).setHasLabelsOnlyForSelected(true).setStrokeWidth(1);

        line.setPointRadius(3);
        List<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();

        data.setAxisXBottom(makeXAxis(myPoints));
        data.setLines(lines);
        return data;
    }

    public Axis makeXAxis(List<PointValue> myPoints) {
        Axis myXAxis = new Axis();
        myXAxis.setName("Day of Month");
        List<AxisValue> myXVals = new ArrayList<>();

        for (int i = 0; i < myPoints.size(); i++) {
            AxisValue tempAxisVal = new AxisValue(myPoints.get(i).getX());
            myXVals.add(tempAxisVal);
        }

        myXAxis.setValues(myXVals);

        return myXAxis;
    }


    public class Transaction implements Serializable {
        private String title;
        private int type;
        private DateTime date;
        private float amount;

        public Transaction(Element myElement) {
            String dateTime = myElement.getElementById("oneweb_financial_history_td_date").text() + " " + myElement.getElementById("oneweb_financial_history_td_time").text();
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.CANADA);
            DateTimeFormatter myDateFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
            try {
                date = DateTime.parse(dateTime, myDateFormat);
                amount = numberFormat.parse(myElement.getElementById("oneweb_financial_history_td_amount").text()).floatValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
            title = myElement.getElementById("oneweb_financial_history_td_terminal").text().substring(7);
            if (title.contains("WAT-FS")) {
                type = 0; // 0 = Meal Plan
                title = title.substring(7); // remove "WAT-FS"
            } else {
                type = 1; // 1 = Flex Dollars
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String newTitle) {
            title = newTitle;
        }

        public String getAmountString() {
            return NumberFormat.getCurrencyInstance(Locale.CANADA).format(amount);
        }

        public String getTimeString() {
            DateTimeFormatter myFormat = DateTimeFormat.forPattern("dd MMM 'at' h:mm aa");
            return myFormat.print(date);
        }

        public float getAmount() {
            return amount;
        }

        public DateTime getDate() {
            return date;
        }

        public String getTypeString() {
            switch (type) {
                case 0: {
                    return "Meal Plan";
                }
                default: {
                    return "Flex Dollars";
                }
            }
        }

        public int getType() {
            return type;
        }
    }

}
