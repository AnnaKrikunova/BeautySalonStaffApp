package com.example.android.beautysalonstaffapp.Retrofit;

import com.example.android.beautysalonstaffapp.Model.FCMResponse;
import com.example.android.beautysalonstaffapp.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAQ8nHd7k:APA91bFDeCAw5qhGbO7IXoqa6_sw1eBMyPCHANTucHw5IW-UXrlaAkH8LqnUDbA-iXAShZR6UW2in77L-eCdnQTSG9A1mWf5br3Jfmdfk9Tf42u7_nysCr5Glh_yqLGqxZWnbmtWN-WW"
    })
    @POST("fсm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
