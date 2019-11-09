package com.application.caravan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.utils.Encryption;

public class PIN extends AppCompatActivity {

    private AppCompatButton btnPin1;
    private AppCompatButton btnPin2;
    private AppCompatButton btnPin3;
    private AppCompatButton btnPin4;
    private AppCompatButton btnPin5;
    private AppCompatButton btnPin6;
    private AppCompatButton btnPin7;
    private AppCompatButton btnPin8;
    private AppCompatButton btnPin9;
    private AppCompatButton btnPin0;
    private AppCompatButton btnPinErase;
    private AppCompatButton btnPinEnter;
    private TextView txtPinNo1;
    private TextView txtPinNo2;
    private TextView txtPinNo3;
    private TextView txtPinNo4;
    private TextView txtPinLabel;
    private String pin1;
    private String pin2;
    private String pin3;
    private String pin4;
    private int numberPosition;
    private Intent returnIntent;
    private boolean create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        numberPosition = 1;
        returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        btnPin0 = (AppCompatButton) findViewById(R.id.btnPin0);
        btnPin1 = (AppCompatButton) findViewById(R.id.btnPin1);
        btnPin2 = (AppCompatButton) findViewById(R.id.btnPin2);
        btnPin3 = (AppCompatButton) findViewById(R.id.btnPin3);
        btnPin4 = (AppCompatButton) findViewById(R.id.btnPin4);
        btnPin5 = (AppCompatButton) findViewById(R.id.btnPin5);
        btnPin6 = (AppCompatButton) findViewById(R.id.btnPin6);
        btnPin7 = (AppCompatButton) findViewById(R.id.btnPin7);
        btnPin8 = (AppCompatButton) findViewById(R.id.btnPin8);
        btnPin9 = (AppCompatButton) findViewById(R.id.btnPin9);
        btnPinErase = (AppCompatButton) findViewById(R.id.btnPinErase);
        btnPinEnter = (AppCompatButton) findViewById(R.id.btnPinEnter);
        txtPinNo1 = (TextView) findViewById(R.id.txtPinNo1);
        txtPinNo2 = (TextView) findViewById(R.id.txtPinNo2);
        txtPinNo3 = (TextView) findViewById(R.id.txtPinNo3);
        txtPinNo4 = (TextView) findViewById(R.id.txtPinNo4);
        txtPinLabel = (TextView) findViewById(R.id.txtPinLabel);
        pin1 = "X";
        pin2 = "X";
        pin3 = "X";
        pin4 = "X";

        setButtonListeners();

        create = true;
        Bundle b = getIntent().getExtras();
        if (b!=null) {
            create = b.getBoolean("create");
            if (create) {
                txtPinLabel.setText("Crie uma senha de 4 dígitos para autorizar acesso ao aplicativo");
            } else {
                txtPinLabel.setText("Digite sua senha de 4 dígitos");
            }
        }

    }

    private void setButtonListeners() {
        btnPin0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(0);
            }
        });

        btnPin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(1);
            }
        });

        btnPin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(2);
            }
        });

        btnPin3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(3);
            }
        });

        btnPin4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(4);
            }
        });

        btnPin5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(5);
            }
        });

        btnPin6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(6);
            }
        });

        btnPin7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(7);
            }
        });

        btnPin8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(8);
            }
        });

        btnPin9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNumber(9);
            }
        });

        btnPinErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eraseNumber();
            }
        });

        btnPinEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterPIN();
            }
        });
    }

    private void inputNumber(Integer number) {

        if (numberPosition == 1) {
            pin1 = number.toString();
            txtPinNo1.setText("⬤");
            numberPosition++;
        } else if (numberPosition == 2) {
            pin2 = number.toString();
            txtPinNo2.setText("⬤");
            numberPosition++;
        } else if (numberPosition == 3) {
            pin3 = number.toString();
            txtPinNo3.setText("⬤");
            numberPosition++;
        } else if (numberPosition == 4) {
            pin4 = number.toString();
            txtPinNo4.setText("⬤");
            numberPosition++;
        }

    }

    private void eraseNumber() {

        if (numberPosition == 5) {
            pin4 = "X";
            txtPinNo4.setText("");
            numberPosition--;
        } else if (numberPosition == 4) {
            pin3 = "X";
            txtPinNo3.setText("");
            numberPosition--;
        } else if (numberPosition == 3) {
            pin2 = "X";
            txtPinNo2.setText("");
            numberPosition--;
        } else if (numberPosition == 2) {
            pin1 = "X";
            txtPinNo1.setText("");
            numberPosition--;
        }

    }

    private void enterPIN() {
        if (pin1.equals("X") || pin2.equals("X") || pin3.equals("X") || pin4.equals("X")) {
            toastShow("PIN deve conter 4 dígitos.");
        } else {
            String encPass = Encryption.generatePassword(pin1+pin2+pin3+pin4);
            returnIntent.putExtra("password",encPass);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private void toastShow (String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}


