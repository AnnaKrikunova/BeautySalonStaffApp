package com.example.android.beautysalonstaffapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beautysalonstaffapp.Common.Common;
import com.example.android.beautysalonstaffapp.Fragments.ShoppingFragment;
import com.example.android.beautysalonstaffapp.Fragments.TotalPriceFragment;
import com.example.android.beautysalonstaffapp.Interface.IBottomSheetDialogOnDismissListener;
import com.example.android.beautysalonstaffapp.Interface.IMasterServicesLoadListener;
import com.example.android.beautysalonstaffapp.Interface.IOnShoppingItemSelected;
import com.example.android.beautysalonstaffapp.Model.CartItem;
import com.example.android.beautysalonstaffapp.Model.MasterServices;
import com.example.android.beautysalonstaffapp.Model.ShoppingItem;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class DoneServicesActivity extends AppCompatActivity implements IMasterServicesLoadListener, IOnShoppingItemSelected, IBottomSheetDialogOnDismissListener {

    private static final int MY_CAMERA_REQUEST_CODE = 1000;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;
    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;
    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;
    @BindView(R.id.chip_group_shopping)
    ChipGroup chip_group_shopping;
    @BindView(R.id.edt_services)
    AppCompatAutoCompleteTextView edt_services;
    @BindView(R.id.img_customer)
    ImageView img_customer;
    @BindView(R.id.add_shopping)
    ImageView add_shopping;
    @BindView(R.id.btn_finish)
    Button btn_finish;

    @BindView(R.id.rdi_no_picture)
    RadioButton rdi_no_picture;

    @BindView(R.id.rdi_picture)
    RadioButton rdi_picture;

    AlertDialog dialog;
    IMasterServicesLoadListener iMasterServicesLoadListener;

    HashSet<MasterServices> serviceAdded = new HashSet<>();
    //List<ShoppingItem> shoppingItems = new ArrayList<>();
    LayoutInflater inflater;

    Uri fileUri;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_services);

        ButterKnife.bind(this);
        init();
        initView();
        setCustomerInformation();
        loadMasterServices();

    }

    private void initView() {
        rdi_picture.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                img_customer.setVisibility(View.VISIBLE);
                btn_finish.setEnabled(false);
            }
        });
        rdi_no_picture.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                img_customer.setVisibility(View.GONE);
                btn_finish.setEnabled(true);
            }
        });
        getSupportActionBar().setTitle("Checkout");
        btn_finish.setOnClickListener(view -> {
            if (rdi_no_picture.isChecked()) {
                dialog.dismiss();
                TotalPriceFragment fragment = TotalPriceFragment.getInstance(DoneServicesActivity.this);
                Bundle bundle = new Bundle();
                bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                //bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "Price");
            } else {
                uploadPicture(fileUri);
            }

        });
        img_customer.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fileUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);

        });
        add_shopping.setOnClickListener(view -> {
            ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServicesActivity.this);
            shoppingFragment.show(getSupportFragmentManager(), "Shopping");
        });


    }

    private void uploadPicture(Uri fileUri) {
        if (fileUri != null) {
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(), fileUri);
            String path = new StringBuilder("Customer_Pictures/")
                    .append(fileName)
                    .toString();
            storageReference = FirebaseStorage.getInstance().getReference(path);
            UploadTask uploadTask = storageReference.putFile(fileUri);

            Task<Uri> task = uploadTask.continueWithTask(task12 -> {
                if (!task12.isSuccessful())
                    Toast.makeText(DoneServicesActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();

                return storageReference.getDownloadUrl();
            }).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    String url = task1.getResult().toString().substring(0, task1.getResult().toString().indexOf("&token"));
                    Log.d("DOWNLOADABLE_LINK", url);
                    dialog.dismiss();

                    TotalPriceFragment fragment = TotalPriceFragment.getInstance(DoneServicesActivity.this);
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                    //bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
                    bundle.putString(Common.IMAGE_DOWNLOADABLE_URL, url);
                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(), "Price");


                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(DoneServicesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });




            
        } else {
            Toast.makeText(this, "Empty image", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MasterStaffApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                return null;
            }
        }

        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath()+File.separator+"IMG_"
        +time_stamp+"_"+new Random().nextInt()+".jpg");
        return mediaFile;
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false)
                .build();

        inflater = LayoutInflater.from(this);
        iMasterServicesLoadListener = this;
    }

    private void loadMasterServices() {
        dialog.show();
        FirebaseFirestore.getInstance()
                .collection("Salons")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonId())
                .collection("Services")
                .get()
                .addOnFailureListener(e -> iMasterServicesLoadListener.onMasterServicesLoadFailed(e.getMessage())).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MasterServices> masterServices = new ArrayList<>();
                        for(DocumentSnapshot masterSnapshot: task.getResult()) {
                            MasterServices services = masterSnapshot.toObject(MasterServices.class);
                            masterServices.add(services);

                        }
                        iMasterServicesLoadListener.onMasterServicesLoadSuccess(masterServices);
                    }
                });
    }

    private void setCustomerInformation() {
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

    }

    @Override
    public void onMasterServicesLoadSuccess(List<MasterServices> masterServicesList) {
        List<String> nameServices = new ArrayList<>();
        Collections.sort(masterServicesList, (masterServices, t1) -> masterServices.getName().compareTo(t1.getName()));

        for(MasterServices masterServices: masterServicesList) {
            nameServices.add(masterServices.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, nameServices);
        edt_services.setThreshold(1);
        edt_services.setAdapter(adapter);
        edt_services.setOnItemClickListener((adapterView, view, i, l) -> {
            int index = nameServices.indexOf(edt_services.getText().toString().trim());
            if (!serviceAdded.contains(masterServicesList.get(index))) {
                serviceAdded.add(masterServicesList.get(index));
                Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                item.setText(edt_services.getText().toString());
                item.setTag(i);
                edt_services.setText("");

                item.setOnCloseIconClickListener(view1 -> {
                    chip_group_services.removeView(view1);
                    serviceAdded.remove((int)item.getTag());


                });

                chip_group_services.addView(item);
            } else {
                edt_services.setText("");
            }

        });
        loadExtraItems();
    }

    @Override
    public void onMasterServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        //shoppingItems.add(shoppingItem);
        //Log.d("ShoppingItem", ""+shoppingItems.size());

        CartItem cartItem = new CartItem();
        cartItem.setProductId(shoppingItem.getId());
        cartItem.setProductImage(shoppingItem.getImage());
        cartItem.setProductName(shoppingItem.getName());
        cartItem.setProductPrice(shoppingItem.getPrice());
        cartItem.setProductQuantity(1);
        cartItem.setUserPhone(Common.currentBookingInformation.getCustomerPhone());


        if (Common.currentBookingInformation.getCartItemList() == null) {
            Common.currentBookingInformation.setCartItemList(new ArrayList<CartItem>());
        }

        boolean flag = false;
        for (int i = 0; i < Common.currentBookingInformation.getCartItemList().size(); i++) {
            if (Common.currentBookingInformation.getCartItemList().get(i).getProductName().equals(shoppingItem.getName())) {
                flag = true;
                CartItem itemUpdate = Common.currentBookingInformation.getCartItemList().get(i);
                itemUpdate.setProductQuantity(itemUpdate.getProductQuantity()+1);
                Common.currentBookingInformation.getCartItemList().set(i, itemUpdate);
            }
        }

        if (!flag) {
            Common.currentBookingInformation.getCartItemList().add(cartItem);
            Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
            item.setText(cartItem.getProductName());
            item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));


            item.setOnCloseIconClickListener(view1 -> {
                chip_group_shopping.removeView(view1);
                Common.currentBookingInformation.getCartItemList().remove((int)item.getTag());


            });
            chip_group_services.addView(item);
        } else {
            chip_group_services.removeAllViews();
            loadExtraItems();
        }



    }

    private void loadExtraItems() {
        if(Common.currentBookingInformation.getCartItemList() != null) {
            for(CartItem cartItem: Common.currentBookingInformation.getCartItemList()) {
                //Common.currentBookingInformation.getCartItemList().add(cartItem);
                Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                item.setText(new StringBuilder(cartItem.getProductName()).append(" x").append(cartItem.getProductQuantity()));
                item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

                item.setOnCloseIconClickListener(view1 -> {
                    chip_group_shopping.removeView(view1);
                    Common.currentBookingInformation.getCartItemList().remove((int)item.getTag());


                });
                chip_group_services.addView(item);
            }
        }
        dialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = null;
                ExifInterface ei = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                    ei = new ExifInterface(getContentResolver().openInputStream(fileUri));

                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotateBitmap = null;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateBitmap = rotateImage(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitmap = rotateImage(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitmap = rotateImage(bitmap, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotateBitmap = bitmap;
                            break;
                    }
                    img_customer.setImageBitmap(rotateBitmap);
                    btn_finish.setEnabled(true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.postRotate(i);
        return Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);

    }

    @Override
    public void onDismissBottomSheetDialog(boolean fromButton) {
        if (fromButton)
            finish();
    }
}
