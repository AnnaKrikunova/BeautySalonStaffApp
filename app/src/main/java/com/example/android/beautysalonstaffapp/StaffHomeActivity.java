package com.example.android.beautysalonstaffapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.beautysalonstaffapp.Adapter.MyTimeSlotAdapter;
import com.example.android.beautysalonstaffapp.Common.Common;
import com.example.android.beautysalonstaffapp.Common.SpacesItemDecoration;
import com.example.android.beautysalonstaffapp.Interface.INotificationCountListener;
import com.example.android.beautysalonstaffapp.Interface.ITimeSlotLoadListener;
import com.example.android.beautysalonstaffapp.Model.BookingInformation;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {

    TextView txt_master_name;

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;

    ActionBarDrawerToggle actionBarDrawerToggle;
    ITimeSlotLoadListener iTimeSlotLoadListener;

    DocumentReference masterDoc;
    AlertDialog alertDialog;

    TextView txt_notification_badge;
    CollectionReference notificationCollection;
    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealtimeListener;

    INotificationCountListener iNotificationCountListener;

    String salonId = "ihvHDE7dnG6EwB4L6Jcd";
    String masterId = "sBTHfOyRD4OPEb4LgEy7";

//    String salonId = Common.currentBookingInformation.getSalonId();
//    String masterId = Common.currentMaster.getMasterId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        ButterKnife.bind(this);
        init();
        initView();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        if (item.getItemId() == R.id.action_new_notification) {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            txt_notification_badge.setText("");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open,
                R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_exit)
                logout();
            return true;
        });


        View headerView = navigationView.getHeaderView(0);
        txt_master_name = headerView.findViewById(R.id.txt_master_name);
        //txt_master_name.setText(Common.currentMaster.getName());
        alertDialog = new SpotsDialog.Builder().setCancelable(false).setContext(this)
                .build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailableTimeSlotOfMaster(masterId,
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfMaster(masterId,
                            simpleDateFormat.format(date.getTime()));
                }
            }
        });

    }

    private void logout() {
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.MASTER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("YES", (dialogInterface, i) -> {
                    Intent intent = new Intent(StaffHomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })

                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void loadAvailableTimeSlotOfMaster(String masterId, String bookDate) {
        alertDialog.show();
        masterDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if(documentSnapshot.exists()) {
                    CollectionReference date = FirebaseFirestore.getInstance()
                            .collection("Salons")
                            .document(Common.state_name)
                            .collection("Branch")
                            .document(salonId)
                            .collection("Master")
                            .document(masterId)
                            .collection(bookDate);
                    date.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            QuerySnapshot querySnapshot = task1.getResult();
                            if (querySnapshot.isEmpty()) {
                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                            } else {
                                List<BookingInformation> timeSlots = new ArrayList<>();
                                for (QueryDocumentSnapshot document: task1.getResult()) {
                                    timeSlots.add(document.toObject(BookingInformation.class));
                                }
                                iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);

                            }

                        }
                    });
                }
            }
        }).addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));
    }

    private void init() {
        iTimeSlotLoadListener = this;
        iNotificationCountListener = this;
        initNotificationRealtimeUpdate();
        initBookingRealtimeUpdate();
    }

    private void initBookingRealtimeUpdate() {
        Common.state_name = "Kyiv";
        masterDoc = FirebaseFirestore.getInstance()
                .collection("Salons")
                .document(Common.state_name)
                .collection("Branch")
                .document(salonId)
                .collection("Master")
                .document(masterId);
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        bookingEvent = (queryDocumentSnapshots, e) -> loadAvailableTimeSlotOfMaster(masterId,
                Common.simpleDateFormat.format(date.getTime()));
        currentBookDateCollection = masterDoc.collection(Common.simpleDateFormat.format(date.getTime()));
        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);

    }

    private void initNotificationRealtimeUpdate() {
        Common.state_name = "Kyiv";
        notificationCollection = FirebaseFirestore.getInstance()
                .collection("Salons")
                .document(Common.state_name)
                .collection("Branch")
                .document(salonId)
                .collection("Master")
                .document(masterId)
                .collection("Notifications");

        notificationEvent = (queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.size() > 0) {
                loadNotification();
            }
        };
        notificationListener = notificationCollection.whereEqualTo("read", false)
        .addSnapshotListener(notificationEvent);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES", (dialogInterface, i) -> Toast.makeText(StaffHomeActivity.this, "Fake function exit", Toast.LENGTH_SHORT).show()).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_new_notification);
        txt_notification_badge = menuItem.getActionView()
                .findViewById(R.id.notification_badge);

        loadNotification();
        menuItem.getActionView().setOnClickListener(view -> onOptionsItemSelected(menuItem));

        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotification() {
        notificationCollection.whereEqualTo("read", false)
                .get()
                .addOnFailureListener(e -> Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                    }
                });
    }

    @Override
    public void onNotificationCountSuccess(int count) {
        if (count == 0)
            txt_notification_badge.setVisibility(View.INVISIBLE);
        else {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if (count <= 9)
                txt_notification_badge.setText(String.valueOf(count));
            else
                txt_notification_badge.setText("9+");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (notificationListener != null)
            notificationListener.remove();
        if (bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null)
            notificationListener.remove();
        if (bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onDestroy();
    }
}
