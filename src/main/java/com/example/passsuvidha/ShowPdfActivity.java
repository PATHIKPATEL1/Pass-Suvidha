package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class ShowPdfActivity extends AppCompatActivity {

    private String url,temp_passname;
    private ProgressDialog progressDialog_show_pass;
    private ListView listview_show_pdf;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ArrayList<String> array_pass_time,array_pass_name;
    private StorageReference mStorageRef;
    private CustomShowPdfAdaptor customShowPdfAdaptor;
    private int count=0,temp1=1;
    private SimpleDateFormat simpleDateFormat;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pdf);

        Toolbar toolbar =  findViewById(R.id.show_pass_toolbar);
        toolbar.setTitle("Pass PDF");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        findElement();
        getNoOfPdf();

        listview_show_pdf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getURL(i);
            }
        });
    }

    private void findElement() {

        listview_show_pdf=findViewById(R.id.listview_show_pdf);
    }


    private void getURL(int i){
        progressbar_show();
        StorageReference reference=mStorageRef.child("Passes/"+user.getUid()+"/pass_"+(count-i)+".pdf");

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                url = uri.toString();
                progressDialog_show_pass.dismiss();
                openPdf();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ShowPdfActivity.this, "Pass is not available", Toast.LENGTH_LONG).show();
                progressDialog_show_pass.dismiss();
            }
        });
    }

    private void openPdf(){
        try
        {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url),"application/pdf");
            startActivity(Intent.createChooser(intent,"Choose an Application:"));
        }catch (ActivityNotFoundException e){
            Toast.makeText(ShowPdfActivity.this,"Application is not available to view PDF",Toast.LENGTH_LONG).show();
        }
        progressDialog_show_pass.dismiss();
    }

    private void progressbar_show()
    {
        progressDialog_show_pass=ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

    private void getNoOfPdf(){

        progressbar_show();
        FirebaseDatabase.getInstance().getReference("CountPass").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(temp1==1) {
                                progressDialog_show_pass.dismiss();
                                count = Integer.parseInt(dataSnapshot.child("total_pass").getValue().toString());
                                temp1=0;
                                getPdfTime(dataSnapshot);
                            }
                        }else{
                            temp1=0;
                            progressDialog_show_pass.dismiss();
                            Toast.makeText(ShowPdfActivity.this, "Pass is not available", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ShowPdfActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                        progressDialog_show_pass.dismiss();
                        onBackPressed();
                    }
                });
    }

    private void getPdfTime(DataSnapshot dataSnapshot) {
        array_pass_name = new ArrayList();
        array_pass_time = new ArrayList();

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy  hh:mm:ss a");
        calendar = Calendar.getInstance();


        for (int i = 1; i <= count; i++) {
            temp_passname = "pass_" + i ;
            array_pass_name.add(temp_passname);
            calendar.setTimeInMillis(Long.parseLong(dataSnapshot.child(temp_passname).getValue().toString()));
            array_pass_time.add(simpleDateFormat.format(calendar.getTime()));
        }
        showPdfList();
    }

    private void showPdfList(){

        String str_pass_name[]=new String[array_pass_name.size()];
        String str_pass_time[]=new String[array_pass_time.size()];

        for(int i=0;i<array_pass_name.size();i++){
            str_pass_time[i]=array_pass_time.get(array_pass_time.size()-i-1);
            str_pass_name[i]=array_pass_name.get(array_pass_name.size()-i-1);

        }

        customShowPdfAdaptor=new CustomShowPdfAdaptor(this,str_pass_name,str_pass_time);
        listview_show_pdf.setAdapter(customShowPdfAdaptor);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ShowPdfActivity.this,HomeActivity.class));
        finishAffinity();
    }

}
