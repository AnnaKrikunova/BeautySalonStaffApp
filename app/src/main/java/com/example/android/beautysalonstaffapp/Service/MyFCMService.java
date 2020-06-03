package com.example.android.beautysalonstaffapp.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.android.beautysalonstaffapp.Common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import androidx.annotation.NonNull;

public class MyFCMService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Common.updateToken(this, token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Common.showNotification(this,
                new Random().nextInt(),
                remoteMessage.getData().get(Common.TITLE_KEY),
                remoteMessage.getData().get(Common.CONTENT_KEY),
                null);

    }
}
