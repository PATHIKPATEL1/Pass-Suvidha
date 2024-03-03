package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationsActivity extends AppCompatActivity {

    private CardView cardView_notification;
    private TextView lbl_notification_message;
    private ProgressDialog progressDialog_notification;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String auth;
    private int re_notify=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar =  findViewById(R.id.notification_toolbar);
        toolbar.setTitle("Notifications");
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

        notification();

    }

    private void findElement() {
        cardView_notification=findViewById(R.id.cardView_notification);
        lbl_notification_message=findViewById(R.id.lbl_notification_message);
    }

    private void notification(){
        progressbar_show();
        databaseReference= FirebaseDatabase.getInstance().getReference("PassRequest").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if(re_notify==1) {
                        auth = dataSnapshot.child("auth").getValue().toString();

                        if (auth.equals("yes")) {
                            lbl_notification_message.setText("Your details are authentic. So you can get the pass");
                            cardView_notification.setVisibility(View.VISIBLE);
                            firebaseDatabase.getReference("Notifications").child(user.getUid()).child("notify").setValue("yes");
                            progressDialog_notification.dismiss();
                            cardView_notification.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(NotificationsActivity.this, PaymentGatewayActivity.class));
                                    finish();
                                }
                            });
                        } else if (auth.equals("no")) {
                            lbl_notification_message.setText("Your details are non-authentic. So you can not get the pass");
                            cardView_notification.setVisibility(View.VISIBLE);
                            firebaseDatabase.getReference("Notifications").child(user.getUid()).child("notify").setValue("yes");
                            progressDialog_notification.dismiss();

                            cardView_notification.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    progressbar_show();
                                    FirebaseDatabase.getInstance().getReference("Notifications").child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseDatabase.getInstance().getReference("PassRequest").child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressDialog_notification.dismiss();
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        } else {

                            progressDialog_notification.dismiss();
                            cardView_notification.setVisibility(View.GONE);
                        }
                        re_notify=0;
                    }
                }else {
                    progressDialog_notification.dismiss();
                    cardView_notification.setVisibility(View.GONE);
                }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NotificationsActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void progressbar_show()
    {
        progressDialog_notification= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}
