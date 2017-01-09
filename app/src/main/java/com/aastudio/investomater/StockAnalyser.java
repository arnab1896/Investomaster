package com.aastudio.investomater;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aastudio.investomater.Databases.NSEDatabaseHelper;
import com.aastudio.investomater.Databases.NSEEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockAnalyser extends AppCompatActivity {

    NSEEntry info;
    private ProgressDialog progressDialog;
    private Context context;
    private JSONObject data;
    private ArrayList<Data> dataPoints;
    private String startDate;
    private String endDate;
    private NSEDatabaseHelper nseDatabaseHelper;
    private TextView tvCurrentPrice;
    private TextView tvDifference;
    private TextView tvDifferencePer;
    private TextView tvDate;
    private TextView tvError;
    private ImageButton refreshButton;
    private LineChart lineChart;

    private int position;

    private GetInfoQ getInfoQ;
    private GetInfo getInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_analyser);

        if (!checkConnection()) {
            //no connection
            AlertDialog.Builder noNetDialogBuilder = new AlertDialog.Builder(this);
            noNetDialogBuilder.setCancelable(false);
            noNetDialogBuilder.setTitle("No Connection");
            noNetDialogBuilder.setMessage("Internet connection is required. Please check your connection.");
            noNetDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            noNetDialogBuilder.show();
        }

        context = this;

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);

        nseDatabaseHelper = new NSEDatabaseHelper(this);
        ArrayList<NSEEntry> entries = nseDatabaseHelper.getAllEntries();
        info = entries.get(position);

        this.setTitle(info.getCompanyName());

        dataPoints = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fetching Data");
        progressDialog.setCancelable(false);

        tvCurrentPrice = (TextView) findViewById(R.id.tv_current_price);
        tvDifference = (TextView) findViewById(R.id.tv_difference);
        tvDifferencePer = (TextView) findViewById(R.id.tv_difference_percentage);
        tvDate = (TextView) findViewById(R.id.tv_date);
        tvError = (TextView) findViewById(R.id.tv_error);
        lineChart = (LineChart) findViewById(R.id.chart);
        refreshButton = (ImageButton) findViewById(R.id.button_refresh);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = makeURL(info.getDataSetCode());
                GetInfo getInfo = new GetInfo();
                getInfo.execute(url);
                Toast.makeText(context, "Refreshing", Toast.LENGTH_SHORT).show();
            }
        });

        String url = makeURL(info.getDataSetCode());
        getInfo = new GetInfo();
        getInfo.execute(url);
        getInfoQ = new GetInfoQ();
        getInfoQ.execute(info.getDataSetCode());
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getInfo.cancel(true);
        getInfoQ.cancel(true);
    }

    private String makeURL(String query) {
        return "http://finance.google.com/finance/info?client=ig&q=NSE:" + query;
    }

    private String makeURL2(String query) {
        return "https://www.quandl.com/api/v3/datasets/NSE/" + query + "/data.json";
    }

    public void graphMaker(ArrayList<Data> points) {
        Collections.reverse(points);
        //chart
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            entries.add(new Entry(i, Float.valueOf(points.get(i).getClose())));
        }
        LineDataSet lineDataSet = new LineDataSet(entries, "Closing Values");
        lineDataSet.setColor(Color.MAGENTA);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.animateX(1000);
        lineChart.invalidate();
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.TRANSPARENT);
        ///////
    }

    private class GetInfo extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader;
            String jsonString = "";
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                InputStream is = connection.getInputStream();
                StringBuilder sb = new StringBuilder();
                if (is == null) {
                    return "error";
                }
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String input;
                while ((input = reader.readLine()) != null) {
                    sb.append(input);
                }
                if (sb.length() == 0) {
                    Log.d("LOG", "here1");
                    return "error";
                }
                return sb.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return "error";
        }

        @Override
        protected void onPostExecute(String param) {
            super.onPostExecute(param);
            if (param.equals("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("No data available or an error occurred :(");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.show();
            }
            try {
                data = new JSONObject(param.substring(4, param.length() - 1));
                tvDate.setText(data.getString("lt"));
                double diff = Double.valueOf(data.getString("c"));
                tvCurrentPrice.setText(data.getString("l"));
                tvDifference.setText(data.getString("c"));
                tvDifferencePer.setText("(");
                tvDifferencePer.append(data.getString("cp"));
                tvDifferencePer.append(")");
                tvDifferencePer.append("%");

                if (diff >= 0) {
                    tvDifference.setTextColor(Color.GREEN);
                    tvDifferencePer.setTextColor(Color.GREEN);
                } else {

                    tvDifference.setTextColor(Color.RED);
                    tvDifferencePer.setTextColor(Color.RED);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetInfoQ extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            tvError.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader;
            String jsonString = "";
            try {
                URL url = new URL("https://www.quandl.com/api/v3/datasets/NSE/" + params[0] + ".json");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                InputStream is = connection.getInputStream();
                StringBuilder sb = new StringBuilder();
                if (is == null) {
                    return "error";
                }
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String input;
                while ((input = reader.readLine()) != null) {
                    sb.append(input);
                }
                if (sb.length() == 0) {
                    return "error";
                }

                JSONObject data = new JSONObject(sb.toString());
                data = data.getJSONObject("dataset");
                startDate = data.getString("newest_available_date");

                url = new URL("https://www.quandl.com/api/v3/datasets/NSE/" +
                        params[0] + ".json?column_index=5&limit=30&start_date" + startDate +
                        "collapse=daily&transform=none&api_key=9sx2RnvGXagD8LeiTGxP");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                is = connection.getInputStream();
                sb = new StringBuilder();
                if (is == null) {
                    return "error";
                }
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((input = reader.readLine()) != null) {
                    sb.append(input);
                }
                if (sb.length() == 0) {
                    return "error";
                }

                data = new JSONObject(sb.toString());
                data = data.getJSONObject("dataset");
                JSONArray dataArray = data.getJSONArray("data");
                String string = "";
                JSONArray temp;
                for (int i = 0; i < 30; i++) {
                    temp = dataArray.getJSONArray(i);
                    dataPoints.add(new Data(temp.getString(0), temp.getString(1)));
                    string += dataPoints.get(i).getDate() + "\n";
                }
                Log.v("LOG", string);
                return "success";

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return "error";
        }

        @Override
        protected void onPostExecute(String param) {
            super.onPostExecute(param);
            progressDialog.dismiss();
            if (param.equals("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("No data available or an error occurred :(");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.show();
            }
            graphMaker(dataPoints);
        }
    }

    private class Data {
        private String date;
        private String close;

        public Data(String date, String close) {
            this.date = date;
            this.close = close;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getClose() {
            return close;
        }

        public void setClose(String close) {
            this.close = close;
        }
    }
}
