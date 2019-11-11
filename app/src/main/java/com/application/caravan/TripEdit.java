package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.application.entities.Trip;
import com.application.utils.MaskEditUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TripEdit extends AppCompatActivity {

    private EditText editTripNameEdit;
    private EditText editTripDestinationEdit;
    private EditText editTripDepartureDateEdit;
    private EditText editTripReturnDateEdit;
    private EditText editTripDepartureHourEdit;
    private EditText editTripReturnHourEdit;
    private EditText editTripSeatQuantityEdit;
    private AppCompatButton buttonTripSave;
    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Trip t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit);

        editTripNameEdit = (EditText) findViewById(R.id.editTripNameEdit);
        editTripDestinationEdit = (EditText) findViewById(R.id.editTripDestinationEdit);
        editTripDepartureDateEdit = (EditText) findViewById(R.id.editTripDepartureDateEdit);
        editTripReturnDateEdit = (EditText) findViewById(R.id.editTripReturnDateEdit);
        editTripDepartureHourEdit = (EditText) findViewById(R.id.editTripDepartureHourEdit);
        editTripReturnHourEdit = (EditText) findViewById(R.id.editTripReturnHourEdit);
        editTripSeatQuantityEdit = (EditText) findViewById(R.id.editTripSeatQuantityEdit);
        buttonTripSave = (AppCompatButton) findViewById(R.id.buttonTripSave);

        editTripDepartureDateEdit.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_DATE, editTripDepartureDateEdit));
        editTripDepartureHourEdit.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_HOUR, editTripDepartureHourEdit));
        editTripReturnDateEdit.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_DATE, editTripReturnDateEdit));
        editTripReturnHourEdit.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_HOUR, editTripReturnHourEdit));

        buttonTripSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTrip();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            toastShow("Erro ao carregar usuário. Não é possível gravar dados.");
            finish();
        }

        //Setting up existing information on fields
        Bundle b = getIntent().getExtras();
        if (b != null) {
            t = (Trip) b.getParcelable("trip");
            editTripNameEdit.setText(t.getNome());
            editTripDestinationEdit.setText(t.getDestino());
            editTripDepartureDateEdit.setText(t.getPartida_data().replace("/",""));
            editTripReturnDateEdit.setText(t.getRetorno_data().replace("/",""));
            editTripDepartureHourEdit.setText(t.getPartida_hora().replace(":",""));
            editTripReturnHourEdit.setText(t.getRetorno_hora().replace(":",""));
            editTripSeatQuantityEdit.setText(t.getLimite());
        } else {
            toastShow("Erro ao carregar passageiro");
            finish();
        }

        databasePassengers = FirebaseFirestore.getInstance();
    }

    private void saveTrip() {

        String name = editTripNameEdit.getText().toString().trim();
        String destination = editTripDestinationEdit.getText().toString().trim();
        String departureDate = editTripDepartureDateEdit.getText().toString().trim();
        String returnDate = editTripReturnDateEdit.getText().toString().trim();
        String departureHour = editTripDepartureHourEdit.getText().toString().trim();
        String returnHour = editTripReturnHourEdit.getText().toString().trim();
        String seatQuantity = editTripSeatQuantityEdit.getText().toString().trim();

        Map updatedDocument = new HashMap();

        if (!name.equals(t.getNome())) {
            updatedDocument.put("nome",name);
            t.setNome(name);
        }
        if (!destination.equals(t.getDestino())) {
            updatedDocument.put("destino",destination);
            t.setDestino(destination);
        }
        if (!departureDate.equals(t.getPartida_data())) {
            updatedDocument.put("partida_data",departureDate);
            t.setPartida_data(departureDate);
        }
        if (!returnDate.equals(t.getRetorno_data())) {
            updatedDocument.put("retorno_data",returnDate);
            t.setRetorno_data(returnDate);
        }
        if (!departureHour.equals(t.getPartida_hora())) {
            updatedDocument.put("partida_hora",departureHour);
            t.setPartida_hora(departureHour);
        }
        if (!returnHour.equals(t.getRetorno_hora())) {
            updatedDocument.put("retorno_hora",returnHour);
            t.setRetorno_hora(returnHour);
        }
        if (!seatQuantity.equals(t.getLimite())) {
            updatedDocument.put("limite",seatQuantity);
            t.setLimite(seatQuantity);
        }

        Intent returnIntent = new Intent();
        returnIntent.putExtra("trip",t);



        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("viagens").document(t.getId()).update(updatedDocument);

        setResult(Activity.RESULT_OK, returnIntent);

        finish();

    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
