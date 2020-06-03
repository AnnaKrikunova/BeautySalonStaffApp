package com.example.android.beautysalonstaffapp.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beautysalonstaffapp.Adapter.MyConfirmShoppingItemAdapter;
import com.example.android.beautysalonstaffapp.Common.Common;
import com.example.android.beautysalonstaffapp.Interface.IBottomSheetDialogOnDismissListener;
import com.example.android.beautysalonstaffapp.Model.CartItem;
import com.example.android.beautysalonstaffapp.Model.FCMResponse;
import com.example.android.beautysalonstaffapp.Model.FCMSendData;
import com.example.android.beautysalonstaffapp.Model.Invoice;
import com.example.android.beautysalonstaffapp.Model.MasterServices;
import com.example.android.beautysalonstaffapp.Model.MyToken;
import com.example.android.beautysalonstaffapp.Model.ShoppingItem;
import com.example.android.beautysalonstaffapp.R;
import com.example.android.beautysalonstaffapp.Retrofit.IFCMService;
import com.example.android.beautysalonstaffapp.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TotalPriceFragment extends BottomSheetDialogFragment {
    Unbinder unbinder;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;

    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;

    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.txt_master_name)
    TextView txt_master_name;

    @BindView(R.id.txt_time)
    TextView txt_time;

    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    HashSet<MasterServices> servicesAdded;
    //List<ShoppingItem> shoppingItemList;

    IFCMService ifcmService;
    IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener;

    AlertDialog dialog;

    String image_url;

    private static TotalPriceFragment instance;

    public TotalPriceFragment(IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener) {
        this.iBottomSheetDialogOnDismissListener = iBottomSheetDialogOnDismissListener;
    }

    public static TotalPriceFragment getInstance(IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener) {
        return instance==null?new TotalPriceFragment(iBottomSheetDialogOnDismissListener):instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_total_price, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        init();
        initView();
        getBundle(getArguments());
        setInformation();
        return itemView;
    }

    private void setInformation() {
        txt_salon_name.setText(Common.selectedSalon.getName());
        txt_master_name.setText(Common.currentMaster.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if (servicesAdded.size() > 0) {
            int i = 0;
            for (MasterServices services:servicesAdded) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(view -> {
                    servicesAdded.remove((int)view.getTag());
                    chip_group_services.removeView(view);
                    calculatePrice();
                });
                chip_group_services.addView(chip);
                i++;
            }
        }
        if (Common.currentBookingInformation.getCartItemList() != null) {
            if (Common.currentBookingInformation.getCartItemList().size() > 0) {
                MyConfirmShoppingItemAdapter adapter = new MyConfirmShoppingItemAdapter(getContext(), Common.currentBookingInformation.getCartItemList());
                recycler_view_shopping.setAdapter(adapter);

            }

            calculatePrice();
        }

    }

    private double calculatePrice() {
        double price = Common.DEFAULT_PRICE;
        for(MasterServices services:servicesAdded) {
            price +=services.getPrice();
        }
        if (Common.currentBookingInformation.getCartItemList() != null) {
            for(CartItem cartItem: Common.currentBookingInformation.getCartItemList()) {
                price +=(cartItem.getProductPrice()*cartItem.getProductQuantity());
            }

        }

        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN).append(price));
        return price;
    }

    private void getBundle(Bundle arguments) {
        this.servicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED), new TypeToken<HashSet<MasterServices>>(){}.getType());

//        this.shoppingItemList = new Gson()
//                .fromJson(arguments.getString(Common.SHOPPING_LIST), new TypeToken<List<ShoppingItem>>(){}.getType());

        image_url = arguments.getString(Common.IMAGE_DOWNLOADABLE_URL);

    }

    private void initView() {

        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        btn_confirm.setOnClickListener(view -> {
            dialog.show();

            DocumentReference bookingSet = FirebaseFirestore.getInstance()
                    .collection("Salons")
                    .document(Common.state_name)
                    .collection("Branch")
                    .document(Common.selectedSalon.getSalonId())
                    .collection("Master")
                    .document(Common.currentMaster.getMasterId())
                    .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                    .document(Common.currentBookingInformation.getBookingId());

            bookingSet.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            Map<String,Object> dataUpdate = new HashMap<>();
                            dataUpdate.put("done", true);
                            bookingSet.update(dataUpdate)
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            createInvoice();
                                        }
                                    });

                        }
                    }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

    }

    private void createInvoice() {
        CollectionReference invoiceRef = FirebaseFirestore.getInstance()
                .collection("Salons")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonId())
                .collection("Invoices");

        Invoice invoice = new Invoice();

        invoice.setMasterId(Common.currentMaster.getMasterId());
        invoice.setMasterName(Common.currentMaster.getName());

        invoice.setSalonId(Common.selectedSalon.getSalonId());
        invoice.setSalonAddress(Common.selectedSalon.getAddress());
        invoice.setSalonName(Common.selectedSalon.getName());

        invoice.setCustomerName(Common.currentBookingInformation.getCustomerName());
        invoice.setCustomerPhone(Common.currentBookingInformation.getCustomerPhone());

        invoice.setImageUrl(image_url);

        invoice.setMasterServices(new ArrayList<MasterServices>(servicesAdded));
        invoice.setShoppingItemList(Common.currentBookingInformation.getCartItemList());
        invoice.setFinalPrice(calculatePrice());

        invoiceRef.document()
                .set(invoice)
                .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());
                }
            }
        });

    }

    private void sendNotificationUpdateToUser(String customerPhone) {
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .whereEqualTo("userPhone", customerPhone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().size() > 0) {
                        MyToken myToken = new MyToken();
                        for(DocumentSnapshot tokenSnapShot: task.getResult()) {
                            myToken = tokenSnapShot.toObject(MyToken.class);
                        }
                        FCMSendData fcmSendData = new FCMSendData();
                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("update_done", "true");
                        dataSend.put(Common.RATING_STATE_KEY, Common.state_name);
                        dataSend.put(Common.RATING_SALON_ID, Common.selectedSalon.getSalonId());
                        dataSend.put(Common.RATING_SALON_NAME, Common.selectedSalon.getName());
                        dataSend.put(Common.RATING_MASTER_ID, Common.currentMaster.getMasterId());

                        fcmSendData.setTo(myToken.getToken());
                        fcmSendData.setData(dataSend);

                        ifcmService.sendNotification(fcmSendData)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.newThread())
                                .subscribe(fcmResponse -> {
                                    dialog.dismiss();
                                    dismiss();
                                    iBottomSheetDialogOnDismissListener.onDismissBottomSheetDialog(true);

                                }, throwable -> {
                                    Looper.prepare();
                                    Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                });


                    }
                });

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);

    }
}
