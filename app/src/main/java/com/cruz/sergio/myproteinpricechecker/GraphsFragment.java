package com.cruz.sergio.myproteinpricechecker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ratan on 7/29/2015.
 */
public class GraphsFragment extends Fragment {
    ArrayList<ArrayList<Double>> priceValues_Array_arrayList = null;
    ArrayList<ArrayList<Long>> dates_Array_arrayList = null;
    ArrayList<String> product_names_arrayList = null;
    ArrayList<String> currency_symb_arrayList = null;
    boolean[] which_checked = null;
    LineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph_fragment, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getChartDataFromDB(getContext());

        SearchFragment sf = new SearchFragment();
        sf.setUpdateGraphListener(new SearchFragment.UpdateGraphForNewProduct() {
            @Override
            public void onProductAdded(Boolean addedNew) {
                Log.w("Sergio>", this + "\n" + "addedNewProduct= " + addedNew);
                lineChart.clear();
                lineChart.notifyDataSetChanged();
                getChartDataFromDB(getContext());
                placeDataToChart(lineChart);
            }
        });

        WatchingFragment wf = new WatchingFragment();
        wf.setDeleteProductlistener(new WatchingFragment.DeletedProductListener() {
            @Override
            public void onProductDeleted(Boolean deleted) {
                Log.w("Sergio>", this + "\n" + "onProductDeleted= " + deleted);
                lineChart.clear();
                lineChart.notifyDataSetChanged();
                getChartDataFromDB(getContext());
                placeDataToChart(lineChart);
            }
        });

    }

    // Função chamada quando altera a visibilidade do fragmento
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            lineChart.setVisibility(View.VISIBLE);
//            lineChart.animateXY(2000, 2000, Easing.EasingOption.EaseInOutBack, Easing.EasingOption.EaseInOutBack);
//        } else {
//            if (lineChart != null) {
//                lineChart.setVisibility(View.INVISIBLE);
//            }
//        }
//    }

    public void getChartDataFromDB(Context context) {
        long startTime = System.nanoTime();

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query1 = "SELECT DISTINCT " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS +
                " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                " ORDER BY " + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS;

        Cursor cursor1 = db.rawQuery(query1, null);
        int cursor1Count = cursor1.getCount();
        if (cursor1Count > 0) {
            List<String> distinct_id_products = new ArrayList<>();
            which_checked = new boolean[cursor1Count];
            int w = 0;
            while (cursor1.moveToNext()) {
                String _id_products = cursor1.getString(cursor1.getColumnIndex(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS));
                distinct_id_products.add(_id_products);
                which_checked[w++] = true;
            }
            priceValues_Array_arrayList = new ArrayList<>(cursor1Count);
            dates_Array_arrayList = new ArrayList<>(cursor1Count);
            product_names_arrayList = new ArrayList<>(cursor1Count);
            currency_symb_arrayList = new ArrayList<>(cursor1Count);

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
                    // TODO ERROR!
                }
                cursor2.close();

                Cursor cursor3 = db.rawQuery("SELECT " +
                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME + " , " + ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL +
                        " FROM " + ProductsContract.ProductsEntry.TABLE_NAME +
                        " WHERE " + ProductsContract.ProductsEntry._ID + " = '" + distinct_id_products.get(i) + "'", null);

                int cursor3Count = cursor3.getCount();
                if (cursor3Count > 0) {
                    while (cursor3.moveToNext()) {
                        String name = cursor3.getString(cursor3.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
                        String symbol = cursor3.getString(cursor3.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL));
                        product_names_arrayList.add(name);
                        currency_symb_arrayList.add(symbol);
                    }
                } else {
                    // TODO ERROR!
                }
                cursor3.close();
            }

        } else {
            // TODO Warning Empty database!
        }
        cursor1.close();
        db.close();

        Log.d("Sergio>", this + "\n" +
                "onCreate Elapsed Millis= " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }

    public class CustomGraphMarker extends MarkerView implements IMarker {
        private TextView marker_txt;

        public CustomGraphMarker(Context context, int layoutResource) {
            super(context, layoutResource);
            marker_txt = (TextView) findViewById(R.id.marker_tv);
        }

        private MPPointF mOffset = null;

        @Override
        public MPPointF getOffset() {
            // center the marker horizontally and vertically
            if (mOffset == null) {
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }
            return mOffset;
        }

        @Override
        public MPPointF getOffsetForDrawingAtPoint(float v, float v1) {
            return null;
        }

        @Override
        public void refreshContent(Entry entry, Highlight highlight) {
            marker_txt.setText("" + entry.getY() + "\n" +
                    entry.getX());

            // this will perform necessary layouting
            super.refreshContent(entry, highlight);
        }

        @Override
        public void draw(Canvas canvas, float v, float v1) {

        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        lineChart = (LineChart) getActivity().findViewById(R.id.line_chart);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setGridBackgroundColor(Color.WHITE);
        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.setPinchZoom(false);        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setNoDataText("Empty DataBase! Add products to track their prices.");
        //lineChart.animateXY(3000, 3000, Easing.EasingOption.EaseInOutBack, Easing.EasingOption.EaseInOutBack);
        lineChart.setDrawBorders(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setDrawInside(true);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);

        CustomGraphMarker graphMarker = new CustomGraphMarker(this.getContext(), R.layout.graph_marker);
        lineChart.setMarker(graphMarker);
        lineChart.setHighlightPerTapEnabled(true);
        lineChart.setHighlightPerDragEnabled(true);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        IAxisValueFormatter ivf = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                //DateFormat df = DateFormat.getDateInstance();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM"); // dia Mês 10 jul
                return sdf.format(new Date((long) v));
            }

        };
        xAxis.setValueFormatter(ivf);
        xAxis.setDrawLimitLinesBehindData(false);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawZeroLine(true);
        yAxis.setDrawGridLines(true);
        //yAxis.setSpaceTop(20);

        placeDataToChart(lineChart);

    }

    public void placeDataToChart(final LineChart lineChart) {
        class DataObj {
            long x;

            DataObj(long x) {
                this.x = x;
            }
        }

        if (dates_Array_arrayList != null && priceValues_Array_arrayList != null) {
            int numgraphs = dates_Array_arrayList.size();

            ArrayList<Integer> fColors = new ArrayList(numgraphs);
            int[] f_colors = getActivity().getResources().getIntArray(R.array.f_colors_xml);

            int p = numgraphs / f_colors.length + 1;
            for (int i = 0; i < p; i++) {
                for (int m = 0; m < f_colors.length; m++) {
                    fColors.add(f_colors[m]);
                }
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>(numgraphs);
            for (int k = 0; k < numgraphs; k++) {

                if (which_checked[k]) {
                    ArrayList<Long> datesArray = dates_Array_arrayList.get(k);
                    ArrayList<Double> pricesArray = priceValues_Array_arrayList.get(k);

                    List<Entry> entries = new ArrayList<>();
                    int datasize = dates_Array_arrayList.get(k).size();
                    for (int i = 0; i < datasize; i++) {

                        // cada ponto -> (x,y) => Entry(float x, float y);
                        float y = pricesArray.get(i).floatValue();

                        if (i > 0 && y > 0) {
                            float y_1 = pricesArray.get(i - 1).floatValue();
                            if (y != y_1) {
                                Long x = datesArray.get(i);
                                entries.add(new Entry(x.floatValue(), y, new DataObj(x)));
                            }
                        }

                        if (i == 0 || i == datasize - 1) {
                            Long x = datesArray.get(i);
                            entries.add(new Entry(x.floatValue(), y, new DataObj(x)));
                        }

                    }

                    LineDataSet lineDataSet = new LineDataSet(entries, product_names_arrayList.get(k)); // add entries to dataset
                    lineDataSet.setMode(LineDataSet.Mode.STEPPED);
                    lineDataSet.setCircleColor(fColors.get(k));
                    lineDataSet.setCircleColorHole(fColors.get(k));
                    lineDataSet.setColor(fColors.get(k));
                    lineDataSet.setLineWidth(2f);
                    lineDataSet.setHighlightEnabled(true);

                    dataSets.add(lineDataSet);
                }

            }

            lineChart.getAxisLeft().setSpaceTop(2 * dataSets.size());

            ArrayList<ILineDataSet> allLineDataSets = (ArrayList<ILineDataSet>) dataSets.clone();

            LineData data = new LineData(dataSets);
            lineChart.setData(data);

            graphs_options(lineChart, dataSets, allLineDataSets);

            final CheckBox gCheckBox = (CheckBox) getActivity().findViewById(R.id.g_checkBox);
            gCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lineChart.getLegend().setEnabled(gCheckBox.isChecked());
                    lineChart.invalidate();
                }
            });
        }
    }

    public void graphs_options(final LineChart lineChart, final ArrayList<ILineDataSet> dataSets, final ArrayList<ILineDataSet> allLineDataSets) {
        Button openOptions = (Button) getActivity().findViewById(R.id.g_options_button);
        openOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean[] in_wich = which_checked.clone();
                CharSequence[] dialogList = product_names_arrayList.toArray(new CharSequence[product_names_arrayList.size()]);

                AlertDialog.Builder builderDialog = new AlertDialog.Builder(getActivity());
                builderDialog.setTitle("Select Items to display");

                // Creating multiple selection by using setMultiChoiceItem method
                builderDialog.setMultiChoiceItems(dialogList, which_checked, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {

                    }
                });

                builderDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataSets.clear();

                        for (int i = 0; i < which_checked.length; i++) {
                            if (which_checked[i]) {
                                dataSets.add(allLineDataSets.get(i));
                            }
                        }

                        lineChart.clear();
                        lineChart.notifyDataSetChanged();

                        LineData data = new LineData(dataSets);
                        lineChart.setData(data);
                        lineChart.getAxisLeft().resetAxisMaximum(); // Y Axis
                        lineChart.getXAxis().resetAxisMinimum();

                    }
                });

                builderDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                which_checked = in_wich.clone();
                            }

                        });

                AlertDialog alert = builderDialog.create();
                alert.show();

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
