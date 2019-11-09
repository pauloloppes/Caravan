package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Splash extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;
    private final int ENTER_PIN_REQUEST = 1;
    private final int CREATE_PIN_REQUEST = 2;
    private String pass;
    private String passBase;
    private boolean createPIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        pass = null;
        passBase = null;

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();
                                toastShow("Usuário logado: "+currentUser.getUid());
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
                            } else {
                                toastShow("Erro ao logar: "+task.getException());
                            }
                        }
                    });
        } else {
            toastShow("Usuário existente "+currentUser.getUid());
        }

        createPIN = true;
        databasePassengers.collection(currentUser.getUid()).document("key").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Object rawPassBase = task.getResult().get("key");
                            if (rawPassBase != null) {
                                passBase = rawPassBase.toString();
                                createPIN = false;
                            }
                            askForPin();
                        } else {
                            askForPin();
                        }
                    }
                });

    }

    private void askForPin() {
        Intent i = new Intent(getApplicationContext(), PIN.class);
        i.putExtra("create",createPIN);
        if (createPIN)
            startActivityForResult(i, CREATE_PIN_REQUEST);
        else
            startActivityForResult(i, ENTER_PIN_REQUEST);
    }

    private void startSystem() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void checkPassword() {
        if (passBase.equals(pass)) {
            startSystem();
        } else {
            toastShow("Senha errada");
            askForPin();
        }
    }

    private void recordPassword() {
        HashMap<String, String> h = new HashMap<>();
        h.put("key",pass);
        databasePassengers.collection(currentUser.getUid()).document("key").set(h)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            toastShow("Senha gravada com sucesso");
                            startSystem();
                        } else {
                            toastShow("Erro ao gravar senha");
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENTER_PIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                pass = data.getStringExtra("password");
                if (pass != null) {
                    checkPassword();
                } else {
                    toastShow("Erro ao carregar senha");
                }
            } else {
                finish();
            }
        } else if (requestCode == CREATE_PIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                pass = data.getStringExtra("password");
                if (pass != null) {
                    recordPassword();
                } else {
                    toastShow("Erro ao carregar senha");
                }
            }
        }
    }
}
