package com.aastudio.investomater.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aastudio.investomater.R;


public class AboutFragment extends Fragment {

    TextView emailAA;
    TextView emailAB;

    public AboutFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailAA = (TextView) view.findViewById(R.id.tv_email_aa);
        emailAB = (TextView) view.findViewById(R.id.tv_email_ab);

        emailAA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail("abhidnya@ymail.com");
            }
        });
        emailAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMail("arnab1896@gmail.com");
            }
        });
    }

    private void sendMail(String mail) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_EMAIL, mail);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Investomaster");

        startActivity(Intent.createChooser(intent, "Send Email"));
    }
}
