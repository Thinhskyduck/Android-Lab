package com.example.lab6_bai3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView lblEventName, lblEventPlace, lblEventDate;
        private SwitchCompat switchEventStatus;
        
        @RequiresApi(api = Build.VERSION_CODES.O)
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            // Set view variables
            lblEventName = itemView.findViewById(R.id.lblEventName);
            lblEventPlace = itemView.findViewById(R.id.lblEventPlace);
            lblEventDate = itemView.findViewById(R.id.lblEventDate);
            switchEventStatus = itemView.findViewById(R.id.switchEventStatus);

            // Update data when click the switch
            switchEventStatus.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                EventAdapter.this.mEvents.get(pos).setStatus(switchEventStatus.isChecked());
                db.updateEvent(EventAdapter.this.mEvents.get(pos));
                notifyItemChanged(pos);
            });
            itemView.setOnLongClickListener(view -> {
                mActivity.setContexItemAdapterPos(getAdapterPosition());
                itemView.showContextMenu();
                return true;
            });
        }
    }

    private boolean HIDE_VIEW_OFF = false;
    private MainActivity mActivity;
    private final Context mContex;
    private final ArrayList<Event> mEvents;
    private DatabaseHandler db;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public EventAdapter(Context mContex, MainActivity activity) {
        this.mContex = mContex;
        this.mActivity = activity;
        db = new DatabaseHandler(mActivity);
        this.mEvents = db.getAllEvents();
    }

    public Event getEvent(int pos){
        return mEvents.get(pos);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addEvent(Event event){
        db.addEvent(event);
        mEvents.add(event);
        notifyItemInserted(getItemCount()-1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void editEvent(Event event){
        for (int i=0; i<= mEvents.size(); i++){
            Event e = mEvents.get(i);
            if (e.getId() == event.getId()){
                db.updateEvent(event);
                e.replace(event);
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void removeEvent(int pos){
        db.deleteEvent(mEvents.get(pos));
        mEvents.remove(pos);
        notifyItemRemoved(pos);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData(){
        db.clearEvent();
        mEvents.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setHideViewOFF(boolean value){
        HIDE_VIEW_OFF = value;
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContex);
        View customView = inflater.inflate(R.layout.event_layout, parent, false);
        return new EventViewHolder(customView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = mEvents.get(position);

        if (HIDE_VIEW_OFF && !event.isStatus()){
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
        else{
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            holder.lblEventName.setText(event.getName());
            holder.lblEventPlace.setText(event.getPlace());
            holder.lblEventDate.setText(event.getDateTimeStringFormat());
            holder.switchEventStatus.setChecked(event.isStatus());
            mActivity.registerForContextMenu(holder.itemView);
        }
    }

    @Override
    public int getItemCount() { return mEvents.size(); }

}
