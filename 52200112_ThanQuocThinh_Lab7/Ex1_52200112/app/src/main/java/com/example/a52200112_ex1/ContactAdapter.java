package com.example.a52200112_ex1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context context;
    private List<Contact> contacts;

    public interface OnItemClickListener {
        void onCallClick(String phone);
    }

    private OnItemClickListener listener;

    public ContactAdapter(Context context, List<Contact> contacts, OnItemClickListener listener) {
        this.context = context;
        this.contacts = contacts;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPhone = itemView.findViewById(R.id.txtPhone);

            itemView.setOnClickListener(v -> {
                listener.onCallClick(contacts.get(getAdapterPosition()).phone);
            });
        }
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.txtName.setText(contact.name);
        holder.txtPhone.setText(contact.phone);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void filterList(List<Contact> filteredList) {
        contacts = filteredList;
        notifyDataSetChanged();
    }
}

