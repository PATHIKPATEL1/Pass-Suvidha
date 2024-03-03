package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UpdateProfileActivity extends AppCompatActivity {

    private String update_profile_profession,update_profile_profile_image_uri,login_user;
    private TextView lbl_updation_msg;
    private Student student;
    private Employee employee;
    private Conductor conductor;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog_update_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseDatabase= FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        lbl_updation_msg=findViewById(R.id.lbl_updation_msg);

        details();

    }

    private void details()
    {
        Intent intent=getIntent();

        update_profile_profession=intent.getStringExtra("verify_adduser_profession_value");
        update_profile_profile_image_uri=intent.getStringExtra("verify_profile_image_uri");
        login_user=intent.getStringExtra("login_user");

        if(login_user.equals("Users")) {
            if (update_profile_profession.equals("Student")) {
                student = new Student();
                student.setFullname(intent.getStringExtra("verify_adduser_fullname"));
                student.setDate_of_birth(intent.getStringExtra("verify_adduser_date_of_birth"));
                student.setGender(intent.getStringExtra("verify_adduser_gender_value"));
                student.setMobileno(intent.getStringExtra("verify_adduser_mobileno"));
                student.setAddress(intent.getStringExtra("verify_adduser_address"));
                student.setState(intent.getStringExtra("verify_adduser_state"));
                student.setProfession(update_profile_profession);
                student.setEnrollmentno(intent.getStringExtra("verify_adduser_enrollmentno"));
                student.setInstitute_name(intent.getStringExtra("verify_adduser_institute_name"));
                student.setInstitute_city(intent.getStringExtra("verify_adduser_institute_city"));

            } else {
                employee = new Employee();
                employee.setFullname(intent.getStringExtra("verify_adduser_fullname"));
                employee.setDate_of_birth(intent.getStringExtra("verify_adduser_date_of_birth"));
                employee.setGender(intent.getStringExtra("verify_adduser_gender_value"));
                employee.setMobileno(intent.getStringExtra("verify_adduser_mobileno"));
                employee.setAddress(intent.getStringExtra("verify_adduser_address"));
                employee.setState(intent.getStringExtra("verify_adduser_state"));
                employee.setProfession(update_profile_profession);
                employee.setWork_address(intent.getStringExtra("verify_adduser_work_address"));

            }
        }else {
            conductor=new Conductor();
            conductor.setFullname(intent.getStringExtra("verify_adduser_fullname"));
            conductor.setMobileno(intent.getStringExtra("verify_adduser_mobileno"));
        }
        update(login_user);
    }

    private void update(String login_user){
        progressbar_show();

        user = mAuth.getCurrentUser();
        if(login_user.equals("Users")) {

            databaseReference = firebaseDatabase.getReference("Users");

            if (update_profile_profession.equals("Student")) {
                databaseReference.child(user.getUid()).setValue(student).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (update_profile_profile_image_uri != null) {
                                uploadProfileImage();
                            } else {
                                progressDialog_update_profile.dismiss();
                                lbl_updation_msg.setText("Profile is updated successfully.");
                                Toast.makeText(UpdateProfileActivity.this, "Profile is updated successfully.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            progressDialog_update_profile.dismiss();
                            Toast.makeText(UpdateProfileActivity.this, "Updation failed. " + task.getException().getMessage() + " Please try again", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            } else {
                databaseReference.child(user.getUid()).setValue(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (update_profile_profile_image_uri != null) {
                                uploadProfileImage();
                            } else {
                                progressDialog_update_profile.dismiss();
                                lbl_updation_msg.setText("Profile is updated successfully.");
                                Toast.makeText(UpdateProfileActivity.this, "Profile is updated successfully.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            progressDialog_update_profile.dismiss();
                            Toast.makeText(UpdateProfileActivity.this, "Updation failed. " + task.getException().getMessage() + " Please try again", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }
        }else {
            databaseReference = firebaseDatabase.getReference("Conductors");
             databaseReference.child(user.getUid()).setValue(conductor).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (update_profile_profile_image_uri != null) {
                                uploadProfileImage();
                            } else {
                                progressDialog_update_profile.dismiss();
                                lbl_updation_msg.setText("Profile is updated successfully.");
                                Toast.makeText(UpdateProfileActivity.this, "Profile is updated successfully.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            progressDialog_update_profile.dismiss();
                            Toast.makeText(UpdateProfileActivity.this, "Updation failed. " + task.getException().getMessage() + " Please try again", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            }
    }

    private void uploadProfileImage(){

        Uri file = Uri.parse(update_profile_profile_image_uri);
        StorageReference reference=mStorageRef.child("Profile_Images/"+user.getUid());
        reference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        deleteCache(getApplicationContext());
                        lbl_updation_msg.setText("Profile is updated successfully.");
                        Toast.makeText(UpdateProfileActivity.this, "Profile is updated successfully." , Toast.LENGTH_LONG).show();
                        progressDialog_update_profile.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(UpdateProfileActivity.this, "Failed to upload Profile image. " , Toast.LENGTH_LONG).show();
                        progressDialog_update_profile.dismiss();
                        finish();

                    }
                });
    }



    private static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }



    public void update_done(View v){
        Intent intent;

        if(login_user.equals("Users")){
            intent=new Intent(UpdateProfileActivity.this,HomeActivity.class);
        }
        else {
            intent=new Intent(UpdateProfileActivity.this,HomeConductorActivity.class);
        }
        intent.putExtra("view_details_check","yes");
        startActivity(intent);
        finishAffinity();
    }


    @Override
    public void onBackPressed() {
        Intent intent;

        if(login_user.equals("Users")){
            intent=new Intent(UpdateProfileActivity.this,HomeActivity.class);
        }
        else {
            intent=new Intent(UpdateProfileActivity.this,HomeConductorActivity.class);
        }        intent.putExtra("view_details_check","yes");
        startActivity(intent);
        finishAffinity();
    }

    private void progressbar_show()
    {
        progressDialog_update_profile= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

}
