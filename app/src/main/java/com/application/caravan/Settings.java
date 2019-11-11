package com.application.caravan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class Settings extends AppCompatActivity {

    private AppCompatButton btnSettingsLogin;
    private AppCompatButton btnSettingsLogout;
    private AppCompatButton btnSettingsSignup;
    private TextView labelLoginStatus;
    private FirebaseAuth mAuth;
    private FirebaseFirestore databasePassengers;
    private FirebaseUser currentUser;
    private final int ENTER_USER_REQUEST = 1;
    private final int CREATE_USER_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnSettingsLogin = (AppCompatButton) findViewById(R.id.btnSettingsLogin);
        btnSettingsSignup = (AppCompatButton) findViewById(R.id.btnSettingsSignup);
        btnSettingsLogout = (AppCompatButton) findViewById(R.id.btnSettingsLogout);
        labelLoginStatus = (TextView) findViewById(R.id.labelLoginStatus);

        mAuth = FirebaseAuth.getInstance();
        databasePassengers = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        updateScreen();

        btnSettingsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLogin(false);
            }
        });

        btnSettingsSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLogin(true);
            }
        });

        btnSettingsLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

    }

    private void updateScreen() {
        if (currentUser!=null) {
            if (!currentUser.isAnonymous()) {
                labelLoginStatus.setText("A conta "+currentUser.getEmail()+" está ativa.");
                btnSettingsLogout.setVisibility(View.VISIBLE);
                btnSettingsLogin.setVisibility(View.GONE);
                btnSettingsSignup.setVisibility(View.GONE);
            } else {
                labelLoginStatus.setText("Nenhuma conta ativa. Faça login ou crie uma conta para ativar backup de dados.");
                btnSettingsLogin.setVisibility(View.VISIBLE);
                btnSettingsSignup.setVisibility(View.VISIBLE);
                btnSettingsLogout.setVisibility(View.GONE);
            }
        }
    }

    private void openLogin(boolean create) {
        Intent i = new Intent(getApplicationContext(), Account.class);
        i.putExtra("create",create);
        if (create) {
            startActivityForResult(i, CREATE_USER_REQUEST);
        }
        else {
            startActivityForResult(i, ENTER_USER_REQUEST);
        }
    }

    private void logout() {
        mAuth.signOut();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            toastShow("Usuário logado: "+currentUser.getUid());
                            updateScreen();
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
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_USER_REQUEST) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra("email");
                String password = data.getStringExtra("password");
                if (email != null && password != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                    currentUser.linkWithCredential(credential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        currentUser = task.getResult().getUser();
                                        System.out.println("                DEU CERTO");
                                        System.out.println("                USER EMAIL: "+currentUser.getEmail());
                                        updateScreen();
                                    } else {
                                        System.out.println("                DEU RUIM");
                                        System.out.println("                "+task.getException().getMessage());
                                    }
                                }
                            });
                }
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
                                        updateScreen();
                                    } else {
                                        toastShow("Erro ao entrar. "+task.getException().getMessage());
                                        System.out.println("        Erro: "+task.getException().getMessage());
                                    }
                                }
                            });
                }
            }
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
