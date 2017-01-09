package com.aastudio.investomater.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.aastudio.investomater.Databases.MyData;
import com.aastudio.investomater.R;

import static android.view.View.GONE;


/**
 * Created by Abhidnya on 1/7/2017.
 */

public class NewStockDialog extends Dialog implements View.OnClickListener {

    private Spinner typeSpinner;
    private Spinner countrySpinner;
    private Spinner marketSpinner;
    private TextView alert;

    private Button nextButton;
    public AdapterView.OnItemSelectedListener stockTypeSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                alert.setVisibility(GONE);
                nextButton.setClickable(false);
                countrySpinner.setVisibility(GONE);
                marketSpinner.setVisibility(GONE);
                return;
            }
            if (i == 1) {
                alert.setVisibility(GONE);
                countrySpinner.setVisibility(View.VISIBLE);
                ArrayAdapter<String> countrySpinnerAdapter = new ArrayAdapter<String>(view.getContext(),
                        R.layout.support_simple_spinner_dropdown_item, MyData.StockData.countries);
                countrySpinner.setAdapter(countrySpinnerAdapter);
            } else {
                countrySpinner.setVisibility(GONE);
                marketSpinner.setVisibility(GONE);
                alert.setVisibility(View.VISIBLE);
                nextButton.setClickable(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    public AdapterView.OnItemSelectedListener countrySpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                alert.setVisibility(GONE);
                nextButton.setClickable(false);
                return;
            }
            if (i == 1) {
                alert.setVisibility(GONE);
                marketSpinner.setVisibility(View.VISIBLE);
                ArrayAdapter<String> marketSpinnerAdapter = new ArrayAdapter<String>(view.getContext(),
                        R.layout.support_simple_spinner_dropdown_item, MyData.StockData.India);
                marketSpinner.setAdapter(marketSpinnerAdapter);
            } else {
                marketSpinner.setVisibility(GONE);
                alert.setVisibility(View.VISIBLE);
                nextButton.setClickable(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    public AdapterView.OnItemSelectedListener marketSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                alert.setVisibility(GONE);
                nextButton.setClickable(false);
                return;
            }
            if (i == 1) {
                alert.setVisibility(GONE);
                nextButton.setClickable(true);
            } else {
                alert.setVisibility(View.VISIBLE);
                nextButton.setClickable(false);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
    private Button cancelButton;

    public NewStockDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.layout_new_stock);

        setTitle("Add New Stock");

        typeSpinner = (Spinner) this.findViewById(R.id.spinner_newstock_type);
        countrySpinner = (Spinner) this.findViewById(R.id.spinner_newstock_country);
        marketSpinner = (Spinner) this.findViewById(R.id.spinner_newstock_market);
        alert = (TextView) this.findViewById(R.id.tv_not_available);

        nextButton = (Button) this.findViewById(R.id.dialog_next);
        cancelButton = (Button) this.findViewById(R.id.dialog_cancel);
        nextButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(context,
                R.layout.support_simple_spinner_dropdown_item, MyData.getTYPES());
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(stockTypeSpinnerListener);
        countrySpinner.setOnItemSelectedListener(countrySpinnerListener);
        marketSpinner.setOnItemSelectedListener(marketSpinnerListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_next:
                StocksSearchDialog stocksSearchDialog = new StocksSearchDialog(view.getContext(), 1, marketSpinner.getSelectedItem().toString());
                stocksSearchDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        dismiss();
                    }
                });
                stocksSearchDialog.show();
                typeSpinner.setVisibility(GONE);
                countrySpinner.setVisibility(GONE);
                marketSpinner.setVisibility(GONE);
                break;

            case R.id.dialog_cancel:
                dismiss();
                break;

            default:
                break;
        }
    }
}
