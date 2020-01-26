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

import com.application.entities.PackageTrip;
import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.CustomAdapterPassenger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class TripEditPassenger extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Passenger p;
    private Trip t;
    private PackageTrip pt;
    private TextView labelEditPassengerName;
    private TextView labelEditTripName;
    private TextView labelEditPackageName;
    private AppCompatButton buttonChangePackageTrip;
    private AppCompatButton buttonDeletePassengerTrip;
    private Intent returnIntent;
    private final int EDIT_PACKAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit_passenger);

        labelEditPassengerName = (TextView) findViewById(R.id.labelEditPassengerName);
        labelEditTripName = (TextView) findViewById(R.id.labelEditTripName);
        labelEditPackageName = (TextView) findViewById(R.id.labelEditPackageName);
        buttonChangePackageTrip = (AppCompatButton) findViewById(R.id.buttonChangePackageTrip);
        buttonDeletePassengerTrip = (AppCompatButton) findViewById(R.id.buttonDeletePassengerTrip);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            System.out.println("\t\t"+p.getPasviagemId());
            t = (Trip) b.getParcelable("trip");
            searchPackage();
        }

        updateInfo();

        buttonDeletePassengerTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePassengerConfirmation();
            }
        });

        buttonChangePackageTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent edit = new Intent(getApplicationContext(), TripEditPassengerPack.class);
                Bundle e = new Bundle();
                e.putParcelable("passenger",p);
                e.putParcelable("trip",t);
                e.putParcelable("pack",pt);
                edit.putExtras(e);
                startActivityForResult(edit,EDIT_PACKAGE_REQUEST);
            }
        });
    }

    private void searchPackage() {
        if (currentUser!=null) {
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
                                    Object pkt = document.get("pacote");
                                    if (pkt != null)
                                        searchPackageById(pkt.toString());
                                }

                            } else
                                toastShow("Erro ao acessar documentos: "+task.getException());
                        }
                    });

        } else {
            toastShow("Erro ao carregar usuário");
        }
    }

    private void searchPackageById(String packID) {
        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("pacotes")
                .document(packID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            pt = doc.toObject(PackageTrip.class);
                            if (pt != null) {
                                pt.setId(doc.getId());
                                updateInfo();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PACKAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getParcelableExtra("pack")!=null) {
                    pt = data.getParcelableExtra("pack");
                } else {
                    if (data.getBooleanExtra("packDelete",false)) {
                        pt = null;
                    }
                }
                updateInfo();
            }
        }
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
        if (pt != null)
            labelEditPackageName.setText("Pacote: "+pt.getNome());
        else
            labelEditPackageName.setText("Pacote não definido");
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
