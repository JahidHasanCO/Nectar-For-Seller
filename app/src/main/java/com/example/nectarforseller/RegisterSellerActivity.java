package com.example.nectarforseller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaeger.library.StatusBarUtil;

import java.util.HashMap;

public class RegisterSellerActivity extends AppCompatActivity {

    private TextView signInTV;
    private TextInputLayout textNameTI,textEmailTI,textPassTI,textCPassTI;
    private Button signUpBtn;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    //image picked uri
    private Uri image_uri;

    private double latitude = 0.0, longitude=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        StatusBarUtil.setTransparent(this);

        signInTV = findViewById(R.id.signInTV);
        textNameTI = findViewById(R.id.textNameTI);
        textEmailTI = findViewById(R.id.textEmailTI);
        textPassTI = findViewById(R.id.textPassTI);
        textCPassTI = findViewById(R.id.textCPassTI);
        signUpBtn = findViewById(R.id.signUpBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        signInTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterSellerActivity.this,LoginSellerActivity.class));
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput();
            }
        });

    }
    private String name, shopName, phoneNumber, deliveryFee, country, state,city,address,email,password,cPassword;

    private boolean validateName(){
        name = textNameTI.getEditText().getText().toString().trim();
        if (name.isEmpty()){
            textNameTI.setError("Name can't be empty");
            return false;
        }
        else {
            textNameTI.setError(null);
            return true;
        }
    }

    private boolean validateEmail(){
        email = textEmailTI.getEditText().getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            textEmailTI.setError("Email not valid");
            return false;
        }
        else {
            textEmailTI.setError(null);
            return true;
        }
    }
    private boolean validatePassword() {
        password = textPassTI.getEditText().getText().toString().trim();
        cPassword = textCPassTI.getEditText().getText().toString().trim();
        if (password.isEmpty()) {
            textPassTI.setError("Password can't be empty");
            return false;
        } else {
            textPassTI.setError(null);
            if (!password.equals(cPassword)) {
                textCPassTI.setError("Password not match");
                return false;
            } else {
                textCPassTI.setError(null);
                return true;
            }
        }
    }

    private void confirmInput() {
        if (!validateName() | !validateEmail() | !validatePassword()){
            return;
        }
        else {
            createAccount();
        }
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account.");
        progressDialog.show();

        //ceate Account
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Account Created
                        saveFirebaseDatabase();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to create account
                progressDialog.dismiss();
                Toast.makeText(RegisterSellerActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFirebaseDatabase() {
        progressDialog.setMessage("Saving Account Info.");
        final String timestamp = "" + System.currentTimeMillis();


        //setup data to save
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+firebaseAuth.getUid());
        hashMap.put("email",""+ email);
        hashMap.put("name",""+ name);
        hashMap.put("shopName","");
        hashMap.put("phone","");
        hashMap.put("deliveryFee","");
        hashMap.put("country","");
        hashMap.put("state","");
        hashMap.put("city","" );
        hashMap.put("address","" );
        hashMap.put("latitude",""+ latitude);
        hashMap.put("longitude",""+ longitude);
        hashMap.put("timestamp",""+ timestamp);
        hashMap.put("online","true");
        hashMap.put("accountType","Seller");
        hashMap.put("shopOpen","true");
        hashMap.put("profileImage","");

        //save to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //db updated
                        progressDialog.dismiss();
                        Intent intent = new Intent(RegisterSellerActivity.this,UploadAvaterActivity.class);
                        intent.putExtra("uid",firebaseAuth.getUid());
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update db
                        progressDialog.dismiss();
                        startActivity( new Intent(RegisterSellerActivity.this,MainActivity.class));
                        finish();
                    }
                });
    }
}