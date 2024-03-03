package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText tb_forgot_password;
    private ProgressDialog progressDialog_forgot_password;
    private Button btn_forgot_password;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar =  findViewById(R.id.user_forgot_password_toolbar);
        toolbar.setTitle("Forgot Password");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tb_forgot_password=findViewById(R.id.tb_forgot_password);
        btn_forgot_password=findViewById(R.id.btn_forgot_password);


        tb_forgot_password.addTextChangedListener(forgotPasswordTextWatcher);

        btn_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressbar_show();
                mAuth = FirebaseAuth.getInstance();
                mAuth.sendPasswordResetEmail(tb_forgot_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            progressDialog_forgot_password.dismiss();
                            Toast.makeText(ForgotPasswordActivity.this,"Password Reset email is sent to your email address. please check your email",Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else {
                            progressDialog_forgot_password.dismiss();
                            Toast.makeText(ForgotPasswordActivity.this,"Process Failed. "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    private TextWatcher forgotPasswordTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String forgot_password_email=tb_forgot_password.getText().toString().trim();
            btn_forgot_password.setEnabled((Patterns.EMAIL_ADDRESS.matcher(forgot_password_email).matches()));
        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    private void progressbar_show()
    {
        progressDialog_forgot_password=ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);

    }
}
