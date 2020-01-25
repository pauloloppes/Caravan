package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
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

public class PackageEdit extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText editPackageNameEdit;
    private EditText editPackagePriceEdit;
    private EditText editPackageDescriptionEdit;
    private TextView labelPackageTripEdit;
    private AppCompatButton buttonPackageEditSave;
    private Trip t;
    private PackageTrip p;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_edit);

        databasePassengers = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);

        if (currentUser==null) {
            toastShow("Erro ao carregar usuário. Não é possível gravar dados.");
            finish();
        }

        editPackageNameEdit = (EditText) findViewById(R.id.editPackageNameEdit);
        editPackagePriceEdit = (EditText) findViewById(R.id.editPackagePriceEdit);
        editPackageDescriptionEdit = (EditText) findViewById(R.id.editPackageDescriptionEdit);
        labelPackageTripEdit = (TextView) findViewById(R.id.labelPackageTripEdit);
        buttonPackageEditSave = (AppCompatButton) findViewById(R.id.buttonPackageEditSave);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            t = (Trip) b.getParcelable("trip");
            p = (PackageTrip) b.getParcelable("pack");
            editPackageNameEdit.setText(p.getNome());
            editPackagePriceEdit.setText(p.getPreco());
            editPackageDescriptionEdit.setText(p.getDescricao());
        } else {
            toastShow("Erro ao carregar informações do pacote");
            finish();
        }

        if (t != null) {
            labelPackageTripEdit.setText("Viagem: "+t.getNome());
        } else {
            toastShow("Erro ao carregar informações da viagem");
            finish();
        }

        buttonPackageEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePackage();
            }
        });
    }

    private void savePackage() {
        Map updatedPack = new HashMap<>();
        String name = editPackageNameEdit.getText().toString().trim();
        String price = editPackagePriceEdit.getText().toString().trim();
        String description = editPackageDescriptionEdit.getText().toString().trim();

        if (!name.equals(p.getNome())) {
            updatedPack.put("nome",name);
            p.setNome(name);
        }
        if (!price.equals(p.getPreco())) {
            updatedPack.put("preco",price);
            p.setPreco(price);
        }
        if (!description.equals(p.getDescricao())) {
            updatedPack.put("descricao",description);
            p.setDescricao(description);
        }

        returnIntent.putExtra("pack",p);

        databasePassengers.collection(currentUser.getUid())
                .document("dados")
                .collection("pacotes").document(p.getId()).update(updatedPack);

        setResult(Activity.RESULT_OK, returnIntent);
        returnIntent.putExtra("edited", true);
        toastShow("Pacote alterado com sucesso");

        finish();
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
