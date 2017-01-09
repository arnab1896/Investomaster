package com.aastudio.investomater.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aastudio.investomater.Databases.NSEDatabaseHelper;
import com.aastudio.investomater.Databases.NSEEntry;
import com.aastudio.investomater.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

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

public class CompareFragment extends Fragment {

    NSEEntry info;
    ArrayList<NSEEntry> companies;
    ArrayAdapter<String> companyNamesAdapter1;
    ArrayAdapter<String> companyNamesAdapter2;
    private ProgressDialog progressDialog;
    private Context context;
    private ArrayList<Data> dataPoints1;
    private ArrayList<Data> dataPoints2;
    private String startDate;
    private NSEDatabaseHelper nseDatabaseHelper;
    private Spinner stock1;
    private Spinner stock2;
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (companies.size() == 0) {
                Toast.makeText(context, "Add stocks to MY Stocks first", Toast.LENGTH_SHORT).show();
                return;
            }
            NSEEntry company1 = companies.get(stock1.getSelectedItemPosition());
            NSEEntry company2 = companies.get(stock2.getSelectedItemPosition());

            if (company1.getCompanyName().equals(company2.getCompanyName())) {
                Toast.makeText(context, "Choose different companies", Toast.LENGTH_SHORT).show();
                return;
            }

            GetInfoQ1 getInfoQ1 = new GetInfoQ1();
            getInfoQ1.execute(company1.getDataSetCode());
            GetInfoQ2 getInfoQ2 = new GetInfoQ2();
            getInfoQ2.execute(company2.getDataSetCode());
        }
    };
    private Button compareButton;
    private LineChart lineChart;
    private TextView tvError;
    private ImageButton refreshButton;


    public CompareFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compare, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        if (!checkConnection()) {
            //no connection
            AlertDialog.Builder noNetDialogBuilder = new AlertDialog.Builder(context);
            noNetDialogBuilder.setCancelable(false);
            noNetDialogBuilder.setTitle("No Connection");
            noNetDialogBuilder.setMessage("Internet connection is required. Please check your connection.");
            noNetDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            noNetDialogBuilder.show();
        } else {
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Fetching Data");
            progressDialog.setCancelable(false);
            tvError = (TextView) view.findViewById(R.id.tv_error_compare);
            dataPoints1 = new ArrayList<>();
            dataPoints2 = new ArrayList<>();

            lineChart = (LineChart) view.findViewById(R.id.chart_compare);

            stock1 = (Spinner) view.findViewById(R.id.spinner_stock1);
            stock2 = (Spinner) view.findViewById(R.id.spinner_stock2);

            nseDatabaseHelper = new NSEDatabaseHelper(context);
            companies = nseDatabaseHelper.getAllEntries();

            String[] companyNames = new String[companies.size()];
            for (int i = 0; i < companies.size(); i++) {
                companyNames[i] = companies.get(i).getCompanyName();
            }

            companyNamesAdapter1 = new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_1, companyNames);
            companyNamesAdapter2 = new ArrayAdapter<String>(context,
                    android.R.layout.simple_list_item_1, companyNames);

            stock1.setAdapter(companyNamesAdapter1);
            stock2.setAdapter(companyNamesAdapter2);

            compareButton = (Button) view.findViewById(R.id.button_compare);
            compareButton.setOnClickListener(onClickListener);
        }
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void graphMaker(ArrayList<Data> points1, ArrayList<Data> points2) {
        Collections.reverse(points1);
        Collections.reverse(points2);
        //chart
        List<Entry> entries1 = new ArrayList<>();
        for (int i = 0; i < points1.size(); i++) {
            entries1.add(new Entry(i, Float.valueOf(points1.get(i).getClose())));
        }
        LineDataSet lineDataSet1 = new LineDataSet(entries1, "Stock 1");
        lineDataSet1.setColor(Color.MAGENTA);

        List<Entry> entries2 = new ArrayList<>();
        for (int i = 0; i < points2.size(); i++) {
            entries2.add(new Entry(i, Float.valueOf(points2.get(i).getClose())));
        }
        LineDataSet lineDataSet2 = new LineDataSet(entries2, "Stock 2");
        lineDataSet2.setColor(Color.BLUE);

        //add entries
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);
        dataSets.add(lineDataSet2);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.animateX(10000);
        lineChart.invalidate();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.TRANSPARENT);
    }

    private class GetInfoQ1 extends AsyncTask<String, Void, String> {

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
                    dataPoints1.add(new Data(temp.getString(0), temp.getString(1)));
                    string += dataPoints1.get(i).getDate() + "\n";
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
            if (param.equals("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("No data available or an error occurred :(");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }
    }

    private class GetInfoQ2 extends AsyncTask<String, Void, String> {

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
                    dataPoints2.add(new Data(temp.getString(0), temp.getString(1)));
                    string += dataPoints2.get(i).getDate() + "\n";
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
                    }
                });
                builder.show();
            }
            graphMaker(dataPoints1, dataPoints2);
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
