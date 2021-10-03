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

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TripDetails extends AppCompatActivity {

    private Trip t;
    private int listPosition;
    private TextView labelTripDetailsName;
    private TextView labelTripDetailsDestination;
    private TextView labelTripDetailsDepartureDate;
    private TextView labelTripDetailsDepartureTime;
    private TextView labelTripDetailsReturnDate;
    private TextView labelTripDetailsReturnTime;
    private TextView labelTripDetailsSeatQuantity;
    private AppCompatButton buttonTripPassengerList;
    private AppCompatButton buttonTripPassengerConfirmation;
    private AppCompatButton buttonTripPackages;
    private AppCompatButton buttonEditTrip;
    private AppCompatButton buttonDeleteTrip;
    private ProgressBar loadDeleteTrip;
    private Intent returnIntent;
    private final int EDIT_TRIP_REQUEST = 1;
    private DBLink dbLink;
    private boolean canReturn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        labelTripDetailsName = (TextView) findViewById(R.id.labelTripDetailsName);
        labelTripDetailsDestination = (TextView) findViewById(R.id.labelTripDetailsDestination);
        labelTripDetailsDepartureDate = (TextView) findViewById(R.id.labelTripDetailsDepartureDate);
        labelTripDetailsDepartureTime = (TextView) findViewById(R.id.labelTripDetailsDepartureTime);
        labelTripDetailsReturnDate = (TextView) findViewById(R.id.labelTripDetailsReturnDate);
        labelTripDetailsReturnTime = (TextView) findViewById(R.id.labelTripDetailsReturnTime);
        labelTripDetailsSeatQuantity = (TextView) findViewById(R.id.labelTripDetailsSeatQuantity);
        buttonTripPassengerList = (AppCompatButton) findViewById(R.id.buttonTripPassengerList);
        buttonTripPassengerConfirmation = (AppCompatButton) findViewById(R.id.buttonTripPassengerConfirmation);
        buttonTripPackages = (AppCompatButton) findViewById(R.id.buttonTripPackages);
        buttonEditTrip = (AppCompatButton) findViewById(R.id.buttonEditTrip);
        buttonDeleteTrip = (AppCompatButton) findViewById(R.id.buttonDeleteTrip);
        loadDeleteTrip = (ProgressBar) findViewById(R.id.loadDeleteTrip);
        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

        dbLink = new DBLink();
        canReturn =  true;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            t = (Trip) b.getParcelable("trip");
            listPosition = b.getInt("listPosition");
            labelTripDetailsName.setText(t.getNome());
            labelTripDetailsDestination.setText("Destino: "+t.getDestino());
            labelTripDetailsDepartureDate.setText("Data/Hora Partida: "+t.getPartida_data());
            labelTripDetailsDepartureTime.setText(t.getPartida_hora());
            labelTripDetailsReturnDate.setText("Data/Hora Retorno: "+t.getRetorno_data());
            labelTripDetailsReturnTime.setText(t.getRetorno_hora());
            labelTripDetailsSeatQuantity.setText("Lotação: "+t.getLimite());
        } else {
            labelTripDetailsName.setText("Erro ao carregar viagem");
        }

        returnIntent.putExtra("trip",t);

        buttonTripPassengerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pasList = new Intent(getApplicationContext(), TripPassengersList.class);
                pasList.putExtra("trip",t);
                startActivity(pasList);
            }
        });

        buttonTripPassengerConfirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pasList = new Intent(getApplicationContext(), TripPassengersConfirmation.class);
                pasList.putExtra("trip",t);
                startActivity(pasList);
            }
        });

        buttonTripPackages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent packages = new Intent(getApplicationContext(), PackagesList.class);
                packages.putExtra("trip",t);
                startActivity(packages);
            }
        });

        buttonEditTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent edit = new Intent(getApplicationContext(), TripEdit.class);
                Bundle e = new Bundle();
                e.putParcelable("trip",t);
                edit.putExtras(e);
                startActivityForResult(edit,EDIT_TRIP_REQUEST);
            }
        });

        buttonDeleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTripConfirmation();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_TRIP_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getParcelableExtra("trip")!=null) {
                    t = data.getParcelableExtra("trip");
                    returnIntent.putExtra("trip",t);
                    returnIntent.putExtra("edited",true);
                    updateInfo();
                }
            }
        }
    }

    private void updateInfo () {
        if (t != null) {
            labelTripDetailsName.setText(t.getNome());
            labelTripDetailsDestination.setText("Destino: "+t.getDestino());
            labelTripDetailsDepartureDate.setText("Partida: "+t.getPartida_data());
            labelTripDetailsDepartureTime.setText(t.getPartida_hora());
            labelTripDetailsReturnDate.setText("Retorno: "+t.getRetorno_data());
            labelTripDetailsReturnTime.setText(t.getRetorno_hora());
            labelTripDetailsSeatQuantity.setText("Lotação: "+t.getLimite());
        }
    }

    private void deleteTripConfirmation() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Excluir viagem")
                .setMessage("Tem certeza que deseja excluir esta viagem?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTrip();
                    }
                })
                .setNegativeButton("Não", null)
                .show();

    }

    private void deleteTrip() {
        OnSuccessListener listenerSuccess = new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                toastShow("Viagem excluída com sucesso");
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
        dbLink.deleteTrip(t.getId(), listenerSuccess, listenerFailure);

    }

    private void changeDeleteButton() {
        if (buttonDeleteTrip.isEnabled()) {
            buttonDeleteTrip.setEnabled(false);
            buttonDeleteTrip.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadDeleteTrip.setVisibility(View.VISIBLE);
        } else {
            buttonDeleteTrip.setEnabled(true);
            buttonDeleteTrip.setBackgroundTintList(this.getResources().getColorStateList(R.color.redAchtung));
            canReturn = true;
            loadDeleteTrip.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
