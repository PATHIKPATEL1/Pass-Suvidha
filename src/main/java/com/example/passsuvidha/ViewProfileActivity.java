
package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private CircleImageView ic_view_profile_profile_image;
    private ListView listview_view_profile;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog_view_profile;
    private CustomViewProfileAdapter customViewProfileAdapter;
    private ProgressBar progressBar_profile_image_view_profile;
    private String Fullname,Date_Of_Birth,Gender,Email_Id,Mobile_Number,Address,State,Profession,Enrollment_Number,Institute_Name,Institute_City,Work_Address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Toolbar toolbar =  findViewById(R.id.user_view_profile_toolbar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        progressBar_profile_image_view_profile=findViewById(R.id.progressBar_profile_image_view_profile);
        ic_view_profile_profile_image=(CircleImageView) findViewById(R.id.ic_view_profile_profile_image);
        listview_view_profile=findViewById(R.id.listview_view_profile);


        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        setDetails();
    }

    private void setDetails() {

        progressbar_show();
        Intent intent=getIntent();

        Fullname=intent.getStringExtra("Fullname");
        Email_Id=intent.getStringExtra("Email Id");
        Mobile_Number=intent.getStringExtra("Mobile Number");
        Profession=intent.getStringExtra("Profession");

        if(Profession!=null) {

        Date_Of_Birth=intent.getStringExtra("Date Of Birth");
        Gender=intent.getStringExtra("Gender");
        Address=intent.getStringExtra("Address");
        State=intent.getStringExtra("State");

            if (Profession.equals("Student")) {
                Enrollment_Number = intent.getStringExtra("Enrollment Number");
                Institute_Name = intent.getStringExtra("Institute_Name");
                Institute_City = intent.getStringExtra("Institute_City");

                String[] titles = {"Fullname", "Date Of Birth", "Gender", "Email Id", "Mobile Number", "Address", "State", "Profession", "Enrollment Number", "Institute Name", "Institute City"};
                String[] subtitles = {Fullname, Date_Of_Birth, Gender, Email_Id, Mobile_Number, Address, State, Profession, Enrollment_Number, Institute_Name, Institute_City};
                customViewProfileAdapter = new CustomViewProfileAdapter(this, titles, subtitles);
                listview_view_profile.setAdapter(customViewProfileAdapter);
                setProfileImage();
            } else if (Profession.equals("Employee")) {
                Work_Address = intent.getStringExtra("Work Address");

                String[] titles = {"Fullname", "Date Of Birth", "Gender", "Email Id", "Mobile Number", "Address", "State", "Profession", "Work Address"};
                String[] subtitles = {Fullname, Date_Of_Birth, Gender, Email_Id, Mobile_Number, Address, State, Profession, Work_Address};
                customViewProfileAdapter = new CustomViewProfileAdapter(this, titles, subtitles);
                listview_view_profile.setAdapter(customViewProfileAdapter);
                setProfileImage();
            }
        }
        else {
            String[] titles={"Fullname","Email Id","Mobile Number"};
            String[] subtitles={Fullname,Email_Id,Mobile_Number};
            customViewProfileAdapter=new CustomViewProfileAdapter(this,titles,subtitles);
            listview_view_profile.setAdapter(customViewProfileAdapter);
            setProfileImage();
        }
    }


    private void setProfileImage() {
        progressBar_profile_image_view_profile.setVisibility(View.VISIBLE);
        final File temp=new File(this.getCacheDir(),user.getUid()+".jpg");
        if(temp.exists()){
            progressDialog_view_profile.dismiss();
            Bitmap bitmap= BitmapFactory.decodeFile(temp.getAbsolutePath());
            progressBar_profile_image_view_profile.setVisibility(View.GONE);
            ic_view_profile_profile_image.setImageBitmap(bitmap);
        }else {
            final File localFile = new File(this.getCacheDir(),user.getUid()+".jpg");
            StorageReference reference = mStorageRef.child("Profile_Images/"+user.getUid());
            reference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog_view_profile.dismiss();
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            progressBar_profile_image_view_profile.setVisibility(View.GONE);
                            ic_view_profile_profile_image.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressDialog_view_profile.dismiss();
                    Toast.makeText(ViewProfileActivity.this, "Can not download profile image", Toast.LENGTH_LONG).show();
                    progressBar_profile_image_view_profile.setVisibility(View.GONE);
                }
            });
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.ic_update_profile:
                progressbar_show();
                Intent intent=new Intent(ViewProfileActivity.this,RegistrationActivity.class);
                intent.putExtra("Fullname",Fullname);
                intent.putExtra("Email Id",Email_Id);
                intent.putExtra("Mobile Number",Mobile_Number);
                intent.putExtra("Profession", Profession);

                if(Profession!=null) {

                    intent.putExtra("Date Of Birth", Date_Of_Birth);
                    intent.putExtra("Gender", Gender);
                    intent.putExtra("Address", Address);
                    intent.putExtra("State", State);
                    intent.putExtra("login_user","Users");
                    if (Profession.equals("Student")) {
                        intent.putExtra("Enrollment Number", Enrollment_Number);
                        intent.putExtra("Institute Name", Institute_Name);
                        intent.putExtra("Institute City", Institute_City);
                    } else {
                        intent.putExtra("Work Address", Work_Address);
                    }
                }
                else{
                    intent.putExtra("login_user","Conductors");
                }
                progressDialog_view_profile.dismiss();
                startActivity(intent);
                return true;

            case R.id.ic_change_password:
                startActivity(new Intent(ViewProfileActivity.this,ChangePasswordActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void progressbar_show()
    {
        progressDialog_view_profile=ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}
