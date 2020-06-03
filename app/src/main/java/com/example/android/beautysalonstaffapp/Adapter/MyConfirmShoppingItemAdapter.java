package com.example.android.beautysalonstaffapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.beautysalonstaffapp.Model.CartItem;
import com.example.android.beautysalonstaffapp.Model.ShoppingItem;
import com.example.android.beautysalonstaffapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MyConfirmShoppingItemAdapter extends RecyclerView.Adapter<MyConfirmShoppingItemAdapter.MyViewHolder> {

    Context context;
    List<CartItem> shoppingItemList;

    public MyConfirmShoppingItemAdapter(Context context, List<CartItem> shoppingItems) {
        this.context = context;
        this.shoppingItemList = shoppingItems;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_confirm_shopping, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get()
                .load(shoppingItemList.get(position).getProductImage())
                .into(holder.item_image);
        holder.txt_name.setText(new StringBuilder(shoppingItemList.get(position).getProductName()).append(" x")
                .append(shoppingItemList.get(position).getProductQuantity()));
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_image)
        ImageView item_image;
        @BindView(R.id.txt_name)
        TextView txt_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}
