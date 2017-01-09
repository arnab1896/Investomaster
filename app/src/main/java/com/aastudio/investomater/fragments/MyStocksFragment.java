package com.aastudio.investomater.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aastudio.investomater.Databases.NSEDatabaseHelper;
import com.aastudio.investomater.Databases.NSEEntry;
import com.aastudio.investomater.R;
import com.aastudio.investomater.StockAnalyser;
import com.aastudio.investomater.dialogs.NewStockDialog;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyStocksFragment extends Fragment {

    public static ArrayAdapter<String> stocksAdapter;

    FloatingActionButton newStockFab;
    ListView myStocksList;
    ProgressDialog dialog;
    private Context context;
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(context, StockAnalyser.class);
            intent.putExtra("position", i);
            context.startActivity(intent);
        }
    };
    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.inflate(R.menu.list_long_press_menu);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return false;
                }
            });
            return true;
        }
    };

    public MyStocksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_stocks, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getContext();

        myStocksList = (ListView) view.findViewById(R.id.frag_mystock_listview);
        stocksAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_list_item_1, new String[]{});
        myStocksList.setAdapter(stocksAdapter);
        myStocksList.setOnItemClickListener(onItemClickListener);
        myStocksList.setOnItemLongClickListener(onItemLongClickListener);

        newStockFab = (FloatingActionButton) view.findViewById(R.id.fab_new_stock);
        newStockFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewStockDialog dialog = new NewStockDialog(view.getContext());
                //StocksSearchDialog dialog = new StocksSearchDialog(view.getContext(), 1, "NSE");
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        updateList();
                    }
                });
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        updateList();
                        return true;
                    }
                });
            }
        });

        updateList();
    }

    public void updateList() {
        Update update = new Update();
        update.execute();
    }

    private class Update extends AsyncTask<Void, Integer, ArrayList<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Updating Stocks");
            dialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            NSEDatabaseHelper nseDatabaseHelper = new NSEDatabaseHelper(getContext());
            ArrayList<String> companies = new ArrayList<>();
            ArrayList<NSEEntry> entries = nseDatabaseHelper.getAllEntries();
            int count = entries.size();
            for (int i = 0; i < count; i++) {
                companies.add(entries.get(i).getCompanyName());
                publishProgress(i / count * 100);
            }
            return companies;
        }

        @Override
        protected void onPostExecute(ArrayList<String> param) {
            super.onPostExecute(param);
            dialog.dismiss();
            stocksAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_list_item_1, param);
            myStocksList.setAdapter(stocksAdapter);
            stocksAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
        }
    }
}
