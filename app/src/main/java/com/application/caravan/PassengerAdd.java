package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.application.utils.DBLink;
import com.application.utils.MaskEditUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PassengerAdd extends AppCompatActivity {

    private EditText editPassengerName;
    private EditText editPassengerIdentity;
    private Spinner spinnerPassengerIdType;
    private EditText editPassengerBirth;
    private EditText editPassengerPhone;
    private EditText editPassengerAddress;
    private AppCompatButton buttonPassengerAdd;
    private ProgressBar loadAddPassenger;
    private DBLink dbLink;
    private OnCompleteListener listener;
    private boolean canReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_add);

        dbLink = new DBLink();
        canReturn =  true;

        listener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    toastShow("Passageiro adicionado com sucesso.");
                    finish();
                } else {
                    toastShow("Erro: "+task.getException().getMessage());
                }
                changeSaveButton();
            }
        };

        //Setting up screen elements
        editPassengerName = (EditText) findViewById(R.id.editPassengerName);
        editPassengerBirth = (EditText) findViewById(R.id.editPassengerBirth);
        editPassengerIdentity = (EditText) findViewById(R.id.editPassengerIdentity);
        spinnerPassengerIdType = (Spinner) findViewById(R.id.spinnerPassengerIdType);
        editPassengerPhone = (EditText) findViewById(R.id.editPassengerPhone);
        editPassengerAddress = (EditText) findViewById(R.id.editPassengerAddress);
        buttonPassengerAdd = (AppCompatButton) findViewById(R.id.buttonPassengerAdd);
        loadAddPassenger = (ProgressBar) findViewById(R.id.loadAddPassenger);

        //Setting up Identity type ID spinner
        String arrayId[] = {"RG","CPF","Certidão de Nascimento","RNE","Outro"};
        ArrayAdapter<String> spinnerAdapterId = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,arrayId);
        spinnerAdapterId.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPassengerIdType.setAdapter(spinnerAdapterId);

        //Setting up Passenger Identity field, adding listener to apply mask
        editPassengerBirth.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_DATE, editPassengerBirth));

        //Setting up button listener
        buttonPassengerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPassenger();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void addPassenger() {

        String name = editPassengerName.getText().toString().trim();
        if (name.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Nome está vazio")
                    .setMessage("Campo 'Nome' é obrigatório.")
                    .setNegativeButton("Ok",null)
                    .show();
        } else {
            String birth = editPassengerBirth.getText().toString().trim();
            String identity = editPassengerIdentity.getText().toString().trim();
            String idType = spinnerPassengerIdType.getSelectedItem().toString();
            String phone = editPassengerPhone.getText().toString().trim();
            String address = editPassengerAddress.getText().toString().trim();

            changeSaveButton();
            dbLink.addPassenger(name,birth,identity,idType,phone,address,listener);
        }

    }

    private void changeSaveButton() {
        if (buttonPassengerAdd.isEnabled()) {
            buttonPassengerAdd.setEnabled(false);
            buttonPassengerAdd.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadAddPassenger.setVisibility(View.VISIBLE);
        } else {
            buttonPassengerAdd.setEnabled(true);
            buttonPassengerAdd.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadAddPassenger.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
