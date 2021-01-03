package com.example.nectarforseller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaeger.library.StatusBarUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddShopDetailsActivity extends AppCompatActivity implements LocationListener {

    private String uid;

    //permission constants
    private static final int LOCATION_REQUEST_CODE = 100;

    //permission arrays
    private String[] locationPermissions;

    private double latitude = 0.0, longitude=0.0;

    private LocationManager locationManager;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private TextInputLayout textShopNameTI,textPhoneTI,textCountryTI,textStateTI
            ,textCityTI,textAddressTI,textDeleFeeTI;
    private Button addShopBtn;
    private ImageButton addLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shop_details);
        StatusBarUtil.setTransparent(this);

        textShopNameTI = findViewById(R.id.textShopNameTI);
        textPhoneTI = findViewById(R.id.textPhoneTI);
        textCountryTI = findViewById(R.id.textCountryTI);
        textStateTI = findViewById(R.id.textStateTI);
        textCityTI = findViewById(R.id.textCityTI);
        textAddressTI = findViewById(R.id.textAddressTI);
        textDeleFeeTI = findViewById(R.id.textDeleFeeTI);
        addShopBtn = findViewById(R.id.addShopBtn);
        addLocationBtn = findViewById(R.id.addLocationBtn);
        //init permissions array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        addShopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmInput();
            }
        });

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect current location
                if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed, request
                    requestLocationPermission();
                }
            }
        });
    }

    private String  shopName, phoneNumber, deliveryFee, country, state,city,address;

    private void confirmInput() {
        shopName = textShopNameTI.getEditText().getText().toString().trim();
        phoneNumber = textPhoneTI.getEditText().getText().toString().trim();
        deliveryFee = textDeleFeeTI.getEditText().getText().toString().trim();
        country = textCountryTI.getEditText().getText().toString().trim();
        state = textStateTI.getEditText().getText().toString().trim();
        city = textCityTI.getEditText().getText().toString().trim();
        address = textAddressTI.getEditText().getText().toString().trim();

        if (shopName.isEmpty()){
            textShopNameTI.setError("Shop Name can't be empty");
        }
        else {
            textShopNameTI.setError(null);
        }

        if (phoneNumber.isEmpty()){
            textPhoneTI.setError("Phone Number can't be empty");
        }
        else {
            textPhoneTI.setError(null);
        }

        if (country.isEmpty() || state.isEmpty() || city.isEmpty()){
            textCountryTI.setError("Add Location please");
        }
        else {
            textCountryTI.setError(null);
        }

        if (deliveryFee.isEmpty()){
            textDeleFeeTI.setError("Add Delivery Fee");
        }
        else {
            textDeleFeeTI.setError(null);
        }

        if (!shopName.isEmpty() && !phoneNumber.isEmpty() && !country.isEmpty() && !city.isEmpty() && !state.isEmpty()

        && !address.isEmpty() && !deliveryFee.isEmpty()){
            //all right
            updateDatabase();
        }

    }

    private void updateDatabase() {
        progressDialog.setMessage("Adding Shop Info.");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("shopName",""+shopName);
        hashMap.put("phone",""+phoneNumber);
        hashMap.put("deliveryFee",""+deliveryFee);
        hashMap.put("country",""+country);
        hashMap.put("state",""+state);
        hashMap.put("city","" +city);
        hashMap.put("address",""+address);
        hashMap.put("latitude",""+ latitude);
        hashMap.put("longitude",""+ longitude);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        progressDialog.dismiss();
                        Intent intent = new Intent(AddShopDetailsActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(AddShopDetailsActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void detectLocation() {
        Toast.makeText(this, "Please wait..", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if not working cut this .. from
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //end
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    private void findAddress() {
        //find address, country , state , city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set address
            //   countryEt.setText(country);
            textCountryTI.getEditText().setText(country);
            textStateTI.getEditText().setText(state);
            textCityTI.getEditText().setText(city);
            textAddressTI.getEditText().setText(address);


        }catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this, locationPermissions,LOCATION_REQUEST_CODE);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        findAddress();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this,"Please Turn on location.",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission allowed
                        detectLocation();
                    }
                    else {
                        //permission denied
                        Toast.makeText(this,"Location Permission is necessary.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}