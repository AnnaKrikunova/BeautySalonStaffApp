package com.example.android.beautysalonstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

import com.example.android.beautysalonstaffapp.Adapter.MySalonAdapter;
import com.example.android.beautysalonstaffapp.Common.Common;
import com.example.android.beautysalonstaffapp.Common.SpacesItemDecoration;
import com.example.android.beautysalonstaffapp.Interface.IBranchLoadListener;
import com.example.android.beautysalonstaffapp.Interface.IGetMasterListener;
import com.example.android.beautysalonstaffapp.Interface.IOnLoadCountSalon;
import com.example.android.beautysalonstaffapp.Interface.IUserLoginRememberListener;
import com.example.android.beautysalonstaffapp.Model.City;
import com.example.android.beautysalonstaffapp.Model.Master;
import com.example.android.beautysalonstaffapp.Model.Salon;
import com.example.android.beautysalonstaffapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SalonListActivity extends AppCompatActivity implements IOnLoadCountSalon, IBranchLoadListener, IGetMasterListener, IUserLoginRememberListener {

    @BindView(R.id.txt_salon_count)
    TextView txt_salon_count;

    @BindView(R.id.recycler_salon)
    RecyclerView recycler_salon;

    IOnLoadCountSalon iOnLoadCountSalon;
    IBranchLoadListener iBranchLoadListener;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salon_list);

        ButterKnife.bind(this);
        initView();
        init();
        loadSalonBaseOnCity(Common.state_name);

    }

    private void loadSalonBaseOnCity(String name) {
        dialog.show();

        FirebaseFirestore.getInstance().collection("Salons")
                .document(name)
                .collection("Branch")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Salon> salonList = new ArrayList<>();
                        iOnLoadCountSalon.onLoadCountSalonSuccess(task.getResult().size());
                        for (DocumentSnapshot salonSnapshot: task.getResult()) {
                            Salon salon = salonSnapshot.toObject(Salon.class);
                            salon.setSalonId(salonSnapshot.getId());
                            salonList.add(salon);
                        }
                        iBranchLoadListener.onBranchLoadSuccess(salonList);

                    }
                }).addOnFailureListener(e -> iBranchLoadListener.onBranchLoadFailed(e.getMessage()));

    }

    private void initView() {
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false).build();
        iOnLoadCountSalon = this;
        iBranchLoadListener = this;
    }

    @Override
    public void onLoadCountSalonSuccess(int count) {
        txt_salon_count.setText(new StringBuilder("All salon (")
        .append(count)
        .append(")"));
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> salonList) {
        MySalonAdapter adapter = new MySalonAdapter(this, salonList, this, this );
        recycler_salon.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onGetMasterSuccess(Master master) {
        Common.currentMaster = master;
        Paper.book().write(Common.MASTER_KEY, new Gson().toJson(master));
    }

    @Override
    public void onUserLoginSuccess(String user) {
        Paper.init(this);
        Paper.book().write(Common.LOGGED_KEY, user);
        Paper.book().write(Common.STATE_KEY, Common.state_name);
        Paper.book().write(Common.SALON_KEY, new Gson().toJson(Common.selectedSalon));

    }
}
