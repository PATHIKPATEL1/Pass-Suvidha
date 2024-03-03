package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CheckPassActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ProgressDialog progressDialog_check_pass;
    private FirebaseDatabase firebaseDatabase;
    private StorageReference mStorageRef;
    private DatabaseReference databaseReference;
    private EditText tb_check_pass_number;
    private Button btn_check_pass_number,btn_check_pass_qr_code;
    private String pass_number,pass_user_uid,pass_count_no,url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_pass);

        Toolbar toolbar =  findViewById(R.id.check_pass_toolbar);
        toolbar.setTitle("Check Pass");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        tb_check_pass_number=findViewById(R.id.tb_check_pass_number);
        tb_check_pass_number.addTextChangedListener(checkPassTextWatcher);
        btn_check_pass_number=findViewById(R.id.btn_check_pass_number);
        btn_check_pass_qr_code=findViewById(R.id.btn_check_pass_qr_code);

        btn_check_pass_qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CheckPassActivity.this,ScannedBarcodeActivity.class));
            }
        });
    }

    private TextWatcher checkPassTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            pass_number=tb_check_pass_number.getText().toString().trim();
            btn_check_pass_number.setEnabled(!pass_number.isEmpty());
        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    public void checkPass_number(View v){
        progressbar_show();
        databaseReference= FirebaseDatabase.getInstance().getReference("PassNo").child(pass_number);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    pass_user_uid = dataSnapshot.child("uid").getValue().toString();
                    pass_count_no = dataSnapshot.child("pass_no").getValue().toString();

                    getPassPDF();
                }
                else {
                    Toast.makeText(CheckPassActivity.this, "This pass is not exist", Toast.LENGTH_LONG).show();
                    progressDialog_check_pass.dismiss();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CheckPassActivity.this, "Please try again", Toast.LENGTH_LONG).show();
                progressDialog_check_pass.dismiss();
            }
        });
    }

    private void getPassPDF(){
        StorageReference reference=mStorageRef.child("Passes/"+pass_user_uid+"/pass_"+pass_count_no+".pdf");

        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                url = uri.toString();
                openPdf();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CheckPassActivity.this, "Pass is not available", Toast.LENGTH_LONG).show();
                progressDialog_check_pass.dismiss();
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
            Toast.makeText(CheckPassActivity.this,"Application is not available to view PDF",Toast.LENGTH_LONG).show();
        }
        progressDialog_check_pass.dismiss();
    }


    private void progressbar_show()
    {
        progressDialog_check_pass= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}
