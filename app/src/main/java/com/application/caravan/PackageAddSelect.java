package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.application.entities.PackageTrip;
import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.CustomAdapterPackage;
import com.application.utils.CustomAdapterPassenger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PackageAddSelect extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;
    private ArrayList<PackageTrip> listAll;
    private ArrayList<PackageTrip> listSearched;
    private ListView listPackagesSelect;
    private EditText editPackageNameSearchSelect;
    private AppCompatButton buttonSearchPackageSelect;
    private CustomAdapterPackage adapter;
    private CustomAdapterPackage aSearched;
    private boolean searched;
    private Intent returnIntent;
    private Trip t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_add_select);

        listPackagesSelect = (ListView) findViewById(R.id.listPackagesSelect);
        editPackageNameSearchSelect = (EditText) findViewById(R.id.editPackageNameSearchSelect);
        buttonSearchPackageSelect = (AppCompatButton) findViewById(R.id.buttonSearchPackageSelect);

        t = (Trip) getIntent().getParcelableExtra("trip");
        if (t == null) {
            toastShow("Falha ao carregar dados da viagem");
            finish();
        }

        listAll = new ArrayList<>();
        searched = false;

        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser!=null) {
            databasePassengers.collection(currentUser.getUid())
                    .document("dados")
                    .collection("pacotes")
                    .whereEqualTo("viagemID",t.getId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    PackageTrip p = document.toObject(PackageTrip.class);
                                    p.setId(document.getId());
                                    listAll.add(p);
                                }

                                sortLists();

                                adapter = new CustomAdapterPackage(listAll, getApplicationContext());
                                listPackagesSelect.setAdapter(adapter);
                                listPackagesSelect.invalidateViews();
                            } else
                                toastShow("Erro ao acessar documentos: "+task.getException());
                        }
                    });
        } else {
            toastShow("Erro ao carregar usuário");
        }

        listPackagesSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (searched) {
                    selectPackage(listSearched.get(i));
                } else {
                    selectPackage(listAll.get(i));
                }
            }
        });

        buttonSearchPackageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPackage();
            }
        });
    }

    private void selectPackage(PackageTrip p) {
        returnIntent.putExtra("package",p);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void searchPackage() {
        String name = editPackageNameSearchSelect.getText().toString().trim().toLowerCase();
        if (name.isEmpty()) {
            listPackagesSelect.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (PackageTrip p : listAll) {
                if (p.getNome().toLowerCase().contains(name)) {
                    listSearched.add(p);
                }
            }
            aSearched = new CustomAdapterPackage(listSearched, getApplicationContext());
            listPackagesSelect.setAdapter(aSearched);
            searched = true;
        }
    }

    private void sortLists() {
        if (listAll != null && listAll.size() > 1) {
            Collections.sort(listAll, new Comparator<PackageTrip>() {
                @Override
                public int compare(PackageTrip p1, PackageTrip p2) {
                    return p1.getNome().compareTo(p2.getNome());
                }
            });
        }

        if (searched && listSearched != null && listSearched.size() > 1) {
            Collections.sort(listSearched, new Comparator<PackageTrip>() {
                @Override
                public int compare(PackageTrip p1, PackageTrip p2) {
                    return p1.getNome().compareTo(p2.getNome());
                }
            });
        }

    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
