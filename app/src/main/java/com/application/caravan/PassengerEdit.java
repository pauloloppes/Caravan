package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.application.entities.Passenger;
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

public class PassengerEdit extends AppCompatActivity {

    private EditText editPassengerNameEdit;
    private EditText editPassengerIdentityEdit;
    private Spinner spinnerPassengerIdTypeEdit;
    private EditText editPassengerBirthEdit;
    private EditText editPassengerPhoneEdit;
    private EditText editPassengerAddressEdit;
    private AppCompatButton buttonPassengerSave;
    private ProgressBar loadEditPassenger;
    private Passenger p;
    private OnCompleteListener listener;
    private DBLink dbLink;
    private boolean canReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_edit);

        //Setting up screen elements
        editPassengerNameEdit = (EditText) findViewById(R.id.editPassengerNameEdit);
        editPassengerBirthEdit = (EditText) findViewById(R.id.editPassengerBirthEdit);
        editPassengerIdentityEdit = (EditText) findViewById(R.id.editPassengerIdentityEdit);
        spinnerPassengerIdTypeEdit = (Spinner) findViewById(R.id.spinnerPassengerIdTypeEdit);
        editPassengerPhoneEdit = (EditText) findViewById(R.id.editPassengerPhoneEdit);
        editPassengerAddressEdit = (EditText) findViewById(R.id.editPassengerAddressEdit);
        buttonPassengerSave = (AppCompatButton) findViewById(R.id.buttonPassengerSave);
        loadEditPassenger = (ProgressBar) findViewById(R.id.loadEditPassenger);

        //Setting up Identity type ID spinner
        String arrayId[] = {"RG","CPF","Certidão de Nascimento","RNE","Outro"};
        ArrayAdapter<String> spinnerAdapterId = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,arrayId);
        spinnerAdapterId.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPassengerIdTypeEdit.setAdapter(spinnerAdapterId);

        //Setting up Passenger Identity field, adding listener to apply mask
        editPassengerBirthEdit.addTextChangedListener(MaskEditUtil.insert(MaskEditUtil.FORMAT_DATE, editPassengerBirthEdit));

        //Setting up button listener
        buttonPassengerSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePassenger();
            }
        });

        dbLink = new DBLink();
        canReturn =  true;

        //Setting up existing information on fields
        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            editPassengerNameEdit.setText(p.getNome());
            editPassengerIdentityEdit.setText(p.getIdentidade());
            spinnerPassengerIdTypeEdit.setSelection(spinnerAdapterId.getPosition(p.getTipoIdentidade()));
            editPassengerBirthEdit.setText(p.getDataNascimento().replace("/",""));
            editPassengerPhoneEdit.setText(p.getTelefone());
            editPassengerAddressEdit.setText(p.getEndereco());
        } else {
            toastShow("Erro ao carregar passageiro");
            finish();
        }

        listener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("passenger",p);
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

    private void savePassenger() {

        String name = editPassengerNameEdit.getText().toString().trim();
        String birthdate = editPassengerBirthEdit.getText().toString().trim();
        String identity = editPassengerIdentityEdit.getText().toString().trim();
        String idType = spinnerPassengerIdTypeEdit.getSelectedItem().toString();
        String phone = editPassengerPhoneEdit.getText().toString().trim();
        String address = editPassengerAddressEdit.getText().toString().trim();

        Map updatedDocument = new HashMap();

        if (!name.equals(p.getNome())) {
            updatedDocument.put("nome",name);
            p.setNome(name);
        }
        if (!birthdate.equals(p.getDataNascimento())) {
            updatedDocument.put("dataNascimento",birthdate);
            p.setDataNascimento(birthdate);
        }
        if (!identity.equals(p.getIdentidade())) {
            updatedDocument.put("identidade",identity);
            p.setIdentidade(identity);
        }
        if (!idType.equals(p.getTipoIdentidade())) {
            updatedDocument.put("tipoIdentidade",idType);
            p.setTipoIdentidade(idType);
        }
        if (!phone.equals(p.getTelefone())) {
            updatedDocument.put("telefone",phone);
            p.setTelefone(phone);
        }
        if (!address.equals(p.getEndereco())) {
            updatedDocument.put("endereco",address);
            p.setEndereco(address);
        }

        changeSaveButton();
        dbLink.updatePassenger(updatedDocument,p.getId(),listener);

    }

    private void changeSaveButton() {
        if (buttonPassengerSave.isEnabled()) {
            buttonPassengerSave.setEnabled(false);
            buttonPassengerSave.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadEditPassenger.setVisibility(View.VISIBLE);
        } else {
            buttonPassengerSave.setEnabled(true);
            buttonPassengerSave.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadEditPassenger.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
