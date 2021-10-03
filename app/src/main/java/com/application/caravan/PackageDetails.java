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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PackageDetails extends AppCompatActivity {

    private PackageTrip p;
    private Trip t;
    private int listPosition;
    private TextView labelPackageDetailsName;
    private TextView labelPackageDetailsPrice;
    private TextView labelPackageDetailsDescription;
    private AppCompatButton buttonEditPackage;
    private AppCompatButton buttonDeletePackage;
    private ProgressBar loadDeletePackage;
    private Intent returnIntent;
    private final int EDIT_PACKAGE_REQUEST = 1;
    private DBLink dbLink;
    private boolean canReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_details);

        labelPackageDetailsName = (TextView) findViewById(R.id.labelPackageDetailsName);
        labelPackageDetailsPrice = (TextView) findViewById(R.id.labelPackageDetailsPrice);
        labelPackageDetailsDescription = (TextView) findViewById(R.id.labelPackageDetailsDescription);
        buttonEditPackage = (AppCompatButton) findViewById(R.id.buttonEditPackage);
        buttonDeletePackage = (AppCompatButton) findViewById(R.id.buttonDeletePackage);
        loadDeletePackage = (ProgressBar) findViewById(R.id.loadDeletePackage);

        returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);

        dbLink = new DBLink();
        canReturn =  true;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            t = (Trip) b.getParcelable("trip");
            p = (PackageTrip) b.getParcelable("pack");
            listPosition = b.getInt("listPosition");
            labelPackageDetailsName.setText(p.getNome());
            labelPackageDetailsPrice.setText("Preço: "+p.getPreco());
            labelPackageDetailsDescription.setText("Tipo: "+p.getDescricao());
        } else {
            labelPackageDetailsName.setText("Erro ao carregar pacote");
        }

        returnIntent.putExtra("pack",p);

        buttonEditPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent edit = new Intent(getApplicationContext(), PackageEdit.class);
                Bundle e = new Bundle();
                e.putParcelable("pack",p);
                e.putParcelable("trip",t);
                edit.putExtras(e);
                startActivityForResult(edit,EDIT_PACKAGE_REQUEST);
            }
        });

        buttonDeletePackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePackageConfirmation();
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
        if (requestCode == EDIT_PACKAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getParcelableExtra("pack")!=null) {
                    p = data.getParcelableExtra("pack");
                    returnIntent.putExtra("pack",p);
                    returnIntent.putExtra("edited",true);
                    updateInfo();
                }
            }
        }
    }

    private void updateInfo () {
        if (p != null) {
            labelPackageDetailsName.setText(p.getNome());
            labelPackageDetailsPrice.setText("Identidade: "+p.getPreco());
            labelPackageDetailsDescription.setText("Tipo: "+p.getDescricao());
        }
    }

    private void deletePackageConfirmation() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Excluir pacote")
                .setMessage("Tem certeza que deseja excluir este pacote?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePackage();
                    }
                })
                .setNegativeButton("Não", null)
                .show();

    }

    private void deletePackage() {

        OnSuccessListener listenerSuccess = new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                toastShow("Pacote excluído com sucesso");
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
        dbLink.deletePackage(p.getId(), listenerSuccess, listenerFailure);

    }

    private void changeDeleteButton() {
        if (buttonDeletePackage.isEnabled()) {
            buttonDeletePackage.setEnabled(false);
            buttonDeletePackage.setBackgroundTintList(this.getResources().getColorStateList(R.color.greyDisabled));
            canReturn = false;
            loadDeletePackage.setVisibility(View.VISIBLE);
        } else {
            buttonDeletePackage.setEnabled(true);
            buttonDeletePackage.setBackgroundTintList(this.getResources().getColorStateList(R.color.redAchtung));
            canReturn = true;
            loadDeletePackage.setVisibility(View.INVISIBLE);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
