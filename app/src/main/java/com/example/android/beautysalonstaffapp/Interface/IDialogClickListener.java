package com.example.android.beautysalonstaffapp.Interface;

import android.content.DialogInterface;

public interface IDialogClickListener {
    void onClickPositiveButton(DialogInterface dialogInterface, String userName, String password);
    void onClickNegativeButton(DialogInterface dialogInterface);

}
