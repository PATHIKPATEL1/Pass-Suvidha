package com.example.passsuvidha;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE =1;
    private CircleImageView ic_reg_profile_img;
    private RadioGroup radiobtn_adduser_profession,radiobtn_adduser_gender;
    private EditText tb_adduser_date_of_birth,tb_adduser_fullname, tb_adduser_email,tb_adduser_mobileno,tb_adduser_address,
            tb_adduser_state,tb_adduser_enrollmentno,tb_adduser_institute_city,tb_adduser_work_address,tb_adduser_password;
    private String adduser_date_of_birth,adduser_fullname,adduser_gender_value="Male",adduser_email,adduser_mobileno,adduser_address,adduser_state,
            adduser_profession_value ="Student",adduser_enrollmentno, adduser_institute_name,adduser_institute_city,adduser_work_address,adduser_password,
            date_of_birth_pattern="([0-2][1-9]|[1-2]0|3[0-1])/(0[1-9]|1[0-2])/(20[0-9][0-9]|19[0-9][0-9]|2100)",profile_image_uri,institute_selected="--Select Institute--",
            update="no",login_user="Users";
    private Button btn_adduser_done;
    private Toolbar toolbar;
    private TextView lbl_adduser_password_mag;
    private ImageView ic_adduser_date_of_birth;
    private ProgressBar progressBar_profile_image_reg,progressBar_adduser_institutename;
    private TextInputLayout TIL_tb_adduser_password, TIL_tb_adduser_institute_city, TIL_tb_adduser_enrollmentno, TIL_tb_adduser_work_address,TIL_tb_adduser_address
            ,TIL_tb_adduser_state;
    private RelativeLayout RL_spinner_adduser_institutename;
    private LinearLayout ll_adduser_date_of_birth,ll_adduser_gender,ll_adduser_profession;
    private Spinner spinner_adduser_institute_name;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private ArrayList array_institute_name,array_institute_city;
    private boolean checkprofile=false,temp_password;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog_signup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        toolbar =  findViewById(R.id.user_registration_toolbar);
        toolbar.setTitle("Registration");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        findElement();

        addTextWatcher();

        setInstitutes();

        dateOfBirth();


        radiobtnUserGender();
        radiobtnUserProfession();

        ic_reg_profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });
        tb_adduser_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    lbl_adduser_password_mag.setVisibility(View.VISIBLE);
                }else {
                    lbl_adduser_password_mag.setVisibility(View.GONE);
                }
            }
        });

        if(user!=null){
            update="yes";
            toolbar.setTitle("Update Profile");
            updateDetails();
        }
    }


    private void setInstitutes() {
        progressBar_adduser_institutename.setVisibility(View.VISIBLE);
        array_institute_name=new ArrayList();
        array_institute_city=new ArrayList();
        databaseReference= FirebaseDatabase.getInstance().getReference("Institutes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                array_institute_name.clear();
                array_institute_city.clear();
                array_institute_name.add("--Select Institute--");
                for(DataSnapshot dataSnapshot_institutes: dataSnapshot.getChildren()){
                    Institute institute=dataSnapshot_institutes.getValue(Institute.class);
                    array_institute_name.add(institute.getInstitute_name());
                    array_institute_city.add(institute.getInstitute_city());
                }
                ArrayAdapter adapter_institutes=new ArrayAdapter(RegistrationActivity.this,android.R.layout.simple_spinner_item,array_institute_name);
                adapter_institutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_adduser_institute_name.setAdapter(adapter_institutes);
                progressBar_adduser_institutename.setVisibility(View.GONE);

                if(!institute_selected.equals("--Select Institute--")){
                    spinner_adduser_institute_name.setSelection(adapter_institutes.getPosition(institute_selected));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar_adduser_institutename.setVisibility(View.GONE);
            }
        });

    }


    private void checkPermission(){

        if (ContextCompat.checkSelfPermission(RegistrationActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(RegistrationActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CropImage.startPickImageActivity(this);
                }
                return;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profile_image_uri=result.getUri().toString();
                ic_reg_profile_img.setImageURI(result.getUri());
                checkprofile=true;
                check_button_enable();
                Toast.makeText(this, "Image Added successfully", Toast.LENGTH_LONG).show();
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Image can't Added successfully" + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private TextWatcher doneTextWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            check_button_enable();
        }
        @Override
        public void afterTextChanged(Editable editable) {}
    };


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(!(adapterView.getItemAtPosition(i).toString()).equals("--Select Institute--")){
                tb_adduser_institute_city.setText(array_institute_city.get(i-1).toString());
                TIL_tb_adduser_institute_city.setVisibility(View.VISIBLE);
            }else{
                TIL_tb_adduser_institute_city.setVisibility(View.GONE);
            }
            check_button_enable();
         }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


    private void check_button_enable(){
        stringValue();
        if(update.equals("yes")){temp_password=true;}
        else {temp_password=(adduser_password.length()>=6);}

        if(login_user.equals("Users")) {
            if (adduser_profession_value.equals("Student")) {
                btn_adduser_done.setEnabled(checkprofile && !adduser_fullname.isEmpty() && (Patterns.EMAIL_ADDRESS.matcher(adduser_email).matches()) && (adduser_mobileno.length() == 10) && !adduser_address.isEmpty() && !adduser_state.isEmpty() && !adduser_enrollmentno.isEmpty()
                        && (adduser_date_of_birth.matches(date_of_birth_pattern)) && !adduser_institute_name.equals("--Select Institute--") && !adduser_institute_city.isEmpty() && temp_password);
            } else {
                btn_adduser_done.setEnabled(checkprofile && !adduser_fullname.isEmpty() && (Patterns.EMAIL_ADDRESS.matcher(adduser_email).matches()) && (adduser_mobileno.length() == 10) && !adduser_address.isEmpty() && !adduser_state.isEmpty()
                        && (adduser_date_of_birth.matches(date_of_birth_pattern)) && !adduser_work_address.isEmpty() && temp_password);
            }
        }
        else {
            btn_adduser_done.setEnabled(checkprofile && !adduser_fullname.isEmpty() && (Patterns.EMAIL_ADDRESS.matcher(adduser_email).matches()) && (adduser_mobileno.length() == 10));
        }
    }


    private void findElement(){
        ic_reg_profile_img = (CircleImageView) findViewById(R.id.ic_reg_profile_img);
        TIL_tb_adduser_enrollmentno =findViewById(R.id.TIL_tb_adduser_enrollmentno);
        TIL_tb_adduser_institute_city =findViewById(R.id.TIL_tb_adduser_institute_city);
        TIL_tb_adduser_work_address =findViewById(R.id.TIL_tb_adduser_work_address);
        progressBar_adduser_institutename=findViewById(R.id.progressBar_adduser_institutename);
        progressBar_profile_image_reg=findViewById(R.id.progressBar_profile_image_reg);
        RL_spinner_adduser_institutename=findViewById(R.id.RL_spinner_adduser_institutename);
        TIL_tb_adduser_password=findViewById(R.id.TIL_tb_adduser_password);
        lbl_adduser_password_mag=findViewById(R.id.lbl_adduser_password_mag);

        ll_adduser_date_of_birth=findViewById(R.id.ll_adduser_date_of_birth);
        ll_adduser_gender=findViewById(R.id.ll_adduser_gender);
        ll_adduser_profession=findViewById(R.id.ll_adduser_profession);
        TIL_tb_adduser_address=findViewById(R.id.TIL_tb_adduser_address);
        TIL_tb_adduser_state=findViewById(R.id.TIL_tb_adduser_state);

        btn_adduser_done=findViewById(R.id.btn_adduser_done);
        tb_adduser_date_of_birth=findViewById(R.id.tb_adduser_date_of_birth);
        ic_adduser_date_of_birth=findViewById(R.id.ic_adduser_date_of_birth);
        tb_adduser_fullname=findViewById(R.id.tb_adduser_fullname);
        radiobtn_adduser_gender=findViewById(R.id.radiobtn_adduser_gender);
        tb_adduser_email=findViewById(R.id.tb_adduser_email);
        tb_adduser_mobileno=findViewById(R.id.tb_adduser_mobileno);
        tb_adduser_address=findViewById(R.id.tb_adduser_address);
        tb_adduser_state=findViewById(R.id.tb_adduser_state);
        radiobtn_adduser_profession=findViewById(R.id.radiobtn_adduser_profession);
        tb_adduser_enrollmentno=findViewById(R.id.tb_adduser_enrollmentno);
        spinner_adduser_institute_name =findViewById(R.id.spinner_adduser_institute_name);
        tb_adduser_institute_city=findViewById(R.id.tb_adduser_institute_city);
        tb_adduser_work_address=findViewById(R.id.tb_adduser_work_address);
        tb_adduser_password=findViewById(R.id.tb_adduser_password);
    }



    private void addTextWatcher(){
        tb_adduser_fullname.addTextChangedListener(doneTextWatcher);
        tb_adduser_date_of_birth.addTextChangedListener(doneTextWatcher);
        tb_adduser_email.addTextChangedListener(doneTextWatcher);
        tb_adduser_mobileno.addTextChangedListener(doneTextWatcher);
        tb_adduser_address.addTextChangedListener(doneTextWatcher);
        tb_adduser_state.addTextChangedListener(doneTextWatcher);
        tb_adduser_enrollmentno.addTextChangedListener(doneTextWatcher);
        tb_adduser_institute_city.addTextChangedListener(doneTextWatcher);
        tb_adduser_work_address.addTextChangedListener(doneTextWatcher);
        tb_adduser_password.addTextChangedListener(doneTextWatcher);

        spinner_adduser_institute_name.setOnItemSelectedListener(this);
    }


    private void dateOfBirth(){
        ic_adduser_date_of_birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal=Calendar.getInstance();
                int year=cal.get(Calendar.YEAR);
                int month=cal.get(Calendar.MONTH);
                int day =cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog=new DatePickerDialog(RegistrationActivity.this,mDateSetListener,year,month,day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                datePickerDialog.show();
            }
        });


        mDateSetListener= new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month +1;
                String temp_month= Integer.toString(month);
                String temp_day= Integer.toString(day);
                if(temp_day.length()==1){
                    temp_day="0"+temp_day;
                }
                if(temp_month.length()==1){
                    temp_month="0"+temp_month;
                }
                adduser_date_of_birth = temp_day + "/" + temp_month + "/" + year;
                tb_adduser_date_of_birth.setText(adduser_date_of_birth);
            }
        };
    }



    private void radiobtnUserGender(){
        radiobtn_adduser_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i)
                {
                    case  R.id.radiobtn_adduser_gender_male:
                        adduser_gender_value="Male";
                        break;

                    case  R.id.radiobtn_adduser_gender_female:
                        adduser_gender_value="Female";
                        break;
                }
            }
        });
    }


    private void radiobtnUserProfession(){
        radiobtn_adduser_profession.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i)
                {
                    case  R.id.radiobtn_adduser_profession_student:
                        TIL_tb_adduser_enrollmentno.setVisibility(View.VISIBLE);
                        RL_spinner_adduser_institutename.setVisibility(View.VISIBLE);
                        TIL_tb_adduser_work_address.setVisibility(View.GONE);
                        adduser_profession_value ="Student";
                        tb_adduser_work_address.setText("");
                        break;

                    case  R.id.radiobtn_adduser_profession_employee:
                        TIL_tb_adduser_enrollmentno.setVisibility(View.GONE);
                        RL_spinner_adduser_institutename.setVisibility(View.GONE);
                        TIL_tb_adduser_institute_city.setVisibility(View.GONE);
                        TIL_tb_adduser_work_address.setVisibility(View.VISIBLE);
                        adduser_profession_value ="Employee";
                        tb_adduser_enrollmentno.setText("");
                        spinner_adduser_institute_name.setSelection(0);
                        tb_adduser_institute_city.setText("");
                        break;
                }
            }
        });
    }


    private int getIndex(Spinner spinner,String myString)
    {
        for (int i=0;i<spinner.getCount();i++){
            if(spinner.getItemAtPosition(i).toString().equals(myString)){
                return i;
            }
        }
        return 0;
    }


    private void stringValue()
    {
        adduser_fullname=tb_adduser_fullname.getText().toString().trim();
        adduser_date_of_birth =tb_adduser_date_of_birth.getText().toString().trim();
        adduser_email=tb_adduser_email.getText().toString().trim();
        adduser_mobileno=tb_adduser_mobileno.getText().toString().trim();
        adduser_address=tb_adduser_address.getText().toString().trim();
        adduser_state=tb_adduser_state.getText().toString().trim();
        adduser_enrollmentno=tb_adduser_enrollmentno.getText().toString().trim();
        adduser_institute_name =String.valueOf(spinner_adduser_institute_name.getSelectedItem()).trim();
        adduser_institute_city=tb_adduser_institute_city.getText().toString().trim();
        adduser_work_address=tb_adduser_work_address.getText().toString().trim();
        adduser_password=tb_adduser_password.getText().toString().trim();
    }


    public void register(View view) {
        progressbar_show();

        Intent intent;
        if(update.equals("yes"))
        {
            intent=new Intent(RegistrationActivity.this,UpdateProfileActivity.class);
            intent.putExtra("login_user",login_user);

        }
        else {
            intent=new Intent(RegistrationActivity.this,AuthenticationActivity.class);
            intent.putExtra("verify_adduser_password",adduser_password);
            intent.putExtra("verify_adduser_email",adduser_email);
        }
        intent.putExtra("verify_profile_image_uri",profile_image_uri);
        intent.putExtra("verify_adduser_fullname",adduser_fullname);
        intent.putExtra("verify_adduser_date_of_birth",adduser_date_of_birth);
        intent.putExtra("verify_adduser_gender_value",adduser_gender_value);
        intent.putExtra("verify_adduser_mobileno",adduser_mobileno);
        intent.putExtra("verify_adduser_address",adduser_address);
        intent.putExtra("verify_adduser_state",adduser_state);
        intent.putExtra("verify_adduser_profession_value", adduser_profession_value);

        if(adduser_profession_value.equals("Student"))
        {
            intent.putExtra("verify_adduser_enrollmentno",adduser_enrollmentno);
            intent.putExtra("verify_adduser_institute_name", adduser_institute_name);
            intent.putExtra("verify_adduser_institute_city", adduser_institute_city);
        }
        else {
            intent.putExtra("verify_adduser_work_address",adduser_work_address);
        }

        progressDialog_signup.dismiss();
        startActivity(intent);

    }

    private void updateDetails() {
        TIL_tb_adduser_password.setVisibility(View.GONE);

        progressbar_show();
        Intent intent=getIntent();

        login_user=intent.getStringExtra("login_user");
        tb_adduser_fullname.setText(intent.getStringExtra("Fullname"));
        tb_adduser_email.setText(intent.getStringExtra("Email Id"));
        tb_adduser_email.setEnabled(false);
        tb_adduser_mobileno.setText(intent.getStringExtra("Mobile Number"));

        if((intent.getStringExtra("Profession"))!=null) {

            tb_adduser_date_of_birth.setText(intent.getStringExtra("Date Of Birth"));
            if ((intent.getStringExtra("Gender")).equals("Female")) {
                radiobtn_adduser_gender.check(R.id.radiobtn_adduser_gender_female);
            }
            tb_adduser_address.setText(intent.getStringExtra("Address"));
            tb_adduser_state.setText(intent.getStringExtra("State"));
            if ((intent.getStringExtra("Profession")).equals("Employee")) {
                radiobtn_adduser_profession.check(R.id.radiobtn_adduser_profession_employee);
            }

            if ((intent.getStringExtra("Profession")).equals("Student")) {
                tb_adduser_enrollmentno.setText(intent.getStringExtra("Enrollment Number"));
                institute_selected = (intent.getStringExtra("Institute Name"));
                tb_adduser_institute_city.setText(intent.getStringExtra("Institute City"));
                setProfileImage();
            } else {
                tb_adduser_work_address.setText(intent.getStringExtra("Work Address"));
                setProfileImage();
            }
        }else {
            ll_adduser_date_of_birth.setVisibility(View.GONE);
            ll_adduser_gender.setVisibility(View.GONE);
            ll_adduser_profession.setVisibility(View.GONE);
            TIL_tb_adduser_address.setVisibility(View.GONE);
            TIL_tb_adduser_state.setVisibility(View.GONE);
            TIL_tb_adduser_enrollmentno.setVisibility(View.GONE);
            RL_spinner_adduser_institutename.setVisibility(View.GONE);
            TIL_tb_adduser_work_address.setVisibility(View.GONE);
            setProfileImage();
        }
    }

    private void setProfileImage() {
        progressBar_profile_image_reg.setVisibility(View.VISIBLE);
        final File temp=new File(this.getCacheDir(),user.getUid()+".jpg");
        if(temp.exists()){
            progressDialog_signup.dismiss();
            Bitmap bitmap= BitmapFactory.decodeFile(temp.getAbsolutePath());
            progressBar_profile_image_reg.setVisibility(View.GONE);
            ic_reg_profile_img.setImageBitmap(bitmap);
            checkprofile = true;
            check_button_enable();
        }else {
            final File localFile = new File(this.getCacheDir(),user.getUid()+".jpg");
            StorageReference reference = mStorageRef.child("Profile_Images/"+user.getUid());
            reference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            progressDialog_signup.dismiss();
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            progressBar_profile_image_reg.setVisibility(View.GONE);
                            ic_reg_profile_img.setImageBitmap(bitmap);
                            checkprofile = true;
                            check_button_enable();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressDialog_signup.dismiss();
                    Toast.makeText(RegistrationActivity.this, "Can not download profile image", Toast.LENGTH_LONG).show();
                    progressBar_profile_image_reg.setVisibility(View.GONE);
                }
            });
        }
    }

    private void progressbar_show()
    {
        progressDialog_signup= ProgressDialog.show(this,"Please wait...","Loading Please wait...",true,false);
    }
}