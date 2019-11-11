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

import com.application.entities.Passenger;
import com.application.entities.Trip;
import com.application.utils.CustomAdapterPassenger;
import com.application.utils.CustomAdapterTrip;
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

public class TripList extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;
    private ListView listTrips;
    private EditText editTripNameSearch;
    private AppCompatButton buttonSearchTrip;
    private ArrayList<Trip> listAll;
    private ArrayList<Trip> listSearched;
    private CustomAdapterTrip adapter;
    private CustomAdapterTrip aSearched;
    private final int OPEN_TRIP_REQUEST = 1;
    private boolean searched;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        listTrips = (ListView) findViewById(R.id.listTrips);
        editTripNameSearch = (EditText) findViewById(R.id.editTripNameSearch);
        buttonSearchTrip = (AppCompatButton) findViewById(R.id.buttonSearchTrip);

        listAll = new ArrayList<>();
        searched = false;

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser!=null) {
            databasePassengers.collection(currentUser.getUid())
                    .document("dados")
                    .collection("viagens")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                listTrips.setAdapter(adapter);
                            } else
                                toastShow("Erro ao acessar documentos: "+task.getException());
                        }
                    });
        } else {
            toastShow("Erro ao carregar usuário");
        }

        listTrips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Trip t;
                if (searched) {
                    t = listSearched.get(i);
                } else {
                    t = listAll.get(i);
                }
                Intent details = new Intent(getApplicationContext(), TripDetails.class);
                Bundle b = new Bundle();
                b.putParcelable("trip", t);
                details.putExtras(b);
                startActivityForResult(details,OPEN_TRIP_REQUEST);
            }
        });

        buttonSearchTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTrip();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_TRIP_REQUEST) {
            if (data != null) {
                Trip t = (Trip) data.getParcelableExtra("trip");
                boolean deleted = data.getBooleanExtra("deleted",false);
                boolean edited = data.getBooleanExtra("edited",false);
                if (t!=null) {
                    if (deleted)
                        removeTripOnLists(t);
                    else if (edited)
                        replaceTripOnLists(t);
                }
            }
        }
    }

    private void replaceTripOnLists(Trip t) {
        String newId = t.getId();

        if (listAll != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listAll.size(); i++) {
                Trip tri = listAll.get(i);
                if (tri.getId().equals(newId)) {
                    listAll.remove(i);
                    listAll.add(t);
                    notFound = false;
                }
            }
        }

        if (searched && listSearched != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listSearched.size(); i++) {
                Trip tri = listSearched.get(i);
                if (tri.getId().equals(newId)) {
                    listSearched.remove(tri);
                    listSearched.add(t);
                    notFound = false;
                }
            }
        }

        sortLists();
        listTrips.invalidateViews();
    }

    private void removeTripOnLists(Trip t) {
        String newId = t.getId();

        if (listAll != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listAll.size(); i++) {
                Trip tri = listAll.get(i);
                if (tri.getId().equals(newId)) {
                    listAll.remove(i);
                    notFound = false;
                }
            }
        }

        if (searched && listSearched != null) {
            boolean notFound = true;
            for (int i = 0; notFound && i < listSearched.size(); i++) {
                Trip tri = listSearched.get(i);
                if (tri.getId().equals(newId)) {
                    listSearched.remove(tri);
                    notFound = false;
                }
            }
        }

        sortLists();
        listTrips.invalidateViews();
    }

    private void searchTrip() {
        String name = editTripNameSearch.getText().toString().trim();
        if (name.isEmpty()) {
            listTrips.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (Trip t : listAll) {
                if (t.getNome().contains(name)) {
                    listSearched.add(t);
                }
            }
            aSearched = new CustomAdapterTrip(listSearched, getApplicationContext());
            listTrips.setAdapter(aSearched);
            searched = true;
        }
    }

    private void sortLists() {
        if (listAll != null && listAll.size() > 1) {
            Collections.sort(listAll, new Comparator<Trip>() {
                @Override
                public int compare(Trip t1, Trip t2) {
                    return t1.getNome().compareTo(t2.getNome());
                }
            });
        }

        if (searched && listSearched != null && listSearched.size() > 1) {
            Collections.sort(listSearched, new Comparator<Trip>() {
                @Override
                public int compare(Trip t1, Trip t2) {
                    return t1.getNome().compareTo(t2.getNome());
                }
            });
        }

    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
