package com.application.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.application.entities.Passenger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBLink {

    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public DBLink() {

        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null)
            throw new RuntimeException("Erro ao carregar usuário.");

    }

    public void addPassenger(String name, String birth, String identity, String idType, String phone, String address, OnCompleteListener listener) {
        Map passenger = new HashMap<>();
        passenger.put("nome", name);
        passenger.put("dataNascimento", birth);
        passenger.put("identidade", identity);
        passenger.put("tipoIdentidade", idType);
        passenger.put("telefone", phone);
        passenger.put("endereco", address);

        database.collection(currentUser.getUid())
                .document("dados")
                .collection("passageiros")
                .add(passenger)
                .addOnCompleteListener(listener);
    }

    public void updatePassenger(Map updatedDocument, String passengerId, OnCompleteListener<QuerySnapshot> listener) {
        database.collection(currentUser.getUid())
                .document("dados")
                .collection("passageiros")
                .document(passengerId)
                .update(updatedDocument)
                .addOnCompleteListener(listener);
    }

    public void deletePassenger(String passengerId, OnSuccessListener listenerSuccess, OnFailureListener listenerFailure) {
        database.collection(currentUser.getUid())
                .document("dados")
                .collection("passageiros")
                .document(passengerId)
                .delete()
                .addOnSuccessListener(listenerSuccess)
                .addOnFailureListener(listenerFailure);
    }

    public void getAllPassengers(OnCompleteListener<QuerySnapshot> listener) {
        database.collection(currentUser.getUid())
            .document("dados")
            .collection("passageiros")
            .get()
            .addOnCompleteListener(listener);
    }

}
