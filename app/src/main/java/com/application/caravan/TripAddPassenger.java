package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TripAddPassenger extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Passenger p;
    private Trip t;
    private TextView labelAddPassengerName;
    private TextView labelAddTripName;
    private AppCompatButton buttonAddPassengerSelect;
    private AppCompatButton buttonAddTripSelect;
    private AppCompatButton buttonConfirmPassengerTrip;
    private final int SELECT_PASSENGER_REQUEST = 1;
    private final int SELECT_TRIP_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_add_passenger);

        labelAddPassengerName = (TextView) findViewById(R.id.labelAddPassengerName);
        labelAddTripName = (TextView) findViewById(R.id.labelAddTripName);
        buttonAddPassengerSelect = (AppCompatButton) findViewById(R.id.buttonAddPassengerSelect);
        buttonAddTripSelect = (AppCompatButton) findViewById(R.id.buttonAddTripSelect);
        buttonConfirmPassengerTrip = (AppCompatButton) findViewById(R.id.buttonConfirmPassengerTrip);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            t = (Trip) b.getParcelable("trip");
        }

        updateInfo();

        buttonAddPassengerSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectPassenger();
            }
        });

        buttonAddTripSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectTrip();
            }
        });

        buttonConfirmPassengerTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    private void confirm() {
        if (p == null) {
            toastShow("Você deve selecionar um passageiro");
        } else if (t == null) {
            toastShow("Você deve selecionar uma viagem");
        } else {
            Map dados = new HashMap<>();
            dados.put("passageiro",p.getId());
            dados.put("viagem",t.getId());
            databasePassengers.collection(currentUser.getUid())
                    .document("dados")
                    .collection("pasviagem")
                    .add(dados)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                toastShow("Dados gravados com sucesso");
                                finish();
                            } else {
                                toastShow("Erro ao gravar dados: "+task.getException().getMessage());
                            }
                        }
                    });
        }
    }

    private void openSelectPassenger() {
        Intent i = new Intent(getApplicationContext(), PassengerAddSelect.class);
        startActivityForResult(i, SELECT_PASSENGER_REQUEST);
    }

    private void openSelectTrip() {
        Intent i = new Intent(getApplicationContext(), TripAddSelect.class);
        startActivityForResult(i, SELECT_TRIP_REQUEST);
    }

    private void updateInfo() {
        if (p != null)
            labelAddPassengerName.setText("Passageiro: "+p.getNome());
        else
            labelAddPassengerName.setText("Passageiro não definido");
        if (t != null)
            labelAddTripName.setText("Viagem: "+t.getNome());
        else
            labelAddTripName.setText("Viagem não definida");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_TRIP_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Trip tri = (Trip) data.getParcelableExtra("trip");
                    if (tri != null) {
                        t = tri;
                        updateInfo();
                    }
                }
            }
        } else if (requestCode == SELECT_PASSENGER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Passenger pas = (Passenger) data.getParcelableExtra("passenger");
                    if (pas != null) {
                        p = pas;
                        updateInfo();
                    }
                }
            }
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
