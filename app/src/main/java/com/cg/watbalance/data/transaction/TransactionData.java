package com.cg.watbalance.data.transaction;

import android.graphics.Color;

import org.threeten.bp.LocalDateTime;

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
    private boolean exception;

    public List<PointValue> makeDayPointValues() {
        List<PointValue> myPointList = new ArrayList<>();

        if (myTransList.size() == 0) return myPointList;

        WatTransaction firstTrans = myTransList.get(0);
        LocalDateTime lastDate = firstTrans.getDateTime();
        PointValue myPoint = new PointValue((float) lastDate.getDayOfMonth(), -firstTrans.getAmount());

        for (int i = 1; i < myTransList.size(); i++) {
            WatTransaction currentTrans = myTransList.get(i);

            if (!currentTrans.getTerminal().contains("WAT-NEWFRONT")) {
                if (!lastDate.toLocalDate().isEqual(currentTrans.getDateTime().toLocalDate())) {
                    myPoint.setLabel(NumberFormat.getCurrencyInstance(Locale.CANADA).format(myPoint.getY()));
                    myPointList.add(myPoint);

                    lastDate = currentTrans.getDateTime();
                    myPoint = new PointValue(lastDate.getDayOfMonth(), -currentTrans.getAmount());
                } else {
                    myPoint.set(myPoint.getX(), myPoint.getY() + -currentTrans.getAmount());
                }
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

    public List<WatTransaction> getTransList() {
        return myTransList;
    }

    public void setTransList(final WatAccount myAccount) {
        exception = false;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int days = LocalDateTime.now().getDayOfMonth() - 1;
                myTransList = myAccount.getLastDaysTransactions(days, false);
            }
        });

        t.start();

        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
