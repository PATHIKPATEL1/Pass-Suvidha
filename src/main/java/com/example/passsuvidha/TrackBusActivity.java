package com.example.passsuvidha;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackBusActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String bus_no,con_email,bus_lat_long [];
    private SupportMapFragment mapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_bus);


        Toolbar toolbar = findViewById(R.id.track_bus_toolbar);
        toolbar.setTitle("Track Bus");

        Intent intent=getIntent();

        if(intent!=null){
            bus_no=intent.getStringExtra("bus_no");
        }
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        getBusLatLong();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(mMap!=null){
            mMap.clear();
        }
        mMap = googleMap;

        LatLng bus_location = new LatLng(Double.parseDouble(bus_lat_long[0]),Double.parseDouble(bus_lat_long[1]));
        mMap.addMarker(new MarkerOptions().position(bus_location).title(bus_no));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bus_location, 16f), 2000, null);
    }


    private void getBusLatLong(){
        databaseReference= FirebaseDatabase.getInstance().getReference("bus_assign");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(bus_no).getValue()!=null) {
                    con_email = dataSnapshot.child(bus_no).getValue().toString();

                    FirebaseDatabase.getInstance().getReference("Bus_Location").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            bus_lat_long = new String[2];
                            if(dataSnapshot1.child(con_email).getValue()!=null) {
                                bus_lat_long = (dataSnapshot1.child(con_email).getValue().toString()).split(",");
                                mapFragment.getMapAsync(TrackBusActivity.this);
                            }else {dataNotExistError();}
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dataNotExistError();
                        }
                    });
                }else {dataNotExistError();}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dataNotExistError();
            }
        });
    }

    private void dataNotExistError(){
        Toast.makeText(TrackBusActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
        finish();
    }
}
