package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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
    private final int ENTER_USER_REQUEST = 3;
    private String pass;
    private String passBase;
    private String loginMethod;
    private boolean createPIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        pass = null;
        passBase = null;
        loginMethod = null;

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        loginAnonymously();
        createPIN = true;

    }

    private void loginAnonymously() {
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
                                                    readLoginMethod();
                                                } else {
                                                    //USER NAO EXISTE, ENTÃO CRIA O DOCUMENTO DELE
                                                    createPIN = true;
                                                    openPinScreen();
                                                }
                                            }
                                        });
                            } else {
                                toastShow("Erro ao logar: "+task.getException());
                                finish();
                            }
                        }
                    });
        } else {
            toastShow("Usuário existente "+currentUser.getUid());
            readLoginMethod();
        }
    }

    private void readLoginMethod() {
        databasePassengers.collection(currentUser.getUid()).document("login").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Object rawLoginMethod = task.getResult().get("method");
                            if (rawLoginMethod != null) {
                                loginMethod = rawLoginMethod.toString();
                                if (loginMethod.equals("0")) {
                                    createPIN = false;
                                    askForPin();
                                } else if (loginMethod.equals("1")) {
                                    createPIN = false;
                                    askForPassword();
                                }
                            } else {
                                //SENHA NAO EXISTE, ENTÃO CRIA O DOCUMENTO DELE
                                createPIN = true;
                                openPinScreen();
                            }
                        } else {
                            //SENHA NAO EXISTE, ENTÃO CRIA O DOCUMENTO DELE
                            createPIN = true;
                            openPinScreen();
                        }
                    }
                });
    }

    private void askForPin() {
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
                        }
                        openPinScreen();
                    }
                });
    }

    private void openPinScreen() {
        Intent i = new Intent(getApplicationContext(), PIN.class);
        i.putExtra("create",createPIN);
        if (createPIN)
            startActivityForResult(i, CREATE_PIN_REQUEST);
        else
            startActivityForResult(i, ENTER_PIN_REQUEST);
    }

    private void askForPassword() {
        Intent i = new Intent(getApplicationContext(), Account.class);
        i.putExtra("create",false);
        startActivityForResult(i, ENTER_USER_REQUEST);
        /*databasePassengers.collection(currentUser.getUid()).document("key").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Object rawPassBase = task.getResult().get("key");
                            if (rawPassBase != null) {
                                passBase = rawPassBase.toString();
                                createPIN = false;
                            }
                        }
                        askForPin();
                    }
                });*/
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
        HashMap<String, String> loginMethod = new HashMap<>();
        loginMethod.put("method","0");
        databasePassengers.collection(currentUser.getUid()).document("exists").set(new HashMap<String, Object>());
        databasePassengers.collection(currentUser.getUid()).document("login").set(loginMethod);
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
            } else {
                finish();
            }
        } else if (requestCode == ENTER_USER_REQUEST) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra("email");
                String password = data.getStringExtra("password");
                if (email != null && password != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    mAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        startSystem();
                                    } else {
                                        toastShow("Erro ao entrar. "+task.getException().getMessage());
                                        System.out.println("        Erro: "+task.getException().getMessage());
                                        finish();
                                    }
                                }
                            });
                }
            }
        } else {
            finish();
        }
    }
}
