package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Account extends AppCompatActivity {

    private EditText accountEmail;
    private EditText accountPassword;
    private EditText accountConfirmPass;
    private Button accountOk;
    private Intent returnIntent;
    private boolean create;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        accountEmail = (EditText) findViewById(R.id.accountEmail);
        accountPassword = (EditText) findViewById(R.id.accountPassword);
        accountOk = (Button) findViewById(R.id.accountOk);
        accountConfirmPass = (EditText) findViewById(R.id.accountConfirmPass);

        create = true;
        Bundle b = getIntent().getExtras();
        if (b!=null) {
            create = b.getBoolean("create",false);
            if (!create) {
                accountConfirmPass.setVisibility(View.GONE);
            }
        }

        accountOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterData();
            }
        });
    }

    private boolean confirmPassword() {
        if (create) {
            String password = accountPassword.getText().toString().trim();
            String confirmPass = accountConfirmPass.getText().toString().trim();
            if (confirmPass != null && password != null && !confirmPass.equals("") && !password.equals("")) {
                if (password.equals(confirmPass))
                    return true;
                else
                    return false;
            } else {
                return false;
            }
        }
        return true;
    }

    private void enterData() {
        String email = accountEmail.getText().toString().trim();
        String password = accountPassword.getText().toString().trim();
        if (email != null && password != null && !email.equals("") && !password.equals("")) {
            if (confirmPassword()) {
                returnIntent.putExtra("email",email);
                returnIntent.putExtra("password",password);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                toastShow("Senha deve ser igual nos dois campos");
            }
        } else {
            toastShow("Campos devem ser preenchidos");
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
