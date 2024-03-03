package com.example.passsuvidha;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyFirebaseInstanceService extends Service {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private  String auth="";
    private Intent notificationIntent;
    private int temp=1;


    @Override
    public void onCreate() {
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("PassRequest").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (temp == 1) {
                    auth = dataSnapshot.child("auth").getValue().toString();
                    if (auth.equals("yes")) {
                        notification("Your details are authentic. So you can get the pass");
                    } else if (auth.equals("no")) {
                        notification("Your details are non-authentic. So you can not get the pass");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyFirebaseInstanceService.this, "Data is not available", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void notification(String message){
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_launcher)
                        .setContentTitle("Pass Suvidha")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if(auth.equals("yes")) {
            notificationIntent = new Intent(MyFirebaseInstanceService.this, PaymentGatewayActivity.class);
        }else if(auth.equals("no")){
            notificationIntent = new Intent(MyFirebaseInstanceService.this, HomeActivity.class);
        }
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Payment Channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        manager.notify(0, builder.build());

        stopServices();
    }


    private void stopServices(){
        stopService(new Intent(this,MyFirebaseInstanceService.class));
    }

    @Override
    public void onDestroy() {
        temp=0;
        super.onDestroy();
    }
}

