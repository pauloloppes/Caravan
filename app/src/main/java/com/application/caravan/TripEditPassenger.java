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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.PackageTrip;
import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.CustomAdapterPassenger;
import com.application.utils.DBLink;
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

    private Passenger p;
    private Trip t;
    private PackageTrip pt;
    private TextView labelEditPassengerName;
    private TextView labelEditTripName;
    private TextView labelEditPackageName;
    private TextView labelEditVehicle;
    private TextView labelEditSeat;
    private TextView labelEditBoarding;
    private TextView labelEditLanding;
    private TextView labelEditPaidAmount;
    private TextView labelEditPaidFull;
    private AppCompatButton buttonChangePackageTrip;
    private AppCompatButton buttonDeletePassengerTrip;
    private ProgressBar loadDeletePassengerTrip;
    private Intent returnIntent;
    private DBLink dbLink;
    private boolean canReturn;
    private final int EDIT_PACKAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit_passenger);

        labelEditPassengerName = (TextView) findViewById(R.id.labelEditPassengerName);
        labelEditTripName = (TextView) findViewById(R.id.labelEditTripName);
        labelEditPackageName = (TextView) findViewById(R.id.labelEditPackageName);
        labelEditVehicle = (TextView) findViewById(R.id.labelEditVehicle);
        labelEditSeat = (TextView) findViewById(R.id.labelEditSeat);
        labelEditBoarding = (TextView) findViewById(R.id.labelEditBoarding);
        labelEditLanding = (TextView) findViewById(R.id.labelEditLanding);
        labelEditPaidAmount = (TextView) findViewById(R.id.labelEditPaidAmount);
        labelEditPaidFull = (TextView) findViewById(R.id.labelEditPaidFull);
        buttonChangePackageTrip = (AppCompatButton) findViewById(R.id.buttonChangePackageTrip);
        buttonDeletePassengerTrip = (AppCompatButton) findViewById(R.id.buttonDeletePassengerTrip);
        loadDeletePassengerTrip = (ProgressBar) findViewById(R.id.loadDeletePassengerTrip);

        dbLink = new DBLink();
        canReturn =  true;


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

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void searchPackage() {
            OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
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
            };

            dbLink.getPackageFromCustomerTrip(p.getId(),t.getId(),listenerComplete);
    }

    private void searchPackageById(String packID) {

        OnCompleteListener listenerComplete = new OnCompleteListener<DocumentSnapshot>() {
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
        };

        dbLink.getPackageById(packID, listenerComplete);

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
                if (data.getParcelableExtra("passenger")!=null) {
                    p = data.getParcelableExtra("passenger");
                    returnIntent.putExtra("passenger",p);
                    returnIntent.putExtra("edited",true);
                }
                updateInfo();
            }
        }
    }

    private void updateInfo() {
        if (p != null) {
            labelEditPassengerName.setText("Passageiro: " + p.getNome());
            labelEditVehicle.setText("Veículo: "+p.getPasviagemVeiculo());
            labelEditSeat.setText("Assento: "+p.getPasviagemAssento());
            labelEditBoarding.setText("Embarque: "+p.getPasviagemEmbarque());
            labelEditLanding.setText("Desembarque: "+p.getPasviagemDesembarque());
            labelEditPaidAmount.setText("Valor pago: "+p.getPasviagemValorPago());
            if (Boolean.valueOf(p.getPasviagemQuitado())) {
                labelEditPaidFull.setText("Passageiro pagou totalmente!");
                labelEditPaidFull.setTextColor(this.getResources().getColorStateList(R.color.greenYes));
            } else {
                labelEditPaidFull.setText("Passageiro ainda não pagou totalmente!");
                labelEditPaidFull.setTextColor(this.getResources().getColorStateList(R.color.redAchtung));
            }

        }
        else {
            labelEditPassengerName.setText("Passageiro não definido");
            labelEditVehicle.setText("Veículo: ");
            labelEditSeat.setText("Assento: ");
            labelEditBoarding.setText("Embarque: ");
            labelEditLanding.setText("Desembarque: ");
            labelEditPaidAmount.setText("Valor pago: ");
            labelEditPaidFull.setText("Passageiro ainda não pagou totalmente!");
        }
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
        OnSuccessListener listenerSuccess = new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                toastShow("Passageiro excluído da viagem com sucesso");
                returnIntent.putExtra("passenger",p);
                returnIntent.putExtra("deleted", true);
                finish();
            }
        };

        OnFailureListener listenerFailure = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toastShow("Erro ao excluir: "+e.getMessage());
                changeDeleteButton();
            }
        };

        changeDeleteButton();
        dbLink.deletePassengerFromTrip(p.getId(), t.getId(), listenerSuccess, listenerFailure);

    }

    private void changeDeleteButton() {
        if (buttonDeletePassengerTrip.isEnabled()) {
            buttonDeletePassengerTrip.setEnabled(false);
            buttonDeletePassengerTrip.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadDeletePassengerTrip.setVisibility(View.VISIBLE);
        } else {
            buttonDeletePassengerTrip.setEnabled(true);
            buttonDeletePassengerTrip.setBackgroundTintList(this.getResources().getColorStateList(R.color.redAchtung));
            canReturn = true;
            loadDeletePassengerTrip.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
