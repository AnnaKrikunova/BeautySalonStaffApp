package com.example.android.beautysalonstaffapp.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.beautysalonstaffapp.Common.Common;
import com.example.android.beautysalonstaffapp.DoneServicesActivity;
import com.example.android.beautysalonstaffapp.Interface.IRecyclerItemSelectedListener;
import com.example.android.beautysalonstaffapp.Model.BookingInformation;
import com.example.android.beautysalonstaffapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder>{
    Context context;
    List<BookingInformation> timeSlotList;
    List<CardView> cardViewList;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(position)).toString());
        if (timeSlotList.size() == 0) {
            holder.card_time_slot.setCardBackgroundColor(context.getResources()
                    .getColor(android.R.color.white));
            holder.txt_time_slot_description.setText("Available");
            holder.txt_time_slot_description.setTextColor(context.getResources()
                    .getColor(android.R.color.black));
            holder.txt_time_slot.setTextColor(context.getResources()
                    .getColor(android.R.color.black));
            holder.setiRecycleItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int position) {

                }
            });
        } else {
            for (BookingInformation slotValue: timeSlotList) {
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == position) {
                    if (!slotValue.isDone()) {
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(context.getResources()
                                .getColor(android.R.color.darker_gray));
                        holder.txt_time_slot_description.setText("Full");
                        holder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        holder.txt_time_slot.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        holder.setiRecycleItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int position) {
                                FirebaseFirestore.getInstance().collection("Salons")
                                        .document(Common.state_name)
                                        .collection("Branch")
                                        .document(Common.selectedSalon.getSalonId())
                                        .collection("Master")
                                        .document(Common.currentMaster.getMasterId())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getSlot().toString())
                                        .get()
                                        .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                            Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                            context.startActivity(new Intent(context, DoneServicesActivity.class));
                                        }
                                    }
                                });

                            }
                        });
                    } else {
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(context.getResources()
                                .getColor(android.R.color.holo_green_dark));
                        holder.txt_time_slot_description.setText("Done");
                        holder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        holder.txt_time_slot.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        holder.setiRecycleItemSelectedListener((view, position12) -> {

                        });
                    }
                } else {
                    if (holder.getiRecycleItemSelectedListener() == null) {
                        holder.setiRecycleItemSelectedListener((view, position1) -> {

                        });
                    }
                }
            }
        }
        if(!cardViewList.contains(holder.card_time_slot))
            cardViewList.add(holder.card_time_slot);
//        holder.setiRecycleItemSelectedListener((view, position1) -> {
//            for (CardView cardView:cardViewList) {
//                if (cardView.getTag() == null) {
//                    cardView.setCardBackgroundColor(context.getResources()
//                            .getColor(android.R.color.white));
//                }
//            }
//            holder.card_time_slot.setCardBackgroundColor(context.getResources()
//                    .getColor(android.R.color.holo_green_dark));
//            Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
//            intent.putExtra(Common.KEY_TIME_SLOT, position);
//            intent.putExtra(Common.KEY_STEP, 3);
//        });
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;
        IRecyclerItemSelectedListener iRecycleItemSelectedListener;

        public void setiRecycleItemSelectedListener(IRecyclerItemSelectedListener iRecycleItemSelectedListener) {
            this.iRecycleItemSelectedListener = iRecycleItemSelectedListener;
        }

        public IRecyclerItemSelectedListener getiRecycleItemSelectedListener() {
            return iRecycleItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = itemView.findViewById(R.id.txt_time_slot_description);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecycleItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}

