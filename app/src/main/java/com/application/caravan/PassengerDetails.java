package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.Passenger;

public class PassengerDetails extends AppCompatActivity {

    private Passenger p;
    private int listPosition;
    private TextView labelPassengerDetailsName;
    private TextView labelPassengerDetailsIdentity;
    private TextView labelPassengerDetailsIdType;
    private TextView labelPassengerDetailsBirthdate;
    private TextView labelPassengerDetailsPhone;
    private TextView labelPassengerDetailsAddress;
    private AppCompatButton buttonEditPassenger;
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
        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PASSENGER_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getParcelableExtra("passenger")!=null) {
                    p = data.getParcelableExtra("passenger");
                    returnIntent.putExtra("passenger",p);
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

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
