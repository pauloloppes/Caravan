package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.PackageTrip;
import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.CustomAdapterPassenger;
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class TripAddPassenger extends AppCompatActivity {

    private Passenger p;
    private Trip t;
    private PackageTrip pck;
    private TextView labelAddPassengerName;
    private TextView labelAddTripName;
    private TextView labelAddPackName;
    private EditText editAddPassengerVehicle;
    private EditText editAddPassengerSeat;
    private EditText editAddPassengerBoarding;
    private EditText editAddPassengerLanding;
    private EditText editAddPassengerPaid;
    private CheckBox checkAddPassengerPaidFull;
    private AppCompatButton buttonAddPassengerSelect;
    private AppCompatButton buttonAddTripSelect;
    private AppCompatButton buttonAddPackSelect;
    private AppCompatButton buttonConfirmPassengerTrip;
    private ProgressBar loadAddPassengerTrip;
    private final int SELECT_PASSENGER_REQUEST = 1;
    private final int SELECT_TRIP_REQUEST = 2;
    private final int SELECT_PACK_REQUEST = 3;
    private Intent returnIntent;
    private DBLink dbLink;
    private boolean canReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_add_passenger);

        labelAddPassengerName = (TextView) findViewById(R.id.labelAddPassengerName);
        labelAddTripName = (TextView) findViewById(R.id.labelAddTripName);
        labelAddPackName = (TextView) findViewById(R.id.labelAddPackName);
        editAddPassengerVehicle = (EditText) findViewById(R.id.editAddPassengerVehicle);
        editAddPassengerSeat = (EditText) findViewById(R.id.editAddPassengerSeat);
        editAddPassengerBoarding = (EditText) findViewById(R.id.editAddPassengerBoarding);
        editAddPassengerLanding = (EditText) findViewById(R.id.editAddPassengerLanding);
        editAddPassengerPaid = (EditText) findViewById(R.id.editAddPassengerPaid);
        checkAddPassengerPaidFull = (CheckBox) findViewById(R.id.checkAddPassengerPaidFull);
        buttonAddPassengerSelect = (AppCompatButton) findViewById(R.id.buttonAddPassengerSelect);
        buttonAddTripSelect = (AppCompatButton) findViewById(R.id.buttonAddTripSelect);
        buttonAddPackSelect = (AppCompatButton) findViewById(R.id.buttonAddPackSelect);
        buttonConfirmPassengerTrip = (AppCompatButton) findViewById(R.id.buttonConfirmPassengerTrip);
        loadAddPassengerTrip = (ProgressBar) findViewById(R.id.loadAddPassengerTrip);

        dbLink = new DBLink();
        canReturn =  true;

        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            t = (Trip) b.getParcelable("trip");
        }

        updateInfo();

        buttonAddPassengerSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectPassenger();
            }
        });

        buttonAddTripSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectTrip();
            }
        });

        buttonAddPackSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t != null)
                    openSelectPack();
                else
                    toastShow("Escolha uma viagem antes de escolher pacote");
            }
        });

        buttonConfirmPassengerTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSaveButton();
                confirm();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void confirm() {
        if (p == null) {
            changeSaveButton();
            toastShow("Você deve selecionar um passageiro");
        } else if (t == null) {
            changeSaveButton();
            toastShow("Você deve selecionar uma viagem");
        } else {

            OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot query = task.getResult();
                        if (query.size() == 0) {
                            recordDB();
                        } else {
                            changeSaveButton();
                            toastShow("Passageiro já presente nesta viagem. Tente outros");
                        }
                    } else {
                        changeSaveButton();
                        toastShow("Erro ao conectar com o banco de dados");
                    }
                }
            };

            dbLink.getPasviagem(p.getId(),t.getId(),listenerComplete);

        }
    }

    private void recordDB() {
        Map dados = new HashMap<>();
        dados.put("passageiro",p.getId());
        dados.put("viagem",t.getId());
        p.setPasviagemVeiculo(editAddPassengerVehicle.getText().toString().trim());
        dados.put("veiculo",p.getPasviagemVeiculo());
        p.setPasviagemAssento(editAddPassengerSeat.getText().toString().trim());
        dados.put("assento",p.getPasviagemAssento());
        p.setPasviagemEmbarque(editAddPassengerBoarding.getText().toString().trim());
        dados.put("embarque",p.getPasviagemEmbarque());
        p.setPasviagemDesembarque(editAddPassengerLanding.getText().toString().trim());
        dados.put("desembarque",p.getPasviagemDesembarque());
        p.setPasviagemValorPago(editAddPassengerPaid.getText().toString().trim());
        dados.put("valorpago",p.getPasviagemValorPago());
        p.setPasviagemQuitado(String.valueOf(checkAddPassengerPaidFull.isChecked()));
        dados.put("quitado",String.valueOf(checkAddPassengerPaidFull.isChecked()));
        if (pck != null) {
            dados.put("pacote",pck.getId());
        }

        OnCompleteListener listenerComplete = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    setResultInfo();
                } else {
                    changeSaveButton();
                    toastShow("Erro ao gravar dados: "+task.getException().getMessage());
                }
            }
        };

        dbLink.addPassengerToTrip(dados, listenerComplete);

    }

    private void setResultInfo() {
        returnIntent.putExtra("passenger",p);
        returnIntent.putExtra("trip",t);
        setResult(Activity.RESULT_OK, returnIntent);
        toastShow("Dados gravados com sucesso");
        finish();
    }

    private void openSelectPassenger() {
        Intent i = new Intent(getApplicationContext(), PassengerAddSelect.class);
        startActivityForResult(i, SELECT_PASSENGER_REQUEST);
    }

    private void openSelectTrip() {
        Intent i = new Intent(getApplicationContext(), TripAddSelect.class);
        startActivityForResult(i, SELECT_TRIP_REQUEST);
    }

    private void openSelectPack() {
        Intent i = new Intent(getApplicationContext(), PackageAddSelect.class);
        i.putExtra("trip",t);
        startActivityForResult(i, SELECT_PACK_REQUEST);
    }

    private void updateInfo() {
        if (p != null)
            labelAddPassengerName.setText("Passageiro: "+p.getNome());
        else
            labelAddPassengerName.setText("Passageiro não definido");
        if (t != null)
            labelAddTripName.setText("Viagem: "+t.getNome());
        else
            labelAddTripName.setText("Viagem não definida");
        if (pck != null)
            labelAddPackName.setText("Pacote de viagem: "+pck.getNome());
        else
            labelAddPackName.setText("Pacote de viagem não definido");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_TRIP_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Trip tri = (Trip) data.getParcelableExtra("trip");
                    if (tri != null) {
                        t = tri;
                        updateInfo();
                    }
                }
            }
        } else if (requestCode == SELECT_PASSENGER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Passenger pas = (Passenger) data.getParcelableExtra("passenger");
                    if (pas != null) {
                        p = pas;
                        updateInfo();
                    }
                }
            }
        } else if (requestCode == SELECT_PACK_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    PackageTrip packa = (PackageTrip) data.getParcelableExtra("package");
                    if (packa != null) {
                        pck = packa;
                        updateInfo();
                    }
                }
            }
        }
    }

    private void changeSaveButton() {
        if (buttonConfirmPassengerTrip.isEnabled()) {
            buttonConfirmPassengerTrip.setEnabled(false);
            buttonConfirmPassengerTrip.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadAddPassengerTrip.setVisibility(View.VISIBLE);
        } else {
            buttonConfirmPassengerTrip.setEnabled(true);
            buttonConfirmPassengerTrip.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadAddPassengerTrip.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
