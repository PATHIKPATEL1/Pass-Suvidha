package com.example.passsuvidha;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneratePassActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE =1;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference,databaseReference1;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog_generate_pass;
    private String temp_profession,temp_validity,temp_filename,temp_pass_no;
    private CardView cardview_generate_pass1, cardview_generate_pass2;
    private Student student;
    private PassNo passNo;
    private int temp=1,temp1=1,count=0,pass_temp;
    private PassHolders passHolders;
    private ImageView ic_pass_image,ic_bus_pass_qr_code;
    private LinearLayout ll_institute_name;
    private TextView lbl_pass_title,lbl_name,lbl_date_of_birth,lbl_gender,lbl_mobile_no,lbl_email,lbl_address,
            lbl_institute_name,lbl_pass_type,lbl_pass_no,lbl_from_place,lbl_to_place,lbl_pass_amount,lbl_start_date,lbl_end_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_pass);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar =  findViewById(R.id.generate_pass_toolbar);
        toolbar.setTitle("Pass");
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
        checkPermission();

    }


    private void findElement() {
        cardview_generate_pass1 = (CardView) findViewById(R.id.cardView_generate_pass1);
        cardview_generate_pass2 = (CardView) findViewById(R.id.cardView_generate_pass2);
        ll_institute_name=findViewById(R.id.ll_institute_name);
        ic_pass_image=findViewById(R.id.ic_pass_image);
        lbl_pass_title=findViewById(R.id.lbl_pass_title);
        lbl_name=findViewById(R.id.lbl_name);
        lbl_date_of_birth=findViewById(R.id.lbl_date_of_birth);
        lbl_gender=findViewById(R.id.lbl_gender);
        lbl_mobile_no=findViewById(R.id.lbl_mobile_no);
        lbl_email=findViewById(R.id.lbl_email);
        lbl_address=findViewById(R.id.lbl_address);
        lbl_institute_name=findViewById(R.id.lbl_institute_name);
        lbl_pass_type=findViewById(R.id.lbl_pass_type);
        lbl_pass_no=findViewById(R.id.lbl_pass_no);
        lbl_start_date=findViewById(R.id.lbl_start_date);
        lbl_end_date=findViewById(R.id.lbl_end_date);
        lbl_from_place=findViewById(R.id.lbl_from_place);
        lbl_to_place=findViewById(R.id.lbl_to_place);
        lbl_pass_amount=findViewById(R.id.lbl_pass_amount);
        ic_bus_pass_qr_code=findViewById(R.id.ic_bus_pass_qr_code);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.generate_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.ic_show_pdf:
                startActivity(new Intent(GeneratePassActivity.this,ShowPdfActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDetails() {
        progressbar_show();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                student=new Student();
                student=dataSnapshot.getValue(Student.class);

                lbl_name.setText(student.getFullname());
                lbl_date_of_birth.setText(student.getDate_of_birth());
                lbl_gender.setText(student.getGender());
                lbl_mobile_no.setText(student.getMobileno());
                lbl_email.setText(user.getEmail());
                lbl_address.setText(student.getAddress());

                if(student.getProfession().equals("Student")){
                    lbl_institute_name.setText(student.getInstitute_name());
                    temp_profession="Student";
                    databaseReference1 = FirebaseDatabase.getInstance().getReference("StudentPass").child(user.getUid());
                }else {
                    ll_institute_name.setVisibility(View.GONE);
                    temp_profession="Passenger";
                    databaseReference1 = FirebaseDatabase.getInstance().getReference("EmployeePass").child(user.getUid());
                }

                databaseReference1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (temp == 1) {
                            passHolders = new PassHolders();
                            passHolders = dataSnapshot.getValue(PassHolders.class);

                            if (passHolders.getPass_validity().equals("1 Month")) {
                                temp_validity = "One";
                            } else {
                                temp_validity = "Three";
                            }

                            lbl_pass_title.setText(temp_validity + " Month " + temp_profession + " Concession Pass");
                            lbl_pass_type.setText(passHolders.getPass_type());
                            temp_pass_no=passHolders.getPass_no();
                            lbl_pass_no.setText(temp_pass_no);
                            lbl_start_date.setText(passHolders.getStart_date());
                            lbl_end_date.setText(passHolders.getEnd_date());
                            lbl_from_place.setText(passHolders.getSource());
                            lbl_to_place.setText(passHolders.getDestination());
                            lbl_pass_amount.setText(passHolders.getPass_amount());
                            setProfile();
                            temp=0;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(GeneratePassActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                        progressDialog_generate_pass.dismiss();
                        onBackPressed();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GeneratePassActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                progressDialog_generate_pass.dismiss();
                onBackPressed();
            }
        });
    }

    private void setProfile(){
        final File localFile =new File(this.getCacheDir(),user.getUid()+".jpg");
        StorageReference reference=mStorageRef.child("Profile_Images/"+user.getUid());
        reference.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap= BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        ic_pass_image.setImageBitmap(bitmap);
                        //layoutToImage();
                        getFileName();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(GeneratePassActivity.this,"Can not download profile image",Toast.LENGTH_LONG).show();
                progressDialog_generate_pass.dismiss();
                onBackPressed();
            }
        });
    }


    public void layoutToImage() {
        cardview_generate_pass1.setDrawingCacheEnabled(true);
        cardview_generate_pass2.setDrawingCacheEnabled(true);

        cardview_generate_pass1.buildDrawingCache();
        cardview_generate_pass2.buildDrawingCache();

        Bitmap bm = cardview_generate_pass1.getDrawingCache();
        Bitmap bm1 = cardview_generate_pass2.getDrawingCache();

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteArrayOutputStream bytes1 = new ByteArrayOutputStream();

        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        bm1.compress(Bitmap.CompressFormat.JPEG, 100, bytes1);

        final File localFile =new File(this.getCacheDir(),"pass.jpg");
        final File localFile1 =new File(this.getCacheDir(),"pass1.jpg");

        try {
            FileOutputStream fo = new FileOutputStream(localFile);
            FileOutputStream fo1 = new FileOutputStream(localFile1);

            fo.write(bytes.toByteArray());
            fo1.write(bytes1.toByteArray());
            imageToPDF();
        } catch (IOException e) {
            Toast.makeText(this, "failed to convert image :"+e.getMessage(), Toast.LENGTH_LONG).show();
            progressDialog_generate_pass.dismiss();
            onBackPressed();
            }
        }

    public void imageToPDF() {
        try {
            Document document = new Document();
            temp_filename= "pass_"+count+".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(this.getCacheDir() +"/"+temp_filename));
            document.open();

            final File localFile =new File(this.getCacheDir(),"pass.jpg");
            final File localFile1 =new File(this.getCacheDir(),"pass1.jpg");

            Image img = Image.getInstance(localFile.getAbsolutePath());
            Image img1 = Image.getInstance(localFile1.getAbsolutePath());

            float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / img.getWidth()) * 100;

            float scaler1 = ((document.getPageSize().getWidth() - document.leftMargin()
                    - document.rightMargin() - 0) / img1.getWidth()) * 100;

            img.scalePercent(scaler);
            img1.scalePercent(scaler1);

            img.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
            img1.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);

            document.add(img);
            document.add(img1);

            document.close();
            saveToStorage();
            } catch (Exception e) {
                Toast.makeText(this, "failed to convert PDF :"+e.getMessage(), Toast.LENGTH_LONG).show();
                progressDialog_generate_pass.dismiss();
                onBackPressed();
            }
        }

    private void buildQRCode()
    {
        String qr_code_string = user.getUid() + "_" + Integer.toString(pass_temp);
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qr_code_string, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ic_bus_pass_qr_code.setImageBitmap(bmp);
            layoutToImage();
        } catch (WriterException e) {
            Toast.makeText(this, "failed to generate QR code", Toast.LENGTH_LONG).show();
            progressDialog_generate_pass.dismiss();
            onBackPressed();
        }
    }

    private void saveToStorage(){
        final File localFile =new File(this.getCacheDir(),temp_filename);
        Uri file = Uri.fromFile(localFile);
        StorageReference reference=mStorageRef.child("Passes/"+user.getUid()+"/"+temp_filename);
        reference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                        FirebaseDatabase.getInstance().getReference("CountPass").child(user.getUid()).child("total_pass").setValue(Integer.toString(count))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        FirebaseDatabase.getInstance().getReference("CountPass").child(user.getUid()).child("pass_"+count).setValue(taskSnapshot.getMetadata().getCreationTimeMillis())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        passNo=new PassNo();
                                                        passNo.setPass_no(Integer.toString(count));
                                                        passNo.setUid(user.getUid());

                                                        FirebaseDatabase.getInstance().getReference("PassNo").child(temp_pass_no).setValue(passNo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    progressDialog_generate_pass.dismiss();
                                                                    Toast.makeText(GeneratePassActivity.this, "Pass is Generated successfully!..", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(GeneratePassActivity.this, "Failed to upload Pass. "+exception.getMessage() , Toast.LENGTH_LONG).show();
                        progressDialog_generate_pass.dismiss();
                        onBackPressed();
                    }
                });
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(GeneratePassActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(GeneratePassActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
            setDetails();
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setDetails();
                }
                return;
            }
        }
    }

    private void getFileName(){

        FirebaseDatabase.getInstance().getReference("CountPass").child(user.getUid())
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(temp1==1){
                    count= Integer.parseInt(dataSnapshot.child("total_pass").getValue().toString());
                       count=count+1;
                       pass_temp=count;
                       temp1=0;
                       buildQRCode();
            }}else {
                if(temp1==1) {
                    temp1=0;
                    count=1;
                    pass_temp=count;
                    buildQRCode();
                }
                }}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GeneratePassActivity.this, "Data is not available", Toast.LENGTH_LONG).show();
                progressDialog_generate_pass.dismiss();
                onBackPressed();
            }
        });
    }


    private void progressbar_show()
    {
        progressDialog_generate_pass=ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }

    @Override
    public void onBackPressed() {
            startActivity(new Intent(GeneratePassActivity.this,HomeActivity.class));
            finishAffinity();
    }
}
