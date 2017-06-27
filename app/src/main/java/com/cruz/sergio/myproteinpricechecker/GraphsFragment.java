package com.cruz.sergio.myproteinpricechecker;

import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ratan on 7/29/2015.
 */
public class GraphsFragment extends Fragment {
    ArrayList<ArrayList<Double>> priceValues_Array_arrayList = null;
    ArrayList<ArrayList<Long>> dates_Array_arrayList = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph_fragment, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.nanoTime();

        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();



        String query1 = "SELECT DISTINCT " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS +
                " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                " ORDER BY " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS;

        Cursor cursor1 = db.rawQuery(query1, null);
        int cursor1Count = cursor1.getCount();
        if (cursor1Count > 0) {
            List<String> distinct_id_products = new ArrayList<>();
            while (cursor1.moveToNext()) {
                String _id_poducts = cursor1.getString(cursor1.getColumnIndex(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS));
                distinct_id_products.add(_id_poducts);
            }
            priceValues_Array_arrayList = new ArrayList<>(cursor1Count);
            dates_Array_arrayList = new ArrayList<>(cursor1Count);

            for (int i = 0; i < distinct_id_products.size(); i++) {
                Cursor cursor2 = db.rawQuery("SELECT " +
                        ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE + " , " + ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE +
                        " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                        " WHERE " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS + " = '" + distinct_id_products.get(i) + "'" +
                        " ORDER BY " + ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE + " ASC", null);
                int cursor2Count = cursor2.getCount();
                if (cursor2Count > 0) {
                    ArrayList<Double> priceValues_arrayList = new ArrayList<>(cursor2Count);
                    ArrayList<Long> dates_arrayList = new ArrayList<>(cursor2Count);
                    while (cursor2.moveToNext()) {
                        double price_value = cursor2.getDouble(cursor2.getColumnIndex(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE));
                        long date_value = cursor2.getLong(cursor2.getColumnIndex(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE));
                        priceValues_arrayList.add(price_value);
                        dates_arrayList.add(date_value);
                    }
                    priceValues_Array_arrayList.add(priceValues_arrayList);
                    dates_Array_arrayList.add(dates_arrayList);

                } else {
                    // TODO Warning Empty database!
                }

                cursor2.close();
            }


        } else {
            // TODO Warning Empty database!
        }
        cursor1.close();


        db.close();
        long elapsed = System.nanoTime() - startTime;
        Log.d("Sergio>", this + " onCreate\npriceValues_Array_arrayList= " + priceValues_Array_arrayList);
        Log.d("Sergio>", this + " dates_Array_arrayList= " + dates_Array_arrayList + "\n");
        Log.d("Sergio>", this + " onCreate\nelapsed= " + elapsed);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        LineChart lineChart = (LineChart) getActivity().findViewById(R.id.line_chart);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setGridBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);
        lineChart.getDescription().setEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
//        LegendEntry[] legendEntry = new LegendEntry[]{"uno", };
//        legend.setCustom();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);


        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMaximum(900f);
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawGridLines(true);

        int numgraphs = dates_Array_arrayList.size();
        ArrayList<ILineDataSet> dataSets = new ArrayList<>(numgraphs);
        for (int k = 0; k < numgraphs; k++) {
            ArrayList<Long> datesArray = dates_Array_arrayList.get(k);
            ArrayList<Double> pricesArray = priceValues_Array_arrayList.get(k);

            List<Entry> entries = new ArrayList<>();
            int datasize = dates_Array_arrayList.get(k).size();
            for (int i = 0; i < datasize; i++) {
                // cada ponto -> (x,y) => Entry(float x, float y);
                entries.add(new Entry(datesArray.get(i).floatValue(), pricesArray.get(i).floatValue()));
            }

            LineDataSet lineDataSet = new LineDataSet(entries, "Product one " + k); // add entries to dataset
            lineDataSet.setMode(LineDataSet.Mode.LINEAR);
            dataSets.add(lineDataSet);
        }

        LineData data = new LineData(dataSets);
        lineChart.setData(data);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
