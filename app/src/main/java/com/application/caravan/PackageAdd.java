package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.entities.PackageTrip;
import com.application.entities.Trip;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PackageAdd extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText editPackageName;
    private EditText editPackagePrice;
    private EditText editPackageDescription;
    private TextView labelPackageTrip;
    private AppCompatButton buttonPackageAddNew;
    private Trip t;
    private PackageTrip p;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_add);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);

        if (currentUser==null) {
            toastShow("Erro ao carregar usuário. Não é possível gravar dados.");
            finish();
        }

        editPackageName = (EditText) findViewById(R.id.editPackageName);
        editPackagePrice = (EditText) findViewById(R.id.editPackagePrice);
        editPackageDescription = (EditText) findViewById(R.id.editPackageDescription);
        labelPackageTrip = (TextView) findViewById(R.id.labelPackageTrip);
        buttonPackageAddNew = (AppCompatButton) findViewById(R.id.buttonPackageAddNew);

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

        buttonPackageAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPackage();
            }
        });
    }

    private void addPackage() {
        Map pack = new HashMap<>();
        String name = editPackageName.getText().toString().trim();
        String price = editPackagePrice.getText().toString().trim();
        String description = editPackageDescription.getText().toString().trim();

        pack.put("nome", name);
        pack.put("preco", price);
        pack.put("descricao", description);
        pack.put("viagemID", t.getId());

        p = new PackageTrip("1",name,price,description,t.getId());

        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("pacotes")
                .add(pack)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            setReturn();
                        } else {
                            toastShow("Erro: "+task.getException().getMessage());
                        }
                    }
                });
    }

    private void setReturn() {
        returnIntent.putExtra("pack",p);
        toastShow("Pacote adicionado com sucesso");
        finish();
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
