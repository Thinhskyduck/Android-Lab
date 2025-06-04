package com.example.lab8_thinh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onDoubleClick(Student student);
        void onLongClick(View view, Student student);
    }

    private List<Student> studentList;
    private Context context;
    private OnItemClickListener listener;

    public StudentAdapter(Context context, List<Student> studentList, OnItemClickListener listener) {
        this.context = context;
        this.studentList = studentList;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        long lastClickTime = 0;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);

            itemView.setOnClickListener(v -> {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < 300) {
                    listener.onDoubleClick(studentList.get(getAdapterPosition()));
                }
                lastClickTime = clickTime;
            });

            itemView.setOnLongClickListener(v -> {
                listener.onLongClick(v, studentList.get(getAdapterPosition()));
                return true;
            });
        }
    }

    @NonNull
    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder holder, int position) {
        holder.txtName.setText(studentList.get(position).name);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}



