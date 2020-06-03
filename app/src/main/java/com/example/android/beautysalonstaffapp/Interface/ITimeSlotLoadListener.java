package com.example.android.beautysalonstaffapp.Interface;

import com.example.android.beautysalonstaffapp.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
