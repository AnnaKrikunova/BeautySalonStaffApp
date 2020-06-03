package com.example.android.beautysalonstaffapp.Interface;

import com.example.android.beautysalonstaffapp.Model.MasterServices;

import java.util.List;

public interface IMasterServicesLoadListener {
    void onMasterServicesLoadSuccess(List<MasterServices> masterServicesList);
    void onMasterServicesLoadFailed(String message);
}
