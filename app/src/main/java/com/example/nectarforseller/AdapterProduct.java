package com.example.nectarforseller;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        ModelProduct modelProduct = productsList.get(position);

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
