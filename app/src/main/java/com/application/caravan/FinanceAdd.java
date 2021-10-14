package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.Trip;
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FinanceAdd extends AppCompatActivity {

    private Trip t;
    private int listPosition;
    private int valorTotal;
    private int valorArrecadado;
    private TextView labelTripFinanceName;
    private TextView labelTripFinanceTotalValue;
    private TextView labelTripFinancePaidValue;
    private TextView labelTripFinancePaidPercentage;
    private TextView labelTripFinancePaidRemaining;
    private ProgressBar barTripFinancePaidPercentage;
    private AppCompatButton buttonTripFinanceRefresh;
    private ProgressBar loadTripFinanceRefresh;
    private Intent returnIntent;
    private DBLink dbLink;
    private boolean canReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_add);

        setScreenElements();

        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        dbLink = new DBLink();
        canReturn =  true;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            t = (Trip) b.getParcelable("trip");
            listPosition = b.getInt("listPosition");
            labelTripFinanceName.setText(t.getNome());
            labelTripFinanceTotalValue.setText("Valor total: R$ "+t.getValor());
            labelTripFinancePaidValue.setText("Valor arrecadado: R$ "+t.getValor_arrecadado());
            if (t.getValor().isEmpty())
                valorTotal = 0;
            else
                valorTotal = Integer.parseInt(t.getValor());

            if (t.getValor_arrecadado().isEmpty())
                valorArrecadado = 0;
            else
                valorArrecadado = Integer.parseInt(t.getValor_arrecadado());
            setBarPercentage(0,0);
        } else {
            labelTripFinanceName.setText("Erro ao carregar viagem");
        }

        returnIntent.putExtra("trip",t);

        buttonTripFinanceRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateValue();
                changeSaveButton();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void setScreenElements() {
        labelTripFinanceName = (TextView) findViewById(R.id.labelTripFinanceName);
        labelTripFinanceTotalValue = (TextView) findViewById(R.id.labelTripFinanceTotalValue);
        labelTripFinancePaidValue = (TextView) findViewById(R.id.labelTripFinancePaidValue);
        labelTripFinancePaidPercentage = (TextView) findViewById(R.id.labelTripFinancePaidPercentage);
        labelTripFinancePaidRemaining = (TextView) findViewById(R.id.labelTripFinancePaidRemaining);
        barTripFinancePaidPercentage = (ProgressBar) findViewById(R.id.barTripFinancePaidPercentage);
        buttonTripFinanceRefresh = (AppCompatButton) findViewById(R.id.buttonTripFinanceRefresh);
        loadTripFinanceRefresh = (ProgressBar) findViewById(R.id.loadTripFinanceRefresh);
    }

    private void setBarPercentage(int pago, int restam) {
        int percentage = 0;
        if (valorTotal > 0) {
            percentage = (int) Math.ceil(((double) valorArrecadado / (double) valorTotal) * 100);
            System.out.println("\t\t\tVALOR TOTAL "+valorTotal);
            System.out.println("\t\t\tVALOR ARRECADADO "+valorArrecadado);
            System.out.println("\t\t\tarre / total "+String.valueOf(valorArrecadado/valorTotal));
            System.out.println("\t\t\tarre / total *100 "+String.valueOf((valorArrecadado/valorTotal)*100));
            System.out.println("\t\t\tPERCENTUAL EH "+percentage);
            barTripFinancePaidPercentage.setProgress(percentage);
        }
        labelTripFinancePaidPercentage.setText(percentage+"% arrecadado.");
        if (pago == 0 && restam == 0)
            labelTripFinancePaidRemaining.setText("");
        else
            labelTripFinancePaidRemaining.setText(pago+" pago(s), resta(m) "+restam+" passageiros.");
    }

    private void updateValue() {
        returnIntent.putExtra("updated",true);
        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int total = 0;
                    int pago = 0;
                    int restam = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (!document.get("valorpago").toString().isEmpty())
                            total += Integer.parseInt(document.get("valorpago").toString());
                        if (Boolean.valueOf(document.get("quitado").toString()))
                            pago += 1;
                        else
                            restam += 1;

                    }
                    t.setValor_arrecadado(String.valueOf(total));
                    valorArrecadado = total;
                    labelTripFinancePaidValue.setText("Valor arrecadado: R$ "+valorArrecadado);
                    returnIntent.putExtra("trip",t);
                    setBarPercentage(pago,restam);
                    updateValueOnDB();
                } else {
                    toastShow("Erro ao ler dados: "+task.getException().getMessage());
                }
                changeSaveButton();
            }
        };
        dbLink.getAllPassengersFromTrip(t.getId(),listenerComplete);
    }

    private void updateValueOnDB() {
        Map updatedDocument = new HashMap();
        updatedDocument.put("valor_arrecadado",t.getValor_arrecadado());
        OnCompleteListener listenerComplete = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    toastShow("nice! fez update");
                } else {
                    toastShow("uh oh, deu pau");
                }
            }
        };
        dbLink.updateTrip(updatedDocument,t.getId(),listenerComplete);
    }

    private void changeSaveButton() {
        if (buttonTripFinanceRefresh.isEnabled()) {
            buttonTripFinanceRefresh.setEnabled(false);
            buttonTripFinanceRefresh.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadTripFinanceRefresh.setVisibility(View.VISIBLE);
        } else {
            buttonTripFinanceRefresh.setEnabled(true);
            buttonTripFinanceRefresh.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadTripFinanceRefresh.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
