package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PassengerDetails extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Passenger p;
    private int listPosition;
    private TextView labelPassengerDetailsName;
    private TextView labelPassengerDetailsIdentity;
    private TextView labelPassengerDetailsIdType;
    private TextView labelPassengerDetailsBirthdate;
    private TextView labelPassengerDetailsPhone;
    private TextView labelPassengerDetailsAddress;
    private AppCompatButton buttonEditPassenger;
    private AppCompatButton buttonDeletePassenger;
    Intent returnIntent;
    private final int EDIT_PASSENGER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_details);
        labelPassengerDetailsName = (TextView) findViewById(R.id.labelPassengerDetailsName);
        labelPassengerDetailsIdentity = (TextView) findViewById(R.id.labelPassengerDetailsIdentity);
        labelPassengerDetailsIdType = (TextView) findViewById(R.id.labelPassengerDetailsIdType);
        labelPassengerDetailsBirthdate = (TextView) findViewById(R.id.labelPassengerDetailsBirthdate);
        labelPassengerDetailsPhone = (TextView) findViewById(R.id.labelPassengerDetailsPhone);
        labelPassengerDetailsAddress = (TextView) findViewById(R.id.labelPassengerDetailsAddress);
        buttonEditPassenger = (AppCompatButton) findViewById(R.id.buttonEditPassenger);
        buttonDeletePassenger = (AppCompatButton) findViewById(R.id.buttonDeletePassenger);
        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            listPosition = b.getInt("listPosition");
            labelPassengerDetailsName.setText(p.getNome());
            labelPassengerDetailsIdentity.setText("Identidade: "+p.getIdentidade());
            labelPassengerDetailsIdType.setText("Tipo: "+p.getTipoIdentidade());
            labelPassengerDetailsBirthdate.setText("Data de nascimento: "+p.getDataNascimento());
            labelPassengerDetailsPhone.setText("Telefone: "+p.getTelefone());
            labelPassengerDetailsAddress.setText("Endereço: "+p.getEndereco());
        } else {
            labelPassengerDetailsName.setText("Erro ao carregar passageiro");
        }

        returnIntent.putExtra("passenger",p);

        buttonEditPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent edit = new Intent(getApplicationContext(), PassengerEdit.class);
                Bundle e = new Bundle();
                e.putParcelable("passenger",p);
                edit.putExtras(e);
                startActivityForResult(edit,EDIT_PASSENGER_REQUEST);
            }
        });

        buttonDeletePassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePassengerConfirmation();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PASSENGER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getParcelableExtra("passenger")!=null) {
                    p = data.getParcelableExtra("passenger");
                    returnIntent.putExtra("passenger",p);
                    returnIntent.putExtra("edited",true);
                    updateInfo();
                }
            }
        }
    }

    private void updateInfo () {
        if (p != null) {
            labelPassengerDetailsName.setText(p.getNome());
            labelPassengerDetailsIdentity.setText("Identidade: "+p.getIdentidade());
            labelPassengerDetailsIdType.setText("Tipo: "+p.getTipoIdentidade());
            labelPassengerDetailsBirthdate.setText("Data de nascimento: "+p.getDataNascimento());
            labelPassengerDetailsPhone.setText("Telefone: "+p.getTelefone());
            labelPassengerDetailsAddress.setText("Endereço: "+p.getEndereco());
        }
    }

    private void deletePassengerConfirmation() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Excluir passageiro")
                .setMessage("Tem certeza que deseja excluir este passageiro?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePassenger();
                    }
                })
                .setNegativeButton("Não", null)
                .show();

    }

    private void deletePassenger() {
        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("passageiros").document(p.getId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toastShow("Passageiro excluído com sucesso");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toastShow("Erro ao excluir: "+e.getMessage());
            }
        });
        returnIntent.putExtra("deleted", true);
        finish();
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
