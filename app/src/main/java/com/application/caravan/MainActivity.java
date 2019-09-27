package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting up button to open Passenger Add screen
        final AppCompatButton passengerAdd = (AppCompatButton)findViewById(R.id.buttonAddPassenger);
        passengerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PassengerAdd.class));
            }
        });

        //Setting up button to open Passenger List screen
        final AppCompatButton passengerList = (AppCompatButton)findViewById(R.id.buttonListPassengers);
        passengerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PassengerList.class));
            }
        });
    }
}
