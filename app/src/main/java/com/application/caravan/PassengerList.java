package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_list);

        listPassengers = (ListView) findViewById(R.id.listPassengers);
        editPassengerNameSearch = (EditText) findViewById(R.id.editPassengerNameSearch);
        buttonSearchPassenger = (AppCompatButton) findViewById(R.id.buttonSearchPassenger);

        listAll = new ArrayList<>();

        databasePassengers = FirebaseFirestore.getInstance();
        databasePassengers.collection("passageiros").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    listAll.addAll(queryDocumentSnapshots.toObjects(Passenger.class));
                    adapter = new CustomAdapterPassenger(listAll, getApplicationContext());
                    listPassengers.setAdapter(adapter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toastShow("Erro ao acessar documentos: "+e.getMessage());
            }
        });

        buttonSearchPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPassenger();
            }
        });
    }

    private void searchPassenger() {
        String name = editPassengerNameSearch.getText().toString();
        if (name.isEmpty()) {
            listPassengers.setAdapter(adapter);
        } else {
            listSearched = new ArrayList<>();
            for (Passenger p : listAll) {
                if (p.getNome().contains(name)) {
                    listSearched.add(p);
                }
            }
            aSearched = new CustomAdapterPassenger(listSearched, getApplicationContext());
            listPassengers.setAdapter(aSearched);
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
