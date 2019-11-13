package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TripEditPassenger extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Passenger p;
    private Trip t;
    private TextView labelEditPassengerName;
    private TextView labelEditTripName;
    private AppCompatButton buttonEditPassengerSelect;
    private AppCompatButton buttonEditTripSelect;
    private AppCompatButton buttonConfirmPassengerTripEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit_passenger);

        labelEditPassengerName = (TextView) findViewById(R.id.labelEditPassengerName);
        labelEditTripName = (TextView) findViewById(R.id.labelEditTripName);
        buttonEditPassengerSelect = (AppCompatButton) findViewById(R.id.buttonEditPassengerSelect);
        buttonEditTripSelect = (AppCompatButton) findViewById(R.id.buttonEditTripSelect);
        buttonConfirmPassengerTripEdit = (AppCompatButton) findViewById(R.id.buttonConfirmPassengerTripEdit);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            t = (Trip) b.getParcelable("trip");
        }

        updateInfo();

        buttonConfirmPassengerTripEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    private void confirm() {
        finish();
    }

    private void updateInfo() {
        if (p != null)
            labelEditPassengerName.setText("Passageiro: "+p.getNome());
        else
            labelEditPassengerName.setText("Passageiro não definido");
        if (t != null)
            labelEditTripName.setText("Viagem: "+t.getNome());
        else
            labelEditTripName.setText("Viagem não definida");
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
