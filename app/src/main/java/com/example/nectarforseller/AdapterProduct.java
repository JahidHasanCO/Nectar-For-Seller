package com.example.nectarforseller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProductSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productsList, filterList;
    private FilterProducts filter;

    public AdapterProduct(Context context, ArrayList<ModelProduct> productsList) {
        this.context = context;
        this.productsList = productsList;
        this.filterList = productsList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        //get data
        final ModelProduct modelProduct = productsList.get(position);

        String id = modelProduct.getProductId();
        String discountNote = modelProduct.getDiscountNote();
        String shopUid = modelProduct.getShopUid();
        String originalPrice = modelProduct.getOriginalPrice();
        String discountPrice = modelProduct.getDiscountPrice();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String productImage = modelProduct.getProductImage();
        String productQuantity= modelProduct.getProductQuantity();
        String productTitle= modelProduct.getProductTitle();
        String timestamp= modelProduct.getTimestamp();

        //set data

        holder.titleTv.setText(productTitle);
        holder.quantityTv.setText(productQuantity);
        holder.discountNoteTv.setText(discountNote);
        holder.discountedPrice.setText("$"+discountPrice);
        holder.originalPrice.setText("$"+originalPrice);
        if (discountAvailable.equals("true")){
            holder.discountedPrice.setVisibility(View.VISIBLE);
            holder.discountNoteTv.setVisibility(View.VISIBLE);
            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


        }else {
            holder.discountedPrice.setVisibility(View.GONE);
            holder.discountNoteTv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(productImage).placeholder(R.drawable.ic_store).into(holder.productIcon);
        } catch (Exception e){
            holder.productIcon.setImageResource(R.drawable.ic_store);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks, show item details

                detailsBottomSheet(modelProduct);
            }
        });

    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        View view = LayoutInflater.from(context).inflate(R.layout.bs_product_details,null);
        bottomSheetDialog.setContentView(view);



        //init views of bottom sheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        TextView productNameTv = view.findViewById(R.id.productNameTv);
        ImageView profileIconTv = view.findViewById(R.id.profileIconTv);
        TextView discountNoteTv = view.findViewById(R.id.discountNoteTv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView categoryTv = view.findViewById(R.id.categoryTv);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        TextView discountedPriceTv = view.findViewById(R.id.discountedPriceTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);

        //get data
        String id = modelProduct.getProductId();
        String discountNote = modelProduct.getDiscountNote();
        String shopUid = modelProduct.getShopUid();
        String originalPrice = modelProduct.getOriginalPrice();
        String discountPrice = modelProduct.getDiscountPrice();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String productImage = modelProduct.getProductImage();
        String productQuantity= modelProduct.getProductQuantity();
        String productTitle= modelProduct.getProductTitle();
        String timestamp= modelProduct.getTimestamp();

        //set data
        titleTv.setText(productTitle);
        descriptionTv.setText(productDescription);
        categoryTv.setText(productCategory);
        quantityTv.setText(productQuantity);
        discountNoteTv.setText(discountNote);
        discountedPriceTv.setText(discountPrice);
        originalPriceTv.setText(originalPrice);
        if (discountAvailable.equals("true")){
            discountedPriceTv.setVisibility(View.VISIBLE);
            discountNoteTv.setVisibility(View.VISIBLE);
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


        }else {
            discountedPriceTv.setVisibility(View.GONE);
            discountNoteTv.setVisibility(View.GONE);
        }

        try {
            Picasso.get().load(productImage).placeholder(R.drawable.ic_store).into(profileIconTv);
        } catch (Exception e){
           profileIconTv.setImageResource(R.drawable.ic_store);
        }
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context,editProductActivity.class);
                intent.putExtra("productId",id);
                context.startActivity(intent);

            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure want to delete product"+productTitle+"?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                deleteProduct(id,productImage);
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void deleteProduct(String id, String productImage) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Products");
        reference.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context,"Product deleted",Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProducts(this,filterList);

        }
        return filter;
    }


    class HolderProductSeller extends RecyclerView.ViewHolder{

        //holder views of recyclerView
        private ImageButton nextBtn;
        private ImageView productIcon;
        private TextView discountNoteTv,titleTv,quantityTv ,discountedPrice,originalPrice;
        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIcon = itemView.findViewById(R.id.productIcon);
            discountNoteTv = itemView.findViewById(R.id.discountNoteTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            discountedPrice = itemView.findViewById(R.id.discountedPrice);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            nextBtn = itemView.findViewById(R.id.nextBtn);

        }
    }
}
