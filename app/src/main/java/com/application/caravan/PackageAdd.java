package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.PackageTrip;
import com.application.entities.Trip;
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PackageAdd extends AppCompatActivity {

    private EditText editPackageName;
    private EditText editPackagePrice;
    private EditText editPackageDescription;
    private TextView labelPackageTrip;
    private AppCompatButton buttonPackageAddNew;
    private ProgressBar loadAddPackage;
    private Trip t;
    private PackageTrip p;
    private Intent returnIntent;
    private DBLink dbLink;
    private OnCompleteListener listener;
    private boolean canReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_add);

        returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);

        dbLink = new DBLink();
        canReturn =  true;

        setScreenElements();
        setTripObject();
        setListeners();
    }

    @Override
    public void onBackPressed() {
        if (canReturn)
            super.onBackPressed();
    }

    private void setScreenElements() {
        editPackageName = (EditText) findViewById(R.id.editPackageName);
        editPackagePrice = (EditText) findViewById(R.id.editPackagePrice);
        editPackageDescription = (EditText) findViewById(R.id.editPackageDescription);
        labelPackageTrip = (TextView) findViewById(R.id.labelPackageTrip);
        buttonPackageAddNew = (AppCompatButton) findViewById(R.id.buttonPackageAddNew);
        loadAddPackage = (ProgressBar) findViewById(R.id.loadAddPackage);
    }

    private void setTripObject() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            t = (Trip) b.getParcelable("trip");
        }

        if (t != null) {
            labelPackageTrip.setText("Viagem: "+t.getNome());
        } else {
            toastShow("Erro ao carregar informações da viagem");
            finish();
        }
    }

    private void setListeners() {
        listener = new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    toastShow("Pacote adicionado com sucesso.");
                    DocumentReference doc = (DocumentReference) task.getResult();
                    p.setId(doc.getId());
                    setReturn();
                } else {
                    toastShow("Erro: "+task.getException().getMessage());
                }
                changeSaveButton();
            }
        };

        buttonPackageAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPackage();
            }
        });
    }

    private void addPackage() {
        String name = editPackageName.getText().toString().trim();

        if (name.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Nome está vazio")
                    .setMessage("Campo 'Nome' é obrigatório.")
                    .setNegativeButton("Ok",null)
                    .show();
        } else {
            String price = editPackagePrice.getText().toString().trim();
            String description = editPackageDescription.getText().toString().trim();
            p = new PackageTrip("",name,price,description,t.getId());
            changeSaveButton();
            dbLink.addPackage(name,price,description,t.getId(),listener);
        }
    }

    private void setReturn() {
        returnIntent.putExtra("pack",p);
        setResult(RESULT_OK, returnIntent);
        toastShow("Pacote adicionado com sucesso");
        finish();
    }

    private void changeSaveButton() {
        if (buttonPackageAddNew.isEnabled()) {
            buttonPackageAddNew.setEnabled(false);
            buttonPackageAddNew.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadAddPackage.setVisibility(View.VISIBLE);
        } else {
            buttonPackageAddNew.setEnabled(true);
            buttonPackageAddNew.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            canReturn = true;
            loadAddPackage.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
