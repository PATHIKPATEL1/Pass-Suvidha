
package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    private ProgressDialog progressDialog_signin;
    private EditText tb_username_signin,tb_password_signin;
    private ImageView ic_user_traveller, ic_conductor;
    private String login_user="Users";
    private Button btn_signin, btn_signup;
    private View view_change_user;
    private RelativeLayout relativeLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();

        findElement();
    }

    private void findElement(){
        btn_signin=findViewById(R.id.btn_signin);
        btn_signup =findViewById(R.id.btn_signup);
        tb_username_signin=findViewById(R.id.tb_username_signin);
        tb_password_signin=findViewById(R.id.tb_password_signin);
        tb_username_signin.addTextChangedListener(signinTextWatcher);
        tb_password_signin.addTextChangedListener(signinTextWatcher);
        ic_user_traveller =findViewById(R.id.ic_user_traveller);
        ic_conductor =findViewById(R.id.ic_conductor);
        relativeLayout=findViewById(R.id.relativeLayout);
        view_change_user=findViewById(R.id.view_change_user);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tb_password_signin.setText("");
    }

    private TextWatcher signinTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String username_signin=tb_username_signin.getText().toString().trim();
            String password_signin=tb_password_signin.getText().toString().trim();
            btn_signin.setEnabled((Patterns.EMAIL_ADDRESS.matcher(username_signin).matches()) && (password_signin.length()>=6));
        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    };





    public void signIn(View v) {
        String username_signin = tb_username_signin.getText().toString();
        String password_signin = tb_password_signin.getText().toString();
        progressbar_show();

        mAuth.signInWithEmailAndPassword(username_signin, password_signin)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            user=mAuth.getCurrentUser();
                            if(user.isEmailVerified()) {

                                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference(login_user);
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(user.getUid())) {
                                            progressDialog_signin.dismiss();
                                            Toast.makeText(SignInActivity.this, "SignIn Successfully...", Toast.LENGTH_LONG).show();
                                            Intent intent;
                                            if(login_user.equals("Users")){
                                                intent = new Intent(SignInActivity.this, HomeActivity.class);
                                            }
                                            else {
                                                intent = new Intent(SignInActivity.this, HomeConductorActivity.class);
                                            }
                                            startActivity(intent);
                                            finishAffinity();
                                        }
                                        else {
                                        cancelUser("Invalid username or password");
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        cancelUser("Error");
                                    }
                                });

                            }else {
                                cancelUser("Please verify your email address");
                            }
                        } else {
                            cancelUser("Invalid username or password");
                        }

                    }
                });
    }

    public void changeConductor(View v){
            if(!login_user.equals("Conductors")) {
                tb_username_signin.setText("");
                tb_username_signin.requestFocus();
                tb_password_signin.setText("");
                login_user = "Conductors";
                ic_user_traveller.setImageResource(R.drawable.ic_traveller_gray);
                btn_signup.setVisibility(View.GONE);

                relativeLayout.setVisibility(View.GONE);
                view_change_user.startAnimation(AnimationUtils.loadAnimation(SignInActivity.this, R.anim.move_user_right));
                ic_conductor.setImageResource(R.drawable.ic_conductor_blue);
            }
        }

    public void changeTraveller(View v){
        if(!login_user.equals("Users")) {
            tb_username_signin.setText("");
            tb_username_signin.requestFocus();
            tb_password_signin.setText("");
            login_user = "Users";
            relativeLayout.setVisibility(View.VISIBLE);
            ic_conductor.setImageResource(R.drawable.ic_conductor_gray);

            ic_user_traveller.setImageResource(R.drawable.ic_traveller_blue);
            view_change_user.startAnimation(AnimationUtils.loadAnimation(SignInActivity.this, R.anim.move_user_left));
            btn_signup.setVisibility(View.VISIBLE);
        }
        }

    private void cancelUser(String msg){
        mAuth.getInstance().signOut();
        Toast.makeText(SignInActivity.this,msg , Toast.LENGTH_LONG).show();
        progressDialog_signin.dismiss();
    }

    public void signUp(View view)
    {
        Intent intent=new Intent(SignInActivity.this,RegistrationActivity.class);
        intent.putExtra("update","no");
        startActivity(intent);
    }

    public void forgotPassword(View v){
        startActivity(new Intent(SignInActivity.this,ForgotPasswordActivity.class));
    }

    private void progressbar_show()
    {
        progressDialog_signin=ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}
