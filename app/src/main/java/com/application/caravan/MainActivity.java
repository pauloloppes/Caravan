package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();

        currentUser = mAuth.getCurrentUser();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            //toastShow("Usuário logado: "+currentUser.getUid());
                        } else {
                            toastShow("Erro ao logar: "+task.getException());
                        }
                    }
                });


        databasePassengers.collection(currentUser.getUid())
                .document("exists")
                .update(new HashMap<String, Object>())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //USER EXISTE
                        } else {
                            //USER NAO EXISTE, ENTÃO CRIA O DOCUMENTO DELE
                            databasePassengers.collection(currentUser.getUid()).document("exists").set(new HashMap<String, Object>());
                        }
                    }
                });

        databasePassengers.collection("users").document("a").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            toastShow("conseguiu ler outra collection");
                        } else {
                            toastShow("DEU ERRO, ISSO AÍ GAROTO! "+task.getException());
                        }
                    }
                });

        databasePassengers.collection(currentUser.getUid()).document("exists").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                            System.out.println("            CONSEGUIU LER DA SUA PERMISSÃO");
                        else
                            System.out.println("            DEU RUIM MOLEQUE: "+task.getException());
                    }
                });


        //Setting up button to open Passenger Add screen
        final AppCompatButton passengerAdd = (AppCompatButton)findViewById(R.id.buttonAddPassenger);
        passengerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PassengerAdd.class));
            }
        });

        //Setting up button to open Passenger List screen
        final AppCompatButton passengerList = (AppCompatButton)findViewById(R.id.buttonListPassengers);
        passengerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PassengerList.class));
            }
        });
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
