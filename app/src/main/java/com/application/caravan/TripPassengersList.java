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

public class TripPassengersList extends AppCompatActivity {

    private ArrayList<Passenger> listAll;
    private ArrayList<Passenger> listSearched;
    private ListView listPassengersTrip;
    private EditText editPassengerNameSearchTrip;
    private AppCompatButton buttonSearchPassengerTrip;
    private AppCompatButton buttonPassengerListAdd;
    private CustomAdapterPassenger adapter;
    private CustomAdapterPassenger aSearched;
    private final int EDIT_PASSENGER_REQUEST = 1;
    private final int ADD_PASSENGER_REQUEST = 2;
    private Trip t;
    private boolean searched;
    private DBLink dbLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_passengers_list);

        listPassengersTrip = (ListView) findViewById(R.id.listPassengersTrip);
        editPassengerNameSearchTrip = (EditText) findViewById(R.id.editPassengerNameSearchTrip);
        buttonSearchPassengerTrip = (AppCompatButton) findViewById(R.id.buttonSearchPassengerTrip);
        buttonPassengerListAdd = (AppCompatButton) findViewById(R.id.buttonPassengerListAdd);

        listAll = new ArrayList<>();
        searched = false;
        dbLink = new DBLink();

        t = (Trip) getIntent().getParcelableExtra("trip");
        if (t == null) {
            toastShow("Falha ao carregar dados da viagem");
            finish();
        }

        searchPassengersOnDB();

        listPassengersTrip.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Passenger p;
                if (searched) {
                    p = listSearched.get(i);
                } else {
                    p = listAll.get(i);
                }
                Intent details = new Intent(getApplicationContext(), TripEditPassenger.class);
                Bundle b = new Bundle();
                b.putParcelable("passenger", p);
                b.putParcelable("trip",t);
                details.putExtras(b);
                startActivityForResult(details,EDIT_PASSENGER_REQUEST);
            }
        });

        buttonSearchPassengerTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPassenger();
            }
        });

        buttonPassengerListAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPassenger();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PASSENGER_REQUEST) {
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
        } else if (requestCode == ADD_PASSENGER_REQUEST) {
            if (data != null && resultCode == RESULT_OK) {
                Trip tripResponse = (Trip) data.getParcelableExtra("trip");
                Passenger pasResponse = (Passenger) data.getParcelableExtra("passenger");
                if (tripResponse != null && tripResponse.getId().equals(t.getId())) {
                    if (pasResponse!= null) {
                        addPassengerOnLists(pasResponse);
                    }
                }
            }
        }
    }

    private void addPassengerOnLists(Passenger p) {
        if (p != null) {

            addPasViagemID(p);
            listAll.add(p);

            if (searched) {
                String name = editPassengerNameSearchTrip.getText().toString().trim().toLowerCase();
                if (!name.isEmpty()) {
                    if (p.getNome().toLowerCase().contains(name)) {
                        listSearched.add(p);
                    }
                }
            }
            sortLists();
            listPassengersTrip.invalidateViews();
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
        listPassengersTrip.invalidateViews();
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
        listPassengersTrip.invalidateViews();
    }

    private void searchPassengersOnDB() {

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        searchPassengerByID(document.get("passageiro").toString(),
                                document.getId(),
                                document.get("veiculo").toString(),
                                document.get("assento").toString(),
                                document.get("embarque").toString(),
                                document.get("desembarque").toString(),
                                document.get("valorpago").toString()
                        );
                    }

                    sortLists();

                    adapter = new CustomAdapterPassenger(listAll, getApplicationContext());
                    listPassengersTrip.setAdapter(adapter);
                } else
                    toastShow("Erro ao acessar documentos: "+task.getException());
            }
        };

        dbLink.getAllPassengersFromTrip(t.getId(), listenerComplete);

    }

    private void addPasViagemID(final Passenger p) {

        OnCompleteListener listenerComplete = new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    p.setPasviagemId(document.getId());
                }
            }
        };

        dbLink.getPasviagem(p.getId(),t.getId(),listenerComplete);

    }

    private void searchPassengerByID(String pasID, final String dbID, final String vehicle, final String seat, final String boarding, final String landing, final String paidAmount) {

        OnCompleteListener listenerComplete = new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    Passenger p = doc.toObject(Passenger.class);
                    p.setId(doc.getId());
                    p.setPasviagemId(dbID);
                    p.setPasviagemVeiculo(vehicle);
                    p.setPasviagemAssento(seat);
                    p.setPasviagemEmbarque(boarding);
                    p.setPasviagemDesembarque(landing);
                    p.setPasviagemValorPago(paidAmount);
                    System.out.println("\t\t"+dbID);
                    listAll.add(p);
                    listPassengersTrip.invalidateViews();
                }
            }
        };

        dbLink.getPassengerById(pasID, listenerComplete);

    }

    private void searchPassenger() {
        String name = editPassengerNameSearchTrip.getText().toString().trim().toLowerCase();
        if (name.isEmpty()) {
            listPassengersTrip.setAdapter(adapter);
            searched = false;
        } else {
            listSearched = new ArrayList<>();
            for (Passenger p : listAll) {
                if (p.getNome().toLowerCase().contains(name)) {
                    listSearched.add(p);
                }
            }
            aSearched = new CustomAdapterPassenger(listSearched, getApplicationContext());
            listPassengersTrip.setAdapter(aSearched);
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

    private void addPassenger() {
        Intent add = new Intent(getApplicationContext(), TripAddPassenger.class);
        Bundle a = new Bundle();
        a.putParcelable("trip",t);
        add.putExtras(a);
        startActivityForResult(add,ADD_PASSENGER_REQUEST);
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
