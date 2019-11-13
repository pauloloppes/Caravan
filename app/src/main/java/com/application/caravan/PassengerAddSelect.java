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

import com.application.entities.Passenger;
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

public class PassengerAddSelect extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;
    private ArrayList<Passenger> listAll;
    private ArrayList<Passenger> listSearched;
    private ListView listPassengersSelect;
    private EditText editPassengerNameSearchSelect;
    private AppCompatButton buttonSearchPassengerSelect;
    private CustomAdapterPassenger adapter;
    private CustomAdapterPassenger aSearched;
    private boolean searched;
    private Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_add_select);

        listPassengersSelect = (ListView) findViewById(R.id.listPassengersSelect);
        editPassengerNameSearchSelect = (EditText) findViewById(R.id.editPassengerNameSearchSelect);
        buttonSearchPassengerSelect = (AppCompatButton) findViewById(R.id.buttonSearchPassengerSelect);

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
                    .collection("passageiros")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Passenger p = document.toObject(Passenger.class);
                                    p.setId(document.getId());
                                    listAll.add(p);
                                }

                                sortLists();

                                adapter = new CustomAdapterPassenger(listAll, getApplicationContext());
                                listPassengersSelect.setAdapter(adapter);
                            } else
                                toastShow("Erro ao acessar documentos: "+task.getException());
                        }
                    });
        } else {
            toastShow("Erro ao carregar usuário");
        }

        listPassengersSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (searched) {
                    selectPassenger(listSearched.get(i));
                } else {
                    selectPassenger(listAll.get(i));
                }
            }
        });

        buttonSearchPassengerSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPassenger();
            }
        });

    }

    private void selectPassenger(Passenger p) {
        returnIntent.putExtra("passenger",p);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void searchPassenger() {
        String name = editPassengerNameSearchSelect.getText().toString().trim().toLowerCase();
        if (name.isEmpty()) {
            listPassengersSelect.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (Passenger p : listAll) {
                if (p.getNome().toLowerCase().contains(name)) {
                    listSearched.add(p);
                }
            }
            aSearched = new CustomAdapterPassenger(listSearched, getApplicationContext());
            listPassengersSelect.setAdapter(aSearched);
            searched = true;
        }
    }

    private void sortLists() {
        if (listAll != null && listAll.size() > 1) {
            Collections.sort(listAll, new Comparator<Passenger>() {
                @Override
                public int compare(Passenger p1, Passenger p2) {
                    return p1.getNome().compareTo(p2.getNome());
                }
            });
        }

        if (searched && listSearched != null && listSearched.size() > 1) {
            Collections.sort(listSearched, new Comparator<Passenger>() {
                @Override
                public int compare(Passenger p1, Passenger p2) {
                    return p1.getNome().compareTo(p2.getNome());
                }
            });
        }

    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
