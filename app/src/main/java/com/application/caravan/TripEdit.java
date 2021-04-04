package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.application.entities.Trip;
import com.application.utils.DBLink;
import com.application.utils.MaskEditUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private Trip t;
    private ProgressBar loadEditTrip;
    private OnCompleteListener listener;
    private DBLink dbLink;
    private boolean canReturn;

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
        loadEditTrip = (ProgressBar) findViewById(R.id.loadEditTrip);

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

        dbLink = new DBLink();
        canReturn =  true;

        listener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("trip",t);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    toastShow("Erro: "+task.getException().getMessage());
                }
                changeSaveButton();
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
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

        changeSaveButton();
        dbLink.updateTrip(updatedDocument,t.getId(),listener);

    }

    private void changeSaveButton() {
        if (buttonTripSave.isEnabled()) {
            buttonTripSave.setEnabled(false);
            buttonTripSave.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadEditTrip.setVisibility(View.VISIBLE);
        } else {
            buttonTripSave.setEnabled(true);
            buttonTripSave.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadEditTrip.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
