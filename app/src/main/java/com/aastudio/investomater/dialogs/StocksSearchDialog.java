package com.aastudio.investomater.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.aastudio.investomater.Databases.NSEDatabaseHelper;
import com.aastudio.investomater.Databases.NSEEntry;
import com.aastudio.investomater.R;

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

/**
 * Created by Abhidnya on 1/7/2017.
 */

public class StocksSearchDialog extends Dialog implements View.OnClickListener {

    private final static String API_KEY = "9sx2RnvGXagD8LeiTGxP";

    private int type;
    private String market;

    private EditText queryEt;
    private Button searchButton;
    private Button dismissButton;
    private ListView searchResultList;
    private ArrayAdapter<String> searchResultAdapter;
    private ArrayList<NSEEntry> resultsFinal;
    private String name;
    private String databaseCode;
    private String datasetCode;

    private ProgressDialog progressDialog;

    private String response;

    public StocksSearchDialog(@NonNull Context context, int type, String market) {
        super(context);
        setContentView(R.layout.dialog_stock_search);
        setTitle("My Stocks");
        getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;

        this.type = type;
        this.market = market;

        resultsFinal = new ArrayList<>();

        queryEt = (EditText) findViewById(R.id.et_search_stock);
        searchButton = (Button) findViewById(R.id.button_search);
        dismissButton = (Button) findViewById(R.id.dialog_dismiss);
        searchResultList = (ListView) findViewById(R.id.list_search_result);

        searchButton.setOnClickListener(this);
        dismissButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Fetching data, please wait.");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        //List
        searchResultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String CompanyName = searchResultAdapter.getItem(i);
                dismiss();
                progressDialog.setTitle("Adding the Stock");
                progressDialog.show();

                NSEEntry temp = resultsFinal.get(i);

                NSEDatabaseHelper nseDatabaseHelper = new NSEDatabaseHelper(view.getContext());
                resultsFinal.clear();
                resultsFinal = nseDatabaseHelper.getAllEntries();
                boolean repeat = false;
                for (int j = 0; j < resultsFinal.size(); j++) {
                    if (resultsFinal.get(j).getCompanyName().equals(temp.getCompanyName())) {
                        repeat = true;
                        break;
                    }
                }
                if (repeat) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("You have already added this stock.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            progressDialog.dismiss();
                        }
                    });
                    builder.show();
                    return;
                }
                nseDatabaseHelper.addNSEEntry(temp);
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_search) {
            Search search = new Search();
            progressDialog.show();
            search.execute(makeQuery(queryEt.getText().toString().trim()));
        }
        if (view.getId() == R.id.dialog_dismiss) {
            dismiss();
        }
    }

    private String makeQuery(String query) {
        query = query.trim().replace(" ", "+");
        return "https://www.quandl.com/api/v3/datasets.json?query=" + query + "&database_code=NSE&per_page=5&page=1";
    }

    class Search extends AsyncTask<String, String, String> {

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

                //Get String
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

                jsonString = sb.toString();
                JSONObject jsonObject = new JSONObject(jsonString);

                return jsonObject.toString();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("No data available or an error occurred :(");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
                builder.show();
                return;
            }
            resultsFinal.clear();
            response = param;
            JSONObject jsonObject = null;
            ArrayList<String> results = new ArrayList<>();
            try {
                jsonObject = new JSONObject(response);
                JSONArray dataSets = jsonObject.getJSONArray("datasets");
                for (int i = 0; i < dataSets.length(); i++) {
                    jsonObject = (dataSets.getJSONObject(i));
                    name = jsonObject.getString("name");
                    databaseCode = jsonObject.getString("database_code");
                    datasetCode = jsonObject.getString("dataset_code");
                    results.add(jsonObject.getString("name"));
                    resultsFinal.add(new NSEEntry(name, databaseCode, datasetCode));
                }
                searchResultAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, results);
                searchResultList.setAdapter(searchResultAdapter);
                searchResultAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0]);

        }
    }
}
