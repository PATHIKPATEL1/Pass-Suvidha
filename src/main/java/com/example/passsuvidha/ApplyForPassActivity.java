package com.example.passsuvidha;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ApplyForPassActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText tb_apply_for_pass_source,tb_apply_for_pass_destination,tb_apply_for_pass_source_prompt;
    private TextView lbl_apply_for_pass_distance,lbl_apply_for_pass_pass_amount;
    private ImageView ic_apply_for_pass_edit_source;
    private View promptsView;
    private Spinner spinner_apply_for_pass_pass_type,spinner_apply_for_pass_pass_validity;
    private ProgressDialog progressDialog_apply_for_pass;
    private Button btn_apply_for_pass;
    private PassRequest passRequest;
    private String apply_for_pass_source_prompt,apply_for_pass_destination,temp_type="--Select Pass Type--",temp_validity="--Select Validity--",temp_profession,temp_gender;
    private int apply_for_pass_distance,edit_source=0,charges=0;
    private float rent_per_km_local,rent_per_km_express;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_for_pass);

        Toolbar toolbar = findViewById(R.id.user_apply_for_pass_toolbar);
        toolbar.setTitle("Apply For Pass");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        findElement();

        setDestination();

        enterSource();

        ic_apply_for_pass_edit_source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_source=1;
                enterSource();
            }
        });
    }


    private void setDestination() {

        progressbar_show();
        databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                temp_profession=dataSnapshot.child("profession").getValue().toString();
                if(temp_profession.equals("Student")) {
                    temp_gender=dataSnapshot.child("gender").getValue().toString();
                    passRequest = new PassRequest();
                    passRequest = dataSnapshot.getValue(PassRequest.class);
                    passRequest.setUID(user.getUid());
                    apply_for_pass_destination = dataSnapshot.child("institute_city").getValue().toString();
                }
                else {
                    apply_for_pass_destination = dataSnapshot.child("work_address").getValue().toString();
                }
                tb_apply_for_pass_destination.setText(apply_for_pass_destination);
                progressDialog_apply_for_pass.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ApplyForPassActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                progressDialog_apply_for_pass.dismiss();
                finish();
            }
        });
    }


    private void findElement() {
        tb_apply_for_pass_source=findViewById(R.id.tb_apply_for_pass_source);
        tb_apply_for_pass_destination=findViewById(R.id.tb_apply_for_pass_destination);
        lbl_apply_for_pass_distance=findViewById(R.id.lbl_apply_for_pass_distance);
        lbl_apply_for_pass_pass_amount=findViewById(R.id.lbl_apply_for_pass_pass_amount);
        ic_apply_for_pass_edit_source=findViewById(R.id.ic_apply_for_pass_edit_source);
        btn_apply_for_pass=findViewById(R.id.btn_apply_for_pass);
        spinner_apply_for_pass_pass_type=findViewById(R.id.spinner_apply_for_pass_pass_type);
        spinner_apply_for_pass_pass_validity=findViewById(R.id.spinner_apply_for_pass_pass_validity);

        spinner_apply_for_pass_pass_type.setOnItemSelectedListener(this);
        spinner_apply_for_pass_pass_validity.setOnItemSelectedListener(this);
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
       switch (adapterView.getId()){
           case R.id.spinner_apply_for_pass_pass_type:
               if(!(adapterView.getItemAtPosition(i).toString()).equals("--Select Pass Type--")){
                   temp_type=adapterView.getItemAtPosition(i).toString();
                   passAmount();
               }
               else {
                   lbl_apply_for_pass_pass_amount.setVisibility(View.GONE);
                   btn_apply_for_pass.setEnabled(false);
                   temp_type="--Select Pass Type--";
               }
               break;

           case R.id.spinner_apply_for_pass_pass_validity:
               if(!(adapterView.getItemAtPosition(i).toString()).equals("--Select Validity--")){
                   temp_validity=adapterView.getItemAtPosition(i).toString();
                   passAmount();
               }
               else {
                   lbl_apply_for_pass_pass_amount.setVisibility(View.GONE);
                   btn_apply_for_pass.setEnabled(false);
                   temp_validity="--Select Validity--";
               }
               break;

       }
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


    private void passAmount(){
        databaseReference= FirebaseDatabase.getInstance().getReference("Bus_Rate");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                rent_per_km_local = Float.parseFloat(dataSnapshot.child("local").getValue().toString());
                rent_per_km_express = Float.parseFloat(dataSnapshot.child("express").getValue().toString());

                if(!temp_type.equals("--Select Pass Type--") && !temp_validity.equals("--Select Validity--")) {
                    if (temp_profession.equals("Student")) {
                        if (temp_gender.equals("Male")) {

                            if (temp_validity.equals("1 Month")) {
                                if (temp_type.equals("Local")) {
                                    charges = (int) ((apply_for_pass_distance * rent_per_km_local) * 30 * (0.3));
                                }
                                if (temp_type.equals("Express")) {
                                    charges = (int) ((apply_for_pass_distance * rent_per_km_express) * 30 * (0.3));
                                }
                            }

                            if (temp_validity.equals("3 Month")) {
                                if (temp_type.equals("Local")) {
                                    charges = (int) ((apply_for_pass_distance * rent_per_km_local) * 90 * (0.3));
                                }
                                if (temp_type.equals("Express")) {
                                    charges = (int) ((apply_for_pass_distance * rent_per_km_express) * 90 * (0.3));
                                }
                            }

                        }else {
                            charges=0;
                        }
                    }
                    else {

                        if (temp_validity.equals("1 Month")) {
                            if (temp_type.equals("Local")) {
                                charges = (int) ((apply_for_pass_distance * rent_per_km_local) * 30 * (0.5));
                            }
                            if (temp_type.equals("Express")) {
                                charges = (int) ((apply_for_pass_distance * rent_per_km_express) * 30 * (0.5));
                            }
                        }

                        if (temp_validity.equals("3 Month")) {
                            if (temp_type.equals("Local")) {
                                charges = (int) ((apply_for_pass_distance * rent_per_km_local) * 90 * (0.5));
                            }
                            if (temp_type.equals("Express")) {
                                charges = (int) (((apply_for_pass_distance * rent_per_km_express) * 90 * (0.5)));
                            }
                        }
                    }

                    lbl_apply_for_pass_pass_amount.setText("Pass Amount : " + charges);
                    lbl_apply_for_pass_pass_amount.setVisibility(View.VISIBLE);
                    btn_apply_for_pass.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ApplyForPassActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                progressDialog_apply_for_pass.dismiss();
                finish();
            }
        });

    }


    private void calculateDistance() {
        try {
            LatLng l1 = getLocationFromAddress(getApplicationContext(), apply_for_pass_source_prompt + " Gujarat");
            LatLng l2 = getLocationFromAddress(getApplicationContext(), apply_for_pass_destination + " Gujarat");

            apply_for_pass_distance = CalculationByDistance(l1, l2);

            lbl_apply_for_pass_distance.setText("Total Distance : " + Integer.toString(apply_for_pass_distance));
            lbl_apply_for_pass_distance.setVisibility(View.VISIBLE);
        }catch (Exception e){
            Toast.makeText(ApplyForPassActivity.this,"Please check your internet connection and try again.",Toast.LENGTH_LONG).show();
            finish();
        }
    }



    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }
    public int CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        return kmInDec;
    }


    private void enterSource(){
        promptsView = getLayoutInflater().inflate(R.layout.activity_apply_for_pass_source_dialog_prompt, null);
        tb_apply_for_pass_source_prompt=(EditText) promptsView.findViewById(R.id.tb_apply_for_pass_source_prompt);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ApplyForPassActivity.this);

        alertDialogBuilder.setView(promptsView);

        if(edit_source==0) {
            alertDialogBuilder.setTitle("Enter Source address");
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Done",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }
                    ).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                    onBackPressed();
                }
            });
        }else {
            alertDialogBuilder.setTitle("Change Source address");
            alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Done",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            }
                    ).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.cancel();
                }
            });
        }

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                stringValue();
                if(apply_for_pass_source_prompt.isEmpty()){
                    tb_apply_for_pass_source_prompt.setError("Please Enter Source address");
                }
                else{
                    tb_apply_for_pass_source.setText(apply_for_pass_source_prompt);
                    progressbar_show();
                    calculateDistance();
                    if(edit_source==1) {
                        passAmount();
                        edit_source=0;
                    }
                    progressDialog_apply_for_pass.dismiss();
                    alertDialog.cancel();
                }
            } });
    }


public void applyForPass(View v) {
    if (temp_profession.equals("Student")) {
        progressbar_show();
        passRequest.setPass_type(temp_type);
        passRequest.setPass_validity(temp_validity);
        passRequest.setSource(apply_for_pass_source_prompt);
        passRequest.setDestination(apply_for_pass_destination);
        passRequest.setTotal_distance(Integer.toString(apply_for_pass_distance));
        passRequest.setPass_amount(Integer.toString(charges));
        passRequest.setAuth("");

        databaseReference = firebaseDatabase.getReference("PassRequest");

        databaseReference.child(user.getUid()).setValue(passRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    firebaseDatabase.getReference("Notifications").child(user.getUid()).child("notify").setValue("no").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                callService();
                                Toast.makeText(ApplyForPassActivity.this, "Pass Request is send successfully.", Toast.LENGTH_LONG).show();
                                progressDialog_apply_for_pass.dismiss();
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(ApplyForPassActivity.this, "Pass Request failed. " + task.getException().getMessage() + " Please try again", Toast.LENGTH_LONG).show();
                    progressDialog_apply_for_pass.dismiss();
                }
            }
        });
    }
    else {
        Intent intent=new Intent(ApplyForPassActivity.this,PaymentGatewayActivity.class);
        intent.putExtra("profession",temp_profession);
        intent.putExtra("Source",apply_for_pass_source_prompt);
        intent.putExtra("Destination",apply_for_pass_destination);
        intent.putExtra("pass_type",temp_type);
        intent.putExtra("pass_validity",temp_validity);
        intent.putExtra("total_distance",Integer.toString(apply_for_pass_distance));
        intent.putExtra("pass_amount",Integer.toString(charges));
        startActivity(intent);
    }

}

    private void callService(){
    startService(new Intent(this,MyFirebaseInstanceService.class));
}


    private void stringValue(){
        apply_for_pass_source_prompt=tb_apply_for_pass_source_prompt.getText().toString().trim();
    }


    private void progressbar_show()
    {
        progressDialog_apply_for_pass= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

}
