package com.example.passsuvidha;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class AuthenticationActivity extends AppCompatActivity {

    private Button btn_verify_signin;
    private String verify_profession,user_email,user_password,verify_profile_image_uri;
    private Student student;
    private Employee employee;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog_authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        if(user!=null){
            mAuth.getInstance().signOut();
        }

        btn_verify_signin=findViewById(R.id.btn_verify_signin);
        btn_verify_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify_signin();
            }
        });

        details();
        verification();

    }


    private void details()
    {
        Intent intent=getIntent();

        verify_profession=intent.getStringExtra("verify_adduser_profession_value");
        user_email=intent.getStringExtra("verify_adduser_email");
        user_password=intent.getStringExtra("verify_adduser_password");
        verify_profile_image_uri=intent.getStringExtra("verify_profile_image_uri");


        if(verify_profession.equals("Student"))
        {
            student=new Student();
            student.setFullname(intent.getStringExtra("verify_adduser_fullname"));
            student.setDate_of_birth(intent.getStringExtra("verify_adduser_date_of_birth"));
            student.setGender(intent.getStringExtra("verify_adduser_gender_value"));
            student.setMobileno(intent.getStringExtra("verify_adduser_mobileno"));
            student.setAddress(intent.getStringExtra("verify_adduser_address"));
            student.setState(intent.getStringExtra("verify_adduser_state"));
            student.setProfession(verify_profession);
            student.setEnrollmentno(intent.getStringExtra("verify_adduser_enrollmentno"));
            student.setInstitute_name(intent.getStringExtra("verify_adduser_institute_name"));
            student.setInstitute_city(intent.getStringExtra("verify_adduser_institute_city"));

        }
        else {
            employee=new Employee();
            employee.setFullname(intent.getStringExtra("verify_adduser_fullname"));
            employee.setDate_of_birth(intent.getStringExtra("verify_adduser_date_of_birth"));
            employee.setGender(intent.getStringExtra("verify_adduser_gender_value"));
            employee.setMobileno(intent.getStringExtra("verify_adduser_mobileno"));
            employee.setAddress(intent.getStringExtra("verify_adduser_address"));
            employee.setState(intent.getStringExtra("verify_adduser_state"));
            employee.setProfession(verify_profession);
            employee.setWork_address(intent.getStringExtra("verify_adduser_work_address"));

        }

    }


    private void verification(){
        progressbar_show();

        mAuth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            user = mAuth.getCurrentUser();
                            databaseReference=firebaseDatabase.getReference("Users");

                            if(verify_profession.equals("Student")){
                                databaseReference.child(user.getUid()).setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            uploadProfileImage();
                                        }else {
                                            Toast.makeText(AuthenticationActivity.this, "Registration failed. "+task.getException().getMessage()+" Please try again",Toast.LENGTH_LONG).show();
                                            removeUser();
                                        }
                                    }
                                });
                            }
                            else {
                                databaseReference.child(user.getUid()).setValue(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            uploadProfileImage();
                                        }else {
                                            Toast.makeText(AuthenticationActivity.this, "Registration failed. "+task.getException().getMessage()+" Please try again",Toast.LENGTH_LONG).show();
                                            removeUser();
                                        }
                                    }
                                });
                            }


                        }
                        if(task.getException() instanceof FirebaseAuthUserCollisionException)
                        {
                            Toast.makeText(AuthenticationActivity.this,"User Already Exist",Toast.LENGTH_LONG).show();
                            progressDialog_authentication.dismiss();
                            mAuth.getInstance().signOut();
                            finish();

                        }else if(task.getException() != null) {
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed. "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            progressDialog_authentication.dismiss();
                            mAuth.getInstance().signOut();
                            finish();
                        }

                    }
                });
    }


    private void removeUser(){
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mAuth.getInstance().signOut();
                    progressDialog_authentication.dismiss();
                    finish();
                }
                else {
                    Toast.makeText(AuthenticationActivity.this, "Failed to remove the user. "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    progressDialog_authentication.dismiss();
                    finish();
                }
            }
        });
    }


    private void sendVerificationEmail(){
        user = mAuth.getCurrentUser();
        if(user!=null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AuthenticationActivity.this, "Email Sent. Please check your email for verification", Toast.LENGTH_LONG).show();
                                progressDialog_authentication.dismiss();
                                btn_verify_signin.setEnabled(true);
                                mAuth.getInstance().signOut();

                            } else {
                                Toast.makeText(AuthenticationActivity.this, "Failed to send verification email. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                removeUser();
                            }
                        }
                    });
        }else {
            Toast.makeText(AuthenticationActivity.this, "Failed to send verification email. " , Toast.LENGTH_LONG).show();
            removeUser();
        }
    }


    private void uploadProfileImage(){

        Uri file = Uri.parse(verify_profile_image_uri);
        StorageReference reference=mStorageRef.child("Profile_Images/"+user.getUid());
        reference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sendVerificationEmail();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(AuthenticationActivity.this, "Failed to upload Profile image. " , Toast.LENGTH_LONG).show();
                        removeUser();

                    }
                });
    }


    private void verify_signin() {

        final AlertDialog.Builder username_message = new AlertDialog.Builder(AuthenticationActivity.this);
        username_message.setCancelable(false);
        username_message.setMessage(Html.fromHtml("Your Email Address <font color = '#2e91d3' > " + user_email + " </font> is Considered as a Username "));
        username_message.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mAuth.getInstance().signOut();
                startActivity(new Intent(AuthenticationActivity.this, SignInActivity.class));
                finishAffinity();

            }
        });
        username_message.create().show();

    }

    @Override
    public void onBackPressed() {
        verify_signin();
    }

    private void progressbar_show()
    {
        progressDialog_authentication= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}
