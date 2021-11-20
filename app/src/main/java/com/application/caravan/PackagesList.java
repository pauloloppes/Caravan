package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
import com.application.utils.DBLink;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PackagesList extends AppCompatActivity {

    private ArrayList<PackageTrip> listAll;
    private ArrayList<PackageTrip> listSearched;
    private ListView listPackages;
    private EditText editPackageNameSearch;
    private AppCompatButton buttonSearchPackage;
    private AppCompatButton buttonPackageAdd;
    private CustomAdapterPackage adapter;
    private CustomAdapterPackage aSearched;
    private final int EDIT_PACKAGE_REQUEST = 1;
    private final int ADD_PACKAGE_REQUEST = 2;
    private Trip t;
    private boolean searched;
    private DBLink dbLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages_list);

        listPackages = (ListView) findViewById(R.id.listPackages);
        editPackageNameSearch = (EditText) findViewById(R.id.editPackageNameSearch);
        buttonSearchPackage = (AppCompatButton) findViewById(R.id.buttonSearchPackage);
        buttonPackageAdd = (AppCompatButton) findViewById(R.id.buttonPackageAdd);

        listAll = new ArrayList<>();
        searched = false;

        dbLink = new DBLink();

        t = (Trip) getIntent().getParcelableExtra("trip");
        if (t == null) {
            toastShow("Falha ao carregar dados da viagem");
            finish();
        }

        searchPackagesOnDB();

        listPackages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PackageTrip p;
                if (searched) {
                    p = listSearched.get(i);
                } else {
                    p = listAll.get(i);
                }
                Intent details = new Intent(getApplicationContext(), PackageDetails.class);
                Bundle b = new Bundle();
                b.putParcelable("pack", p);
                b.putParcelable("trip",t);
                details.putExtras(b);
                startActivityForResult(details,EDIT_PACKAGE_REQUEST);
            }
        });

        buttonSearchPackage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPackage();
            }
        });

        buttonPackageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPackage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PACKAGE_REQUEST) {
            if (data != null) {
                PackageTrip p = (PackageTrip) data.getParcelableExtra("pack");
                boolean deleted = data.getBooleanExtra("deleted",false);
                boolean edited = data.getBooleanExtra("edited",false);
                if (p!=null) {
                    if (deleted)
                        removePackageOnLists(p);
                    else if (edited)
                        replacePackageOnLists(p);
                }
            }
        } else if (requestCode == ADD_PACKAGE_REQUEST) {
            if (data != null && resultCode == RESULT_OK) {
                PackageTrip packResponse = (PackageTrip) data.getParcelableExtra("pack");
                if (packResponse!= null) {
                    addPackageOnLists(packResponse);
                }
            }
        }
    }

    private void addPackageOnLists(PackageTrip p) {
        if (p != null) {
            listAll.add(p);

            if (searched) {
                String name = editPackageNameSearch.getText().toString().trim().toLowerCase();
                if (!name.isEmpty()) {
                    if (p.getNome().toLowerCase().contains(name)) {
                        listSearched.add(p);
                    }
                }
            }
            sortLists();
            listPackages.invalidateViews();
        }
    }

    private void replacePackageOnLists(PackageTrip p) {
        String newId = p.getId();

        if (listAll != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listAll.size(); i++) {
                PackageTrip pas = listAll.get(i);
                if (pas.getId().equals(newId)) {
                    listAll.remove(i);
                    listAll.add(p);
                    notFound = false;
                }
            }
        }

        if (searched && listSearched != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listSearched.size(); i++) {
                PackageTrip pas = listSearched.get(i);
                if (pas.getId().equals(newId)) {
                    listSearched.remove(pas);
                    listSearched.add(p);
                    notFound = false;
                }
            }
        }

        sortLists();
        listPackages.invalidateViews();
    }

    private void removePackageOnLists(PackageTrip p) {
        String newId = p.getId();

        if (listAll != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listAll.size(); i++) {
                PackageTrip pas = listAll.get(i);
                if (pas.getId().equals(newId)) {
                    listAll.remove(i);
                    notFound = false;
                }
            }
        }

        if (searched && listSearched != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listSearched.size(); i++) {
                PackageTrip pas = listSearched.get(i);
                if (pas.getId().equals(newId)) {
                    listSearched.remove(pas);
                    notFound = false;
                }
            }
        }

        sortLists();
        listPackages.invalidateViews();
    }

    private void searchPackagesOnDB() {

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
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
                    listPackages.setAdapter(adapter);
                    listPackages.invalidateViews();
                } else
                    toastShow("Erro ao acessar documentos: "+task.getException());

            }
        };

        dbLink.getAllPackages(t.getId(), listenerComplete);

    }

    private void searchPackage() {
        String name = editPackageNameSearch.getText().toString().trim().toLowerCase();
        if (name.isEmpty()) {
            listPackages.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (PackageTrip p : listAll) {
                if (p.getNome().toLowerCase().contains(name)) {
                    listSearched.add(p);
                }
            }
            aSearched = new CustomAdapterPackage(listSearched, getApplicationContext());
            listPackages.setAdapter(aSearched);
            searched = true;
        }
    }

    private void sortLists() {
        if (listAll != null && listAll.size() > 1) {
            Collections.sort(listAll, new Comparator<PackageTrip>() {
                @Override
                public int compare(PackageTrip p1, PackageTrip p2) {
                    return p1.getNome().compareToIgnoreCase(p2.getNome());
                }
            });
        }

        if (searched && listSearched != null && listSearched.size() > 1) {
            Collections.sort(listSearched, new Comparator<PackageTrip>() {
                @Override
                public int compare(PackageTrip p1, PackageTrip p2) {
                    return p1.getNome().compareToIgnoreCase(p2.getNome());
                }
            });
        }

    }

    private void addPackage() {
        Intent add = new Intent(getApplicationContext(), PackageAdd.class);
        Bundle a = new Bundle();
        a.putParcelable("trip",t);
        add.putExtras(a);
        startActivityForResult(add,ADD_PACKAGE_REQUEST);
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
