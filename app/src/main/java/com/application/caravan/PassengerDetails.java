package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.application.entities.Passenger;

public class PassengerDetails extends AppCompatActivity {

    private Passenger p;
    TextView labelPassengerDetailsName;
    TextView labelPassengerDetailsIdentity;
    TextView labelPassengerDetailsIdType;
    TextView labelPassengerDetailsBirthdate;
    TextView labelPassengerDetailsPhone;
    TextView labelPassengerDetailsAddress;

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

        Bundle b = getIntent().getExtras();
        if (b != null) {
            p = (Passenger) b.getParcelable("passenger");
            labelPassengerDetailsName.setText(p.getNome());
            labelPassengerDetailsIdentity.setText("Identidade: "+p.getIdentidade());
            labelPassengerDetailsIdType.setText("Tipo: "+p.getTipoIdentidade());
            labelPassengerDetailsBirthdate.setText("Data de nascimento: "+p.getDataNascimento());
            labelPassengerDetailsPhone.setText("Telefone: "+p.getTelefone());
            labelPassengerDetailsAddress.setText("Endereço: "+p.getEndereco());
        } else {
            labelPassengerDetailsName.setText("Erro ao carregar passageiro");
        }
    }
}
