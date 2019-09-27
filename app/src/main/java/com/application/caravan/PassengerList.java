package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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
    private ListView listPassengers;
    private CustomAdapterPassenger adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_list);

        listPassengers = (ListView) findViewById(R.id.listPassengers);

        listAll = new ArrayList<>();

        databasePassengers = FirebaseFirestore.getInstance();

        /*databasePassengers.collection("passageiros").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Passenger p = doc.toObject(Passenger.class);
                        System.out.println("                    CRIOU PASSAGEIRO "+p.getNome());
                        p.setId(doc.getId());
                        listAll.add(p);
                    }

                } else {
                    toastShow("Erro ao acessar documentos: "+task.getException());
                }

            }
        });*/

        databasePassengers.collection("passageiros").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    listAll.addAll(queryDocumentSnapshots.toObjects(Passenger.class));
                    System.out.println("                    CRIOU A LISTA ");
                    //List<String> tes = new ArrayList<String>();

                    /*for (Passenger p : listAll) {
                        System.out.println("                  PERCORREU LISTA NOME "+p.getNome());
                        //toastShow("Leu os cara");
                        tes.add(p.getNome());
                    }





                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            getApplicationContext(), android.R.layout.simple_list_item_1,
                            tes
                    );

                    listPassengers.setAdapter(arrayAdapter);*/
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

        /*Task<QuerySnapshot> task = databasePassengers.collection("passageiros").get();
        if (task.isSuccessful()) {
            for (DocumentSnapshot doc : task.getResult()) {
                Passenger p = doc.toObject(Passenger.class);
                System.out.println("                    CRIOU PASSAGEIRO "+p.getNome());
                p.setId(doc.getId());
                listAll.add(p);
            }
        } else {
            toastShow("Erro ao acessar documentos: "+task.getException());
        }*/




    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
