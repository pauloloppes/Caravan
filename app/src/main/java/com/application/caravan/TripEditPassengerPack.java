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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.PackageTrip;
import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.DBLink;
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

    private Passenger p;
    private Trip t;
    private PackageTrip pt;
    private TextView labelEditPassengerNamePack;
    private TextView labelEditTripNamePack;
    private TextView labelEditPackNamePack;
    private EditText editEditPassengerVehicle;
    private EditText editEditPassengerSeat;
    private EditText editEditPassengerBoarding;
    private EditText editEditPassengerLanding;
    private EditText editEditPassengerPaid;
    private AppCompatButton buttonEditPackSelectPack;
    private AppCompatButton buttonConfirmPassengerTripPack;
    private AppCompatButton buttonRemovePassengerTripPack;
    private ProgressBar loadEditPassengerTrip;
    private Intent returnIntent;
    private DBLink dbLink;
    private boolean canReturn;
    private final int SELECT_PACK_REQUEST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_edit_passenger_pack);

        labelEditPassengerNamePack = (TextView) findViewById(R.id.labelEditPassengerNamePack);
        labelEditTripNamePack = (TextView) findViewById(R.id.labelEditTripNamePack);
        labelEditPackNamePack = (TextView) findViewById(R.id.labelEditPackNamePack);
        editEditPassengerVehicle = (EditText) findViewById(R.id.editEditPassengerVehicle);
        editEditPassengerSeat = (EditText) findViewById(R.id.editEditPassengerSeat);
        editEditPassengerBoarding = (EditText) findViewById(R.id.editEditPassengerBoarding);
        editEditPassengerLanding = (EditText) findViewById(R.id.editEditPassengerLanding);
        editEditPassengerPaid = (EditText) findViewById(R.id.editEditPassengerPaid);
        buttonEditPackSelectPack = (AppCompatButton) findViewById(R.id.buttonEditPackSelectPack);
        buttonRemovePassengerTripPack = (AppCompatButton) findViewById(R.id.buttonRemovePassengerTripPack);
        buttonConfirmPassengerTripPack = (AppCompatButton) findViewById(R.id.buttonConfirmPassengerTripPack);
        loadEditPassengerTrip = (ProgressBar) findViewById(R.id.loadEditPassengerTrip);

        dbLink = new DBLink();
        canReturn =  true;

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

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void updateInfo() {
        if (p != null) {
            labelEditPassengerNamePack.setText("Passageiro: " + p.getNome());
            editEditPassengerVehicle.setText(p.getPasviagemVeiculo());
            editEditPassengerSeat.setText(p.getPasviagemAssento());
            editEditPassengerBoarding.setText(p.getPasviagemEmbarque());
            editEditPassengerLanding.setText(p.getPasviagemDesembarque());
            editEditPassengerPaid.setText(p.getPasviagemValorPago());
        }
        else {
            labelEditPassengerNamePack.setText("Passageiro não definido");
            editEditPassengerVehicle.setText("Veículo: ");
            editEditPassengerSeat.setText("Assento: ");
            editEditPassengerBoarding.setText("Embarque: ");
            editEditPassengerLanding.setText("Desembarque: ");
            editEditPassengerPaid.setText("Valor pago: ");
        }
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

        p.setPasviagemVeiculo(editEditPassengerVehicle.getText().toString().trim());
        dados.put("veiculo",p.getPasviagemVeiculo());
        p.setPasviagemAssento(editEditPassengerSeat.getText().toString().trim());
        dados.put("assento",p.getPasviagemAssento());
        p.setPasviagemEmbarque(editEditPassengerBoarding.getText().toString().trim());
        dados.put("embarque",p.getPasviagemEmbarque());
        p.setPasviagemDesembarque(editEditPassengerLanding.getText().toString().trim());
        dados.put("desembarque",p.getPasviagemDesembarque());
        p.setPasviagemValorPago(editEditPassengerPaid.getText().toString().trim());
        dados.put("valorpago",p.getPasviagemValorPago());

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    returnIntent.putExtra("pack",pt);
                    returnIntent.putExtra("passenger",p);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    toastShow("Erro ao atualizar: "+task.getException());
                    changeSaveButton();
                }
            }
        };

        changeSaveButton();
        dbLink.updatePassengerFromTrip(dados,p.getPasviagemId(),listenerComplete);

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

    private void changeSaveButton() {
        if (buttonConfirmPassengerTripPack.isEnabled()) {
            buttonConfirmPassengerTripPack.setEnabled(false);
            buttonConfirmPassengerTripPack.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadEditPassengerTrip.setVisibility(View.VISIBLE);
        } else {
            buttonConfirmPassengerTripPack.setEnabled(true);
            buttonConfirmPassengerTripPack.setBackgroundTintList(this.getResources().getColorStateList(R.color.redAchtung));
            canReturn = true;
            loadEditPassengerTrip.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
