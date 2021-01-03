package com.example.nectarforseller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TextView nameTv,tabProductsTv,tabOrdersTv,filteredProductsTv;
    private ImageButton logoutBtn,addProductBtn,filterProductsBtn;
    private RelativeLayout productsRl,ordersRl;
    private EditText searchProduct;
    private RecyclerView productRv;

    private String SHOP_UID;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabaseRef;
    private ArrayList<ModelProduct> productList;
    private AdapterProduct adapterProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setTransparent(this);

        nameTv = (TextView) findViewById(R.id.nameTv);
        tabProductsTv = (TextView) findViewById(R.id.tabProductsTv);
        tabOrdersTv = (TextView) findViewById(R.id.tabOrdersTv);
        logoutBtn = (ImageButton) findViewById(R.id.logoutBtn);
        addProductBtn = (ImageButton) findViewById(R.id.addProductBtn);
        productsRl = findViewById(R.id.productsRl);
        ordersRl = findViewById(R.id.ordersRl);
        searchProduct = findViewById(R.id.searchProduct);
        filterProductsBtn = findViewById(R.id.filterProductsBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productRv = findViewById(R.id.productRv);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();
        loadAllProducts();
        showProductsUI();

        searchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterProduct.getFilter().filter(s);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeMeOffline();

            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddProductActivity.class));
            }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load products
                showProductsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //load orders
                showOrdersUI();
            }
        });

        filterProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose Category:").setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get selected item
                        String selected = Constants.productCategories1[which];
                        filteredProductsTv.setText(selected);
                        if (selected.equals("All")){
                            loadAllProducts();
                        }
                        else {
                            loadFilteredProducts(selected);
                        }
                    }
                }).show();
            }
        });
    }

    private void loadFilteredProducts(String selected) {
        SHOP_UID = firebaseAuth.getUid();
        productRv.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Products");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                    if (SHOP_UID.equals(postSnapshot.child("shopUid").getValue())){
                        if (selected.equals(postSnapshot.child("productCategory").getValue())){
                            ModelProduct modelProduct = postSnapshot.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }

                    }

                }
                adapterProduct =new AdapterProduct(MainActivity.this, productList);
                productRv.setAdapter(adapterProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllProducts() {
        SHOP_UID = firebaseAuth.getUid();
        productRv.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Products");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot:snapshot.getChildren()){
                    if (SHOP_UID.equals(postSnapshot.child("shopUid").getValue())){
                        ModelProduct modelProduct = postSnapshot.getValue(ModelProduct.class);
                        productList.add(modelProduct);
                    }

                }
                adapterProduct =new AdapterProduct(MainActivity.this, productList);
                productRv.setAdapter(adapterProduct);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showProductsUI() {
        productsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);
        tabProductsTv.setBackgroundResource(R.drawable.tab_shape_rec2);
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));


    }
    private void showOrdersUI() {
        ordersRl.setVisibility(View.VISIBLE);
        productsRl.setVisibility(View.GONE);
        tabOrdersTv.setBackgroundResource(R.drawable.tab_shape_rec2);
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));


    }



    private void makeMeOffline() {
        //after logout in make user offline
        progressDialog.setMessage("Logging out.");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity( new Intent(MainActivity.this,LoginSellerActivity.class));
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            nameTv.setText(name +"("+accountType+")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}