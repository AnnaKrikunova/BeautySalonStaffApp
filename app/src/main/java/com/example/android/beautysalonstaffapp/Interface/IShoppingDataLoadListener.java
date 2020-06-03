package com.example.android.beautysalonstaffapp.Interface;

import com.example.android.beautysalonstaffapp.Model.ShoppingItem;

import java.util.List;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadFailed(String message);
}

