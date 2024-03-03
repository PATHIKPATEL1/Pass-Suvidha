package com.example.passsuvidha;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class PaymentGatewayActivity extends AppCompatActivity {

    private EditText tb_pass_payment_amount,tb_pass_payment_card_number,tb_pass_payment_card_MM,tb_pass_payment_card_YY,tb_pass_payment_card_CVV;
    private String pass_payment_amount,pass_payment_card_number,pass_payment_card_MM,pass_payment_card_YY,pass_payment_card_CVV,temp_profession,start_date,end_date;
    private PassHolders passHolders;
    private Button btn_pay_amount;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private ProgressDialog progressDialog_pass_payment;
    private int temp=1;
    private int pass_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_gateway);

        Toolbar toolbar =  findViewById(R.id.payment_gateway_toolbar);
        toolbar.setTitle("Payment");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        firebaseDatabase=FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        findElement();
        addTextWatcher();

        if(user!=null) {
            setDetails();
        }else{
            startActivity(new Intent(PaymentGatewayActivity.this,SignInActivity.class));
            finishAffinity();
        }

        btn_pay_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDetails();
            }
        });
    }

    private void findElement() {

        tb_pass_payment_amount=findViewById(R.id.tb_pass_payment_amount);
        tb_pass_payment_card_number=findViewById(R.id.tb_pass_payment_card_number);
        tb_pass_payment_card_MM=findViewById(R.id.tb_pass_payment_card_MM);
        tb_pass_payment_card_YY=findViewById(R.id.tb_pass_payment_card_YY);
        tb_pass_payment_card_CVV=findViewById(R.id.tb_pass_payment_card_CVV);
        btn_pay_amount=findViewById(R.id.btn_pay_amount);

    }


    private void addTextWatcher() {
        tb_pass_payment_card_number.addTextChangedListener(doneTextWatcher);
        tb_pass_payment_card_MM.addTextChangedListener(doneTextWatcher);
        tb_pass_payment_card_YY.addTextChangedListener(doneTextWatcher);
        tb_pass_payment_card_CVV.addTextChangedListener(doneTextWatcher);
    }

    private TextWatcher doneTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            stringValue();
            btn_pay_amount.setEnabled((pass_payment_card_number.length()==16) && (pass_payment_card_MM.length()==2) && (pass_payment_card_YY.length()==2) && (pass_payment_card_CVV.length()==3));
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };


    private void stringValue() {
        pass_payment_card_number = tb_pass_payment_card_number.getText().toString().trim();
        pass_payment_card_MM = tb_pass_payment_card_MM.getText().toString().trim();
        pass_payment_card_YY = tb_pass_payment_card_YY.getText().toString().trim();
        pass_payment_card_CVV = tb_pass_payment_card_CVV.getText().toString().trim();
    }

    private void setDetails() {
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        start_date = simpleDateFormat.format(calendar.getTime());

        passHolders = new PassHolders();
        Random random = new Random();
        pass_no=random.nextInt(9999);
        Intent intent = getIntent();
        temp_profession = intent.getStringExtra("profession");
        if (temp_profession != null) {

                passHolders.setUID(user.getUid());
                passHolders.setPass_no(Integer.toString(pass_no));
                passHolders.setSource(intent.getStringExtra("Source"));
                passHolders.setDestination(intent.getStringExtra("Destination"));
                passHolders.setPass_type(intent.getStringExtra("pass_type"));
                passHolders.setPass_validity(intent.getStringExtra("pass_validity"));
                passHolders.setTotal_distance(intent.getStringExtra("total_distance"));
                passHolders.setPass_amount(intent.getStringExtra("pass_amount"));
                passHolders.setStart_date(start_date);
                if (intent.getStringExtra("pass_validity").equals("1 Month")) {
                    calendar.add(Calendar.MONTH, 1);
                    calendar.add(Calendar.DAY_OF_WEEK, -1);
                    end_date = simpleDateFormat.format(calendar.getTime());
                } else {
                    calendar.add(Calendar.MONTH, 3);
                    calendar.add(Calendar.DAY_OF_WEEK, -1);
                    end_date = simpleDateFormat.format(calendar.getTime());
                }
                passHolders.setEnd_date(end_date);

                tb_pass_payment_amount.setText(intent.getStringExtra("pass_amount"));

            } else {
            progressbar_show();
            databaseReference= FirebaseDatabase.getInstance().getReference("PassRequest").child(user.getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (!dataSnapshot.child("auth").getValue().toString().equals("")) {
                            passHolders = dataSnapshot.getValue(PassHolders.class);
                            passHolders.setStart_date(start_date);
                            passHolders.setPass_no(Integer.toString(pass_no));

                            if (dataSnapshot.child("pass_validity").getValue().toString().equals("1 Month")) {
                                calendar.add(Calendar.MONTH, 1);
                                calendar.add(Calendar.DAY_OF_WEEK, -1);
                                end_date = simpleDateFormat.format(calendar.getTime());
                            } else {
                                calendar.add(Calendar.MONTH, 3);
                                calendar.add(Calendar.DAY_OF_WEEK, -1);
                                end_date = simpleDateFormat.format(calendar.getTime());
                            }
                            passHolders.setEnd_date(end_date);
                            tb_pass_payment_amount.setText(dataSnapshot.child("pass_amount").getValue().toString());
                            progressDialog_pass_payment.dismiss();
                            checkGender();
                        }
                        else {
                            progressDialog_pass_payment.dismiss();
                            finish();}
                    }else {
                        progressDialog_pass_payment.dismiss();
                        finish();}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PaymentGatewayActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                    progressDialog_pass_payment.dismiss();
                }
            });
        }
        }

        private void checkGender(){
            FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                    if(temp==1) {
                        temp=0;
                        if (dataSnapshot2.child("gender").getValue().toString().equals("Female")) {
                            saveDetails();
                        }
                }}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PaymentGatewayActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                }
            });
        }
    public void saveDetails() {

        progressbar_show();

        if (temp_profession != null) {
            databaseReference = firebaseDatabase.getReference("EmployeePass");
        } else {
            databaseReference = firebaseDatabase.getReference("StudentPass");
        }
        databaseReference.child(user.getUid()).setValue(passHolders).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if(temp_profession==null){
                        FirebaseDatabase.getInstance().getReference("PassRequest").child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseDatabase.getInstance().getReference("Notifications").child(user.getUid()).removeValue();
                            }
                        });
                    }
                    Toast.makeText(PaymentGatewayActivity.this,"Payment is successful",Toast.LENGTH_LONG).show();
                    progressDialog_pass_payment.dismiss();
                    startActivity(new Intent(PaymentGatewayActivity.this,GeneratePassActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(PaymentGatewayActivity.this, "Payment failed. " + task.getException().getMessage() + " Please try again", Toast.LENGTH_LONG).show();
                    progressDialog_pass_payment.dismiss();
                }
            }
        });
    }

            private void progressbar_show()
            {
                progressDialog_pass_payment= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
            }

    @Override
    public void onBackPressed() {
        if (temp_profession != null) {
            finish();
        }else {
            startActivity(new Intent(PaymentGatewayActivity.this,HomeActivity.class));
            finish();
        }
    }
}
