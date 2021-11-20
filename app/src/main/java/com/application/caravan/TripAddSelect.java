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

import com.application.entities.Trip;
import com.application.utils.CustomAdapterTrip;
import com.application.utils.DBLink;
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

public class TripAddSelect extends AppCompatActivity {

    private ListView listTripsSelect;
    private EditText editTripNameSearchSelect;
    private AppCompatButton buttonSearchTripSelect;
    private ArrayList<Trip> listAll;
    private ArrayList<Trip> listSearched;
    private CustomAdapterTrip adapter;
    private CustomAdapterTrip aSearched;
    private boolean searched;
    private Intent returnIntent;
    private DBLink dbLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_add_select);

        listTripsSelect = (ListView) findViewById(R.id.listTripsSelect);
        editTripNameSearchSelect = (EditText) findViewById(R.id.editTripNameSearchSelect);
        buttonSearchTripSelect = (AppCompatButton) findViewById(R.id.buttonSearchTripSelect);

        listAll = new ArrayList<>();
        searched = false;
        dbLink = new DBLink();

        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Trip t = document.toObject(Trip.class);
                        t.setId(document.getId());
                        listAll.add(t);
                    }

                    sortLists();

                    adapter = new CustomAdapterTrip(listAll, getApplicationContext());
                    listTripsSelect.setAdapter(adapter);
                } else
                    toastShow("Erro ao acessar documentos: "+task.getException());

            }
        };

        dbLink.getAllTrips(listenerComplete);

        listTripsSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (searched) {
                    selectTrip(listSearched.get(i));
                } else {
                    selectTrip(listAll.get(i));
                }
            }
        });

        buttonSearchTripSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTrip();
            }
        });
    }

    private void selectTrip(Trip t) {
        returnIntent.putExtra("trip",t);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void searchTrip() {
        String name = editTripNameSearchSelect.getText().toString().trim().toLowerCase();
        if (name.isEmpty()) {
            listTripsSelect.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (Trip t : listAll) {
                if (t.getNome().toLowerCase().contains(name)) {
                    listSearched.add(t);
                }
            }
            aSearched = new CustomAdapterTrip(listSearched, getApplicationContext());
            listTripsSelect.setAdapter(aSearched);
            searched = true;
        }
    }

    private void sortLists() {
        if (listAll != null && listAll.size() > 1) {
            Collections.sort(listAll, new Comparator<Trip>() {
                @Override
                public int compare(Trip t1, Trip t2) {
                    return t1.getNome().compareToIgnoreCase(t2.getNome());
                }
            });
        }

        if (searched && listSearched != null && listSearched.size() > 1) {
            Collections.sort(listSearched, new Comparator<Trip>() {
                @Override
                public int compare(Trip t1, Trip t2) {
                    return t1.getNome().compareToIgnoreCase(t2.getNome());
                }
            });
        }

    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
