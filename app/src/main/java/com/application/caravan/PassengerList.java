package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.application.entities.Passenger;
import com.application.utils.CustomAdapterPassenger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PassengerList extends AppCompatActivity {

    private FirebaseFirestore databasePassengers;
    private ArrayList<Passenger> listAll;
    private ArrayList<Passenger> listSearched;
    private ListView listPassengers;
    private EditText editPassengerNameSearch;
    private AppCompatButton buttonSearchPassenger;
    private CustomAdapterPassenger adapter;
    private CustomAdapterPassenger aSearched;
    private final int OPEN_PASSENGER_REQUEST = 1;
    private boolean searched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_list);

        listPassengers = (ListView) findViewById(R.id.listPassengers);
        editPassengerNameSearch = (EditText) findViewById(R.id.editPassengerNameSearch);
        buttonSearchPassenger = (AppCompatButton) findViewById(R.id.buttonSearchPassenger);

        listAll = new ArrayList<>();
        searched = false;

        databasePassengers = FirebaseFirestore.getInstance();

        databasePassengers.collection("passageiros")
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
                            listPassengers.setAdapter(adapter);
                        } else
                            toastShow("Erro ao acessar documentos: "+task.getException());
                    }
                });

        listPassengers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Passenger p;
                if (searched) {
                    p = listSearched.get(i);
                } else {
                    p = listAll.get(i);
                }
                Intent details = new Intent(getApplicationContext(), PassengerDetails.class);
                Bundle b = new Bundle();
                b.putParcelable("passenger", p);
                details.putExtras(b);
                startActivityForResult(details,OPEN_PASSENGER_REQUEST);
            }
        });

        buttonSearchPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPassenger();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_PASSENGER_REQUEST) {
            if (data != null) {
                Passenger p = (Passenger) data.getParcelableExtra("passenger");
                boolean deleted = data.getBooleanExtra("deleted",false);
                boolean edited = data.getBooleanExtra("edited",false);
                if (p!=null) {
                    if (deleted)
                        removePassengerOnLists(p);
                    else if (edited)
                        replacePassengerOnLists(p);
                }
            }
        }
    }

    private void replacePassengerOnLists(Passenger p) {
        String newId = p.getId();

        if (listAll != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listAll.size(); i++) {
                Passenger pas = listAll.get(i);
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
                Passenger pas = listSearched.get(i);
                if (pas.getId().equals(newId)) {
                    listSearched.remove(pas);
                    listSearched.add(p);
                    notFound = false;
                }
            }
        }

        sortLists();
        listPassengers.invalidateViews();
    }

    private void removePassengerOnLists(Passenger p) {
        String newId = p.getId();

        if (listAll != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listAll.size(); i++) {
                Passenger pas = listAll.get(i);
                if (pas.getId().equals(newId)) {
                    listAll.remove(i);
                    notFound = false;
                }
            }
        }

        if (searched && listSearched != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listSearched.size(); i++) {
                Passenger pas = listSearched.get(i);
                if (pas.getId().equals(newId)) {
                    listSearched.remove(pas);
                    notFound = false;
                }
            }
        }

        sortLists();
        listPassengers.invalidateViews();
    }

    private void searchPassenger() {
        String name = editPassengerNameSearch.getText().toString().trim();
        if (name.isEmpty()) {
            listPassengers.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (Passenger p : listAll) {
                if (p.getNome().contains(name)) {
                    listSearched.add(p);
                }
            }
            aSearched = new CustomAdapterPassenger(listSearched, getApplicationContext());
            listPassengers.setAdapter(aSearched);
            searched = true;
        }
    }

    private void sortLists() {
        if (listAll != null) {
            Collections.sort(listAll, new Comparator<Passenger>() {
                @Override
                public int compare(Passenger p1, Passenger p2) {
                    return p1.getNome().compareTo(p2.getNome());
                }
            });
        }

        if (searched && listSearched != null) {
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
