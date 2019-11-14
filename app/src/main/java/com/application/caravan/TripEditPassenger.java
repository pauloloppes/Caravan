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
import com.application.entities.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TripEditPassenger extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Passenger p;
    private Trip t;
    private TextView labelEditPassengerName;
    private TextView labelEditTripName;
    private AppCompatButton buttonDeletePassengerTrip;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit_passenger);

        labelEditPassengerName = (TextView) findViewById(R.id.labelEditPassengerName);
        labelEditTripName = (TextView) findViewById(R.id.labelEditTripName);
        buttonDeletePassengerTrip = (AppCompatButton) findViewById(R.id.buttonDeletePassengerTrip);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            t = (Trip) b.getParcelable("trip");
        }

        updateInfo();

        buttonDeletePassengerTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePassengerConfirmation();
            }
        });
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

    private void deletePassengerConfirmation() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Remover passageiro")
                .setMessage("Tem certeza que deseja remover este passageiro da viagem?")
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
                .collection("pasviagem")
                .whereEqualTo("passageiro",p.getId())
                .whereEqualTo("viagem",t.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                deletePassenger(document.getId());
                            }
                        } else {
                            toastShow("Erro ao remover passageiro: "+task.getException().getMessage());
                        }

                    }
                });
    }

    private void deletePassenger(String id) {
        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("pasviagem").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        toastShow("Passageiro removido da viagem com sucesso");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        toastShow("Erro ao remover: "+e.getMessage());
                    }
                });
        returnIntent.putExtra("passenger",p);
        returnIntent.putExtra("deleted", true);
        finish();
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
