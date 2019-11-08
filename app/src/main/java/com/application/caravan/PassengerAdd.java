package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_add);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser==null) {
            toastShow("Erro ao carregar usuário. Não é possível gravar dados.");
            finish();
        }

        //Setting up screen elements
        editPassengerName = (EditText) findViewById(R.id.editPassengerName);
        editPassengerBirth = (EditText) findViewById(R.id.editPassengerBirth);
        editPassengerIdentity = (EditText) findViewById(R.id.editPassengerIdentity);
        spinnerPassengerIdType = (Spinner) findViewById(R.id.spinnerPassengerIdType);
        editPassengerPhone = (EditText) findViewById(R.id.editPassengerPhone);
        editPassengerAddress = (EditText) findViewById(R.id.editPassengerAddress);
        buttonPassengerAdd = (AppCompatButton) findViewById(R.id.buttonPassengerAdd);

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

    private void addPassenger() {

        Map passenger = new HashMap<>();
        passenger.put("nome", editPassengerName.getText().toString().trim());
        passenger.put("dataNascimento", editPassengerBirth.getText().toString().trim());
        passenger.put("identidade", editPassengerIdentity.getText().toString().trim());
        passenger.put("tipoIdentidade", spinnerPassengerIdType.getSelectedItem().toString());
        passenger.put("telefone", editPassengerPhone.getText().toString().trim());
        passenger.put("endereco", editPassengerAddress.getText().toString().trim());

        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("passageiros")
                .add(passenger)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            toastShow("Passageiro adicionado com sucesso");
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
