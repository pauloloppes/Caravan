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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class TripEditPassengerPack extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Passenger p;
    private Trip t;
    private PackageTrip pt;
    private TextView labelEditPassengerNamePack;
    private TextView labelEditTripNamePack;
    private TextView labelEditPackNamePack;
    private AppCompatButton buttonEditPackSelectPack;
    private AppCompatButton buttonConfirmPassengerTripPack;
    private AppCompatButton buttonRemovePassengerTripPack;
    private Intent returnIntent;
    private final int SELECT_PACK_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit_passenger_pack);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        labelEditPassengerNamePack = (TextView) findViewById(R.id.labelEditPassengerNamePack);
        labelEditTripNamePack = (TextView) findViewById(R.id.labelEditTripNamePack);
        labelEditPackNamePack = (TextView) findViewById(R.id.labelEditPackNamePack);
        buttonEditPackSelectPack = (AppCompatButton) findViewById(R.id.buttonEditPackSelectPack);
        buttonRemovePassengerTripPack = (AppCompatButton) findViewById(R.id.buttonRemovePassengerTripPack);
        buttonConfirmPassengerTripPack = (AppCompatButton) findViewById(R.id.buttonConfirmPassengerTripPack);

        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            t = (Trip) b.getParcelable("trip");
            pt = (PackageTrip) b.getParcelable("pack");
        }

        updateInfo();

        buttonEditPackSelectPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t != null)
                    openSelectPack();
                else
                    toastShow("Escolha uma viagem antes de escolher pacote");
            }
        });

        buttonConfirmPassengerTripPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        buttonRemovePassengerTripPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePack();
            }
        });

    }

    private void updateInfo() {
        if (p != null)
            labelEditPassengerNamePack.setText("Passageiro: "+p.getNome());
        else
            labelEditPassengerNamePack.setText("Passageiro não definido");
        if (t != null)
            labelEditTripNamePack.setText("Viagem: "+t.getNome());
        else
            labelEditTripNamePack.setText("Viagem não definida");
        if (pt != null)
            labelEditPackNamePack.setText("Pacote: "+pt.getNome());
        else
            labelEditPackNamePack.setText("Pacote não definido");
    }

    private void removePack() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Retirar pacote")
                .setMessage("Tem certeza que deseja retirar este pacote do passageiro?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pt = null;
                        updateInfo();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void confirm() {
        if (p == null) {
            toastShow("Você deve selecionar um passageiro");
        } else if (t == null) {
            toastShow("Você deve selecionar uma viagem");
        } else {
            recordDB();
        }
    }

    private void recordDB() {
        Map dados = new HashMap<>();

        if (pt != null) {
            dados.put("pacote",pt.getId());
        } else {
            dados.put("pacote", FieldValue.delete());
            returnIntent.putExtra("packDelete",true);
        }

        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("pasviagem")
                .document(p.getPasviagemId())
                .update(dados);
        returnIntent.putExtra("pack",pt);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PACK_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    PackageTrip packa = (PackageTrip) data.getParcelableExtra("package");
                    if (packa != null) {
                        pt = packa;
                        updateInfo();
                    }
                }
            }
        }
    }

    private void openSelectPack() {
        Intent i = new Intent(getApplicationContext(), PackageAddSelect.class);
        i.putExtra("trip",t);
        startActivityForResult(i, SELECT_PACK_REQUEST);
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
