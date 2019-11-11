package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.application.utils.MaskEditUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TripAdd extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText editTripName;
    private EditText editTripDestination;
    private EditText editTripDepartureDate;
    private EditText editTripDepartureHour;
    private EditText editTripReturnDate;
    private EditText editTripReturnHour;
    private EditText editTripSeatQuantity;
    private AppCompatButton buttonTripAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_add);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            toastShow("Erro ao carregar usuário. Não é possível gravar dados.");
            finish();
        }

        editTripName = (EditText) findViewById(R.id.editTripName);
        editTripDestination = (EditText) findViewById(R.id.editTripDestination);
        editTripDepartureDate = (EditText) findViewById(R.id.editTripDepartureDate);
        editTripDepartureHour = (EditText) findViewById(R.id.editTripDepartureHour);
        editTripReturnDate = (EditText) findViewById(R.id.editTripReturnDate);
        editTripReturnHour = (EditText) findViewById(R.id.editTripReturnHour);
        editTripSeatQuantity = (EditText) findViewById(R.id.editTripSeatQuantity);
        buttonTripAdd = (AppCompatButton) findViewById(R.id.buttonTripAdd);

        //adding listener to apply mask
        editTripDepartureDate.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_DATE, editTripDepartureDate));
        editTripDepartureHour.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_HOUR, editTripDepartureHour));
        editTripReturnDate.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_DATE, editTripReturnDate));
        editTripReturnHour.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_HOUR, editTripReturnHour));

        buttonTripAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrip();
            }
        });
    }

    private void addTrip() {
        Map trip = new HashMap<>();
        trip.put("nome", editTripName.getText().toString().trim());
        trip.put("destino", editTripDestination.getText().toString().trim());
        trip.put("partida_data", editTripDepartureDate.getText().toString().trim());
        trip.put("partida_hora", editTripDepartureHour.getText().toString().trim());
        trip.put("retorno_data", editTripReturnDate.getText().toString().trim());
        trip.put("retorno_hora", editTripReturnHour.getText().toString().trim());
        trip.put("limite", editTripSeatQuantity.getText().toString().trim());

        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("viagens")
                .add(trip)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            toastShow("Viagem adicionada com sucesso");
                            finish();
                        } else {
                            toastShow("Erro: "+task.getException().getMessage());
                        }
                    }
                });
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

