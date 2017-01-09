package com.aastudio.investomater.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.aastudio.investomater.Databases.NSEDatabaseHelper;
import com.aastudio.investomater.Databases.NSEEntry;
import com.aastudio.investomater.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class newsFragment extends Fragment {

    private Spinner newsSpinner;
    private ListView newsListView;
    private Button findButton;

    private Context context;
    private ArrayList<NSEEntry> companies;
    private ArrayAdapter<String> companyNamesAdapter;
    private ArrayAdapter<String> newsAdapter;

    private ProgressDialog progressDialog;

    private ArrayList<News> news = new ArrayList<>();

    public newsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        newsSpinner = (Spinner) view.findViewById(R.id.spinner_news);
        newsListView = (ListView) view.findViewById(R.id.list_news);
        findButton = (Button) view.findViewById(R.id.button_find_news);

        NSEDatabaseHelper nseDatabaseHelper = new NSEDatabaseHelper(context);
        companies = nseDatabaseHelper.getAllEntries();

        String[] companyNames = new String[companies.size()];
        for (int i = 0; i < companies.size(); i++) {
            companyNames[i] = companies.get(i).getCompanyName();
        }
        companyNamesAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, companyNames);
        newsSpinner.setAdapter(companyNamesAdapter);

        newsAdapter = new ArrayAdapter<>(context, android.R.layout.activity_list_item, new String[]{});
        newsListView.setAdapter(newsAdapter);
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendToBrowser(i);
            }
        });

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Fetching news, please wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (companies.size() == 0) {
                    Toast.makeText(context, "Add stocks to My Stocks first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String query = companies.get(newsSpinner.getSelectedItemPosition()).getDataSetCode();
                String url = "https://www.google.co.in/finance/company_news?q=NSE:" + query + "&output=rss";
                FindNews findNews = new FindNews();
                findNews.execute(url);
            }
        });
    }

    private void sendToBrowser(int i) {
        String url = news.get(i).getLink();
        Log.v("LOG", url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    private class FindNews extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            newsAdapter = new ArrayAdapter<>(context, android.R.layout.activity_list_item, new String[]{});
            newsListView.setAdapter(newsAdapter);
            newsAdapter.notifyDataSetChanged();
            news.clear();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader;
            String jsonString = "";
            try {
                Log.d("LOG", params[0]);
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

                //Get String
                InputStream is = connection.getInputStream();

                if (is == null) {
                    return "error";
                }

                parser.setInput(is, null);

                int event = parser.getEventType();

                String temp = "";
                String title = "";

                while (event != XmlPullParser.END_DOCUMENT) {
                    String name = parser.getName();
                    switch (event) {
                        case XmlPullParser.TEXT:
                            temp = parser.getText();
                            break;
                        case XmlPullParser.END_TAG:
                            if (name.equals("title")) {
                                title = temp;
                            } else if (name.equals("link")) {
                                news.add(new News(title, temp));
                            }
                            break;
                        default:
                            break;
                    }
                    event = parser.next();
                }
                if (news.size() < 1) {
                    Log.v("LOG", "here9");
                    return "error";
                }
                return "success";

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
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
                    }
                });
                builder.show();
                return;
            }

            String[] titles = new String[news.size()];
            for (int i = 0; i < news.size(); i++) {
                titles[i] = news.get(i).getTitle();
            }
            Log.v("LOG", "adapter" + titles[1]);
            newsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, titles);
            newsListView.setAdapter(newsAdapter);
            newsAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(values[0]);

        }
    }

    private class News {
        private String title;
        private String link;

        public News(String title, String link) {
            this.title = title;
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
