package com.example.android.beautysalonstaffapp.Interface;

import com.example.android.beautysalonstaffapp.Model.City;

import java.util.List;

public interface IOnAllStateLoadListener {
    void onAllStateLoadSuccess(List<City> cityList);
    void onAllStateLoadFailed(String message);
}
