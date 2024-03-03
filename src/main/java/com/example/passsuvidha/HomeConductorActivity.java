package com.example.passsuvidha;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class HomeConductorActivity extends AppCompatActivity {

    private CircleImageView ic_home_conductor_profile_image;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog_home_conductor;
    private ProgressBar progressBar_profile_image_home_conductor;
    private ImageView ic_view_profile_conductor,ic_check_pass,ic_location_service;
    private TextView lbl_location_service;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private int temp_con,profile_updated=0;
    private String temp_auth;
    private Conductor conductor;
    private static final int REQUEST_LOCATION_PERMISSION = 201;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_conductor);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar =  findViewById(R.id.user_home_conductor_toolbar);
        toolbar.setTitle("Pass Suvidha");
        setSupportActionBar(toolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        progressBar_profile_image_home_conductor=findViewById(R.id.progressBar_profile_image_home_conductor);
        ic_check_pass =findViewById(R.id.ic_check_pass);
        ic_view_profile_conductor=findViewById(R.id.ic_view_profile_conductor);
        ic_location_service=findViewById(R.id.ic_location_service);
        lbl_location_service=findViewById(R.id.lbl_location_service);
        ic_home_conductor_profile_image=(CircleImageView) findViewById(R.id.ic_home_conductor_profile_image);

        Intent intent=getIntent();
        if(intent.getStringExtra("view_details_check")!=null){
            profile_updated=1;
            viewDetails();
        }


        if(user!=null){
            setProfileImage();
        }else {
            startActivity(new Intent(HomeConductorActivity.this,SignInActivity.class));
            finishAffinity();
        }

        checkServiceRunningIcon();

        ic_view_profile_conductor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_updated=1;
                viewDetails();
            }
        });

        ic_check_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeConductorActivity.this,CheckPassActivity.class));
            }
        });

        ic_location_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(HomeConductorActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeConductorActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(HomeConductorActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
                    return;
                }
                else {
                    checkLocationAccuracyStatus();
                    }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(HomeConductorActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();

                        checkLocationAccuracyStatus();
                    }
                }else{
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
    }

    private void checkServiceRunningIcon(){
        if(isMyServiceRunning(PassSuvidhaLocationService.class)) {
            ic_location_service.setImageResource(R.drawable.ic_location_service_on);
            lbl_location_service.setText("Location Service On");
            temp_con=1;
        }
        else {
            ic_location_service.setImageResource(R.drawable.ic_location_service_off);
            lbl_location_service.setText("Location Service Off");
            temp_con=0;
        }
    }

    private void checkLocationAccuracyStatus(){

        LocationRequest mLocationRequest = LocationRequest.create();

        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settingsBuilder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build());


        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                    if(temp_con==0) {
                        ContextCompat.startForegroundService(HomeConductorActivity.this, new Intent(HomeConductorActivity.this, PassSuvidhaLocationService.class));
                        Toast.makeText(HomeConductorActivity.this, "Location service has started", Toast.LENGTH_LONG).show();
                    }
                    else {
                        stopServices();
                        Toast.makeText(HomeConductorActivity.this, "Location service has stoped", Toast.LENGTH_LONG).show();
                    }
                    checkServiceRunningIcon();

                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(HomeConductorActivity.this,101);

                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        });

    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void viewDetails(){
        progressbar_show();
        databaseReference= FirebaseDatabase.getInstance().getReference("Conductors").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (profile_updated == 1) {
                    if (dataSnapshot.exists()) {
                        viewConductorDetails(dataSnapshot);
                    } else {
                        Toast.makeText(HomeConductorActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                        progressDialog_home_conductor.dismiss();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeConductorActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                progressDialog_home_conductor.dismiss();
                finish();
            }
        });
    }

    private void viewConductorDetails(DataSnapshot dataSnapshot) {
        conductor=new Conductor();
        conductor=dataSnapshot.getValue(Conductor.class);

        Intent intent=new Intent(HomeConductorActivity.this,ViewProfileActivity.class);
        intent.putExtra("Fullname",conductor.getFullname());
        intent.putExtra("Email Id",user.getEmail());
        intent.putExtra("Mobile Number",conductor.getMobileno());
        profile_updated=0;
        progressDialog_home_conductor.dismiss();
        startActivity(intent);
    }

    private void setProfileImage() {
        progressBar_profile_image_home_conductor.setVisibility(View.VISIBLE);

        final File localFile =new File(this.getCacheDir(),user.getUid()+".jpg");
        StorageReference reference=mStorageRef.child("Profile_Images/"+user.getUid());
        reference.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        progressBar_profile_image_home_conductor.setVisibility(View.GONE);
                        ic_home_conductor_profile_image.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(HomeConductorActivity.this,"Can not download profile image",Toast.LENGTH_LONG).show();
                progressBar_profile_image_home_conductor.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.home_act_menu_con,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.ic_home_con_signout:

                final AlertDialog.Builder signout_message = new AlertDialog.Builder(HomeConductorActivity.this);
                signout_message.setCancelable(false);
                signout_message.setMessage(Html.fromHtml("Are you sure you want to sign out?"));
                signout_message.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopServices();
                        Toast.makeText(HomeConductorActivity.this,"SignOut Successfully...",Toast.LENGTH_LONG).show();
                        mAuth.getInstance().signOut();
                        startActivity(new Intent(HomeConductorActivity.this,SignInActivity.class));
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
                startActivity(new Intent(HomeConductorActivity.this,NotificationsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void stopServices(){
        stopService(new Intent(this,PassSuvidhaLocationService.class));
    }

    private void progressbar_show()
    {
        progressDialog_home_conductor= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

}
