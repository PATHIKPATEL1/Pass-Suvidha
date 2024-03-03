package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText tb_user_current_password, tb_user_new_password;
    private TextView lbl_user_change_password_mag;
    private Button btn_user_change_password;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressDialog progressDialog_change_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);


        Toolbar toolbar = findViewById(R.id.user_change_password_toolbar);
        toolbar.setTitle("Change Password");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        tb_user_new_password = findViewById(R.id.tb_user_new_password);
        tb_user_current_password = findViewById(R.id.tb_user_current_password);
        lbl_user_change_password_mag = findViewById(R.id.lbl_user_change_password_mag);
        btn_user_change_password=findViewById(R.id.btn_user_change_password);

        tb_user_new_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    lbl_user_change_password_mag.setVisibility(View.VISIBLE);
                } else {
                    lbl_user_change_password_mag.setVisibility(View.GONE);
                }
            }
        });
        tb_user_new_password.addTextChangedListener(doneTextWatcher);
        tb_user_current_password.addTextChangedListener(doneTextWatcher);
    }


    private TextWatcher doneTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            btn_user_change_password.setEnabled((tb_user_current_password.length()>=6) && (tb_user_new_password.length()>=6));
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };

    public void change_password(View v) {

        progressbar_show();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), tb_user_current_password.getText().toString());

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(tb_user_new_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this,"Password successfully updated",Toast.LENGTH_LONG).show();
                                        progressDialog_change_password.dismiss();
                                        finish();
                                    } else {
                                        Toast.makeText(ChangePasswordActivity.this,"Error password not updated.",Toast.LENGTH_LONG).show();
                                        progressDialog_change_password.dismiss();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this,"Invalid password.",Toast.LENGTH_LONG).show();
                            progressDialog_change_password.dismiss();
                        }
                    }
                });
    }

    private void progressbar_show()
    {
        progressDialog_change_password= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

}