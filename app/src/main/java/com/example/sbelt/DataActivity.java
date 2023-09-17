package com.example.sbelt;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sbelt.utils.GestureData;
import com.example.sbelt.utils.utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DataActivity extends AppCompatActivity {

    public class IntPair {
        public int day;
        public int amount;
        public IntPair(int day, int amount){
            this.day = day;
            this.amount = amount;
        }
    }
    LineChart lineChart;
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Creating the new activity!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        lineChart = findViewById(R.id.lineChart);
        ArrayList<GestureData> dataList = getIntent().getParcelableArrayListExtra("Data");
        buildWeeklyChart(dataList);
    }

    private void buildWeeklyChart(List<GestureData> dataList) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date oneWeekAgo = calendar.getTime();
        if (dataList == null)
            dataList = new ArrayList<>();
        List<IntPair> filteredData = dataList.stream()
                .filter(gestureData -> gestureData.startDate.after(oneWeekAgo))
                .map((data) -> new IntPair((int) ((data.startDate.getTime() - oneWeekAgo.getTime()) / (24 * 60 * 60 * 1000)), data.amount))
                .collect(Collectors.groupingBy(pair -> pair.day, Collectors.summingInt(pair -> pair.amount)))
                .entrySet()
                .stream()
                .map(entry -> new IntPair(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(pair -> pair.day))
                .collect(Collectors.toList());

        Iterator<IntPair> iterator = filteredData.iterator();
        IntPair curr;
        if (!iterator.hasNext()) {
            Description description = new Description();
            description.setText("No data available");
            lineChart.setDescription(description);
            lineChart.invalidate();
            return;
        }
        else curr = iterator.next();
        ArrayList<Entry> entries = new ArrayList<>();
        int amount;
        for(int i=0;i<8;i++){
            if(curr.day==i) {
                amount = curr.amount;
                if(iterator.hasNext())
                    curr = iterator.next();
            }
            else
                amount = 0;
            entries.add(new Entry(i,amount));
        }
        LineDataSet lineDataSet = new LineDataSet(entries, "Gestures");
        lineDataSet.setCircleColors(Color.BLUE);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(16f);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setLineWidth(3f);

        LineData lineData = new LineData(lineDataSet);

        lineChart.setData(lineData);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.setVisibleXRange(0f, 7f);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setText("");
        lineChart.setScaleEnabled(false);


        LinkedList<String> lst = utils.WEEKDAYS;
        Calendar today = Calendar.getInstance();
        int todayNum = today.get(Calendar.DAY_OF_WEEK);
        String currentDay = lst.get(todayNum-1);
        for(int i=0;i<todayNum;i++){
            lst.addLast(lst.removeFirst());
        }
        lst.addLast(currentDay);
        lst.forEach(System.out::println);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(lst));
        xAxis.setDrawAxisLine(true);
        xAxis.setLabelRotationAngle(330f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        lineChart.invalidate();
    }

    public void exitDataActivity(View view){
        finish();
    }
}
