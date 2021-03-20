package com.application.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DBLink {

    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Context context;

    public DBLink(Context context) {

        this.context = context;

        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

    }

    public void addPassenger(String name, String birth, String identity, String idType, String phone, String address,OnCompleteListener listener) {
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

}
