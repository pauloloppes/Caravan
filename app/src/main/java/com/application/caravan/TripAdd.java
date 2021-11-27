                                                                                                                                                                                                                            package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.application.utils.DBLink;
import com.application.utils.MaskEditUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TripAdd extends AppCompatActivity {

    private EditText editTripName;
    private EditText editTripDestination;
    private EditText editTripDepartureDate;
    private EditText editTripDepartureHour;
    private EditText editTripReturnDate;
    private EditText editTripReturnHour;
    private EditText editTripSeatQuantity;
    private EditText editTripTotalValue;
    private ProgressBar loadAddTrip;
    private AppCompatButton buttonTripAdd;
    private OnCompleteListener listener;
    private boolean canReturn;
    private DBLink dbLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_add);

        dbLink = new DBLink();
        canReturn =  true;

        listener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    toastShow("Viagem adicionada com sucesso");
                    finish();
                } else {
                    toastShow("Erro: "+task.getException().getMessage());
                }
                changeSaveButton();
            }
        };

        editTripName = (EditText) findViewById(R.id.editTripName);
        editTripDestination = (EditText) findViewById(R.id.editTripDestination);
        editTripDepartureDate = (EditText) findViewById(R.id.editTripDepartureDate);
        editTripDepartureHour = (EditText) findViewById(R.id.editTripDepartureHour);
        editTripReturnDate = (EditText) findViewById(R.id.editTripReturnDate);
        editTripReturnHour = (EditText) findViewById(R.id.editTripReturnHour);
        editTripSeatQuantity = (EditText) findViewById(R.id.editTripSeatQuantity);
        editTripTotalValue = (EditText) findViewById(R.id.editTripTotalValue);
        buttonTripAdd = (AppCompatButton) findViewById(R.id.buttonTripAdd);
        loadAddTrip = (ProgressBar) findViewById(R.id.loadAddTrip);

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

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void addTrip() {
        String name = editTripName.getText().toString().trim();

        if (name.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Nome está vazio")
                    .setMessage("Campo 'Nome' é obrigatório.")
                    .setNegativeButton("Ok",null)
                    .show();
        } else {
            String destination = editTripDestination.getText().toString().trim();
            String departureDate = editTripDepartureDate.getText().toString().trim();
            String departureHour = editTripDepartureHour.getText().toString().trim();
            String returnDate = editTripReturnDate.getText().toString().trim();
            String returnHour = editTripReturnHour.getText().toString().trim();
            String seatLimit = editTripSeatQuantity.getText().toString().trim();
            String totalValue = editTripTotalValue.getText().toString().trim();
            if (totalValue.isEmpty()) {
                totalValue = "0";
            }

            changeSaveButton();
            dbLink.addTrip(name,destination,departureDate,departureHour,returnDate,returnHour,seatLimit,totalValue,listener);
        }
    }

    private void changeSaveButton() {
        if (buttonTripAdd.isEnabled()) {
            buttonTripAdd.setEnabled(false);
            buttonTripAdd.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadAddTrip.setVisibility(View.VISIBLE);
        } else {
            buttonTripAdd.setEnabled(true);
            buttonTripAdd.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadAddTrip.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

