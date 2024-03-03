package com.example.passsuvidha;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private CircleImageView ic_home_profile_image;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog_home;
    private ProgressBar progressBar_profile_image_home;
    private ImageView ic_view_profile,ic_apply_for_pass,ic_view_pass,ic_track_bus;
    private EditText tb_track_bus_no_prompt;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private int profile_updated=0;
    private View promptsView;
    private Student student;
    private Employee employee;
    private String temp_auth,bus_no_pattern="GJ-18-Z-([0-9][0-9][0-9][0-9])";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar =  findViewById(R.id.user_home_toolbar);
        toolbar.setTitle("Pass Suvidha");
        setSupportActionBar(toolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        progressBar_profile_image_home=findViewById(R.id.progressBar_profile_image_home);
        ic_apply_for_pass =findViewById(R.id.ic_apply_pass);
        ic_view_profile=findViewById(R.id.ic_view_profile);
        ic_track_bus=findViewById(R.id.ic_track_bus);
        ic_view_pass=findViewById(R.id.ic_view_pass);
        ic_home_profile_image=(CircleImageView) findViewById(R.id.ic_home_profile_image);

        Intent intent=getIntent();
        if(intent.getStringExtra("view_details_check")!=null){
            profile_updated=1;
            viewDetails();
        }


        if(user!=null){
            setProfileImage();
        }else {
            startActivity(new Intent(HomeActivity.this,SignInActivity.class));
            finishAffinity();
        }

        ic_view_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_updated=1;
                viewDetails();
            }
        });

        ic_apply_for_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,ApplyForPassActivity.class));
            }
        });

        ic_view_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,ShowPdfActivity.class));
            }
        });

        ic_track_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterBusNo();
            }
        });
    }



    private void enterBusNo(){
        promptsView = getLayoutInflater().inflate(R.layout.activity_track_bus_dialog_prompt, null);
        tb_track_bus_no_prompt=(EditText) promptsView.findViewById(R.id.tb_track_bus_no_prompt);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);

        alertDialogBuilder.setView(promptsView);

            alertDialogBuilder.setTitle("Enter Bus Number");
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Done",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {dialog.cancel();}
                            }
                    ).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(!tb_track_bus_no_prompt.getText().toString().trim().matches(bus_no_pattern)){
                    tb_track_bus_no_prompt.setError("Please Enter Valid Bus Number");
                }
                else{
                    alertDialog.dismiss();
                    Intent intent=new Intent(HomeActivity.this,TrackBusActivity.class);
                    intent.putExtra("bus_no",tb_track_bus_no_prompt.getText().toString().trim());
                    startActivity(intent);
                }
            } });
    }


    private void viewDetails(){
        progressbar_show();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (profile_updated == 1) {
                    if (dataSnapshot.exists()) {
                        if ((dataSnapshot.child("profession").getValue().toString()).equals("Student")) {
                            viewStudentDetails(dataSnapshot);
                        } else {
                            viewEmployeeDetails(dataSnapshot);
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                        progressDialog_home.dismiss();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                progressDialog_home.dismiss();
                finish();
            }
        });
    }

    private void viewEmployeeDetails(DataSnapshot dataSnapshot) {
        employee=new Employee();
        employee=dataSnapshot.getValue(Employee.class);

        Intent intent=new Intent(HomeActivity.this,ViewProfileActivity.class);
        intent.putExtra("Fullname",employee.getFullname());
        intent.putExtra("Date Of Birth",employee.getDate_of_birth());
        intent.putExtra("Gender",employee.getGender());
        intent.putExtra("Email Id",user.getEmail());
        intent.putExtra("Mobile Number",employee.getMobileno());
        intent.putExtra("Address",employee.getAddress());
        intent.putExtra("State",employee.getState());
        intent.putExtra("Profession",employee.getProfession());
        intent.putExtra("Work Address",employee.getWork_address());
        profile_updated=0;
        progressDialog_home.dismiss();
        startActivity(intent);
    }

    private void viewStudentDetails(DataSnapshot dataSnapshot) {
        student=new Student();
        student=dataSnapshot.getValue(Student.class);

        Intent intent=new Intent(HomeActivity.this,ViewProfileActivity.class);
        intent.putExtra("Fullname",student.getFullname());
        intent.putExtra("Date Of Birth",student.getDate_of_birth());
        intent.putExtra("Gender",student.getGender());
        intent.putExtra("Email Id",user.getEmail());
        intent.putExtra("Mobile Number",student.getMobileno());
        intent.putExtra("Address",student.getAddress());
        intent.putExtra("State",student.getState());
        intent.putExtra("Profession",student.getProfession());
        intent.putExtra("Enrollment Number",student.getEnrollmentno());
        intent.putExtra("Institute_Name",student.getInstitute_name());
        intent.putExtra("Institute_City",student.getInstitute_city());
        profile_updated=0;
        progressDialog_home.dismiss();
        startActivity(intent);
    }


    private void setProfileImage() {
        progressBar_profile_image_home.setVisibility(View.VISIBLE);

        final File localFile =new File(this.getCacheDir(),user.getUid()+".jpg");
        StorageReference reference=mStorageRef.child("Profile_Images/"+user.getUid());
        reference.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        progressBar_profile_image_home.setVisibility(View.GONE);
                        ic_home_profile_image.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(HomeActivity.this,"Can not download profile image",Toast.LENGTH_LONG).show();
                progressBar_profile_image_home.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.home_act_menu,menu);
        if(user!=null) {
            FirebaseDatabase.getInstance().getReference("PassRequest").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    if (dataSnapshot1.exists()) {
                        temp_auth = dataSnapshot1.child("auth").getValue().toString();
                        if (!temp_auth.equals("")) {
                            FirebaseDatabase.getInstance().getReference("Notifications").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (!temp_auth.equals("")) {
                                        if (dataSnapshot.exists()) {
                                            if ((dataSnapshot.child("notify").getValue().toString()).equals("no")) {
                                                menu.getItem(0).setIcon(ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_notifications_active));
                                            } else if ((dataSnapshot.child("notify").getValue().toString()).equals("yes")) {
                                                menu.getItem(0).setIcon(ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_notifications));
                                            }
                                        } else {
                                            menu.getItem(0).setIcon(ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_notifications));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        } else {
                            menu.getItem(0).setIcon(ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_notifications));
                        }
                    } else {
                        menu.getItem(0).setIcon(ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_notifications));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }else {
            startActivity(new Intent(HomeActivity.this,SignInActivity.class));
            finishAffinity();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.ic_home_signout:

                final AlertDialog.Builder signout_message = new AlertDialog.Builder(HomeActivity.this);
                signout_message.setCancelable(false);
                signout_message.setMessage(Html.fromHtml("Are you sure you want to sign out?"));
                signout_message.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopServices();
                        Toast.makeText(HomeActivity.this,"SignOut Successfully...",Toast.LENGTH_LONG).show();
                        mAuth.getInstance().signOut();
                        startActivity(new Intent(HomeActivity.this,SignInActivity.class));
                        finishAffinity();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                signout_message.create().show();
                return true;

            case R.id.ic_home_notification:
                startActivity(new Intent(HomeActivity.this,NotificationsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void stopServices(){
        stopService(new Intent(this,MyFirebaseInstanceService.class));
    }

    private void progressbar_show()
    {
        progressDialog_home= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

}
