
package com.example.passsuvidha;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ApplicationAnimationActivity extends AppCompatActivity {

    private ProgressDialog progressDialog_app_anim;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_animation);

        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        final ImageView animation_app_logo=findViewById(R.id.img_app_logo_animation);

        final Animation AppLogoAnimation= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.app_logo_animation);
        AppLogoAnimation.setFillAfter(false);
        animation_app_logo.setAnimation(AppLogoAnimation);
        AppLogoAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressbar_show();
                if(user!=null){
                    user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                if (user != null) {

                                    DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users");
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            progressDialog_app_anim.dismiss();
                                            Intent intent;
                                            if (dataSnapshot.hasChild(user.getUid())) {
                                                intent=new Intent(ApplicationAnimationActivity.this, HomeActivity.class);
                                            }
                                            else {
                                                intent=new Intent(ApplicationAnimationActivity.this, HomeConductorActivity.class);
                                            }
                                            startActivity(intent);
                                            finish();
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            progressDialog_app_anim.dismiss();
                                            Toast.makeText(ApplicationAnimationActivity.this,"Please try again",Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(e instanceof FirebaseAuthInvalidUserException){
                                progressDialog_app_anim.dismiss();
                                startActivity(new Intent(ApplicationAnimationActivity.this, SignInActivity.class));
                                finish();
                            }
                            else {
                                progressDialog_app_anim.dismiss();
                                final AlertDialog.Builder connection_status_msg = new AlertDialog.Builder(ApplicationAnimationActivity.this);
                                connection_status_msg.setCancelable(false);
                                connection_status_msg.setMessage(Html.fromHtml("Please check your internet connection and try again."));
                                connection_status_msg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                });
                                connection_status_msg.create().show();
                            }}
                    });} else {
                    progressDialog_app_anim.dismiss();
                    startActivity(new Intent(ApplicationAnimationActivity.this, SignInActivity.class));
                    finish();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void progressbar_show()
    {
        progressDialog_app_anim= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}


