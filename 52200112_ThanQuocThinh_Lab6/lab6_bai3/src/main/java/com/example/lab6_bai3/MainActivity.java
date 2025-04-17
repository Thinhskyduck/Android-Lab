package com.example.lab6_bai3;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_EVENT_HANDLER = 1102;
    public static final int RESULT_CODE_CREATE_EVENT = 1103;
    public static final int RESULT_CODE_EDIT_EVENT = 1104;

    private int contexItemAdapterPos = -1;
    private MenuItem switchStatusMenu;
    private SwitchCompat switchStatus;
    private RecyclerView mRecyclerView;
    private EventAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVariables();
        initData();
    }

    private void initVariables(){
        mRecyclerView = findViewById(R.id.recyclerView);
    }

    @SuppressLint("NewApi")
    private void initData(){
        mAdapter = new EventAdapter(this, this)   ;
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setUpEventsListenerForMenuItems(){
        switchStatus.setOnCheckedChangeListener((compountButton, value) -> {
            mAdapter.setHideViewOFF(!switchStatus.isChecked());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);
        switchStatusMenu = menu.findItem(R.id.switchStatus);
        switchStatus = Objects.requireNonNull(switchStatusMenu.getActionView())
                .findViewById(R.id.switchForActionBar);
        setUpEventsListenerForMenuItems();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.optionAdd){
            Intent intent = new Intent(MainActivity.this, EventHandlerActivity.class);
            intent.putExtra("requestId", RESULT_CODE_CREATE_EVENT);
            startActivityForResult(intent, REQUEST_CODE_EVENT_HANDLER);
        }
        if (item.getItemId() == R.id.optionRemove){
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure want to remove all events?")
                    .setPositiveButton("YES", (dialog, which) -> {
                        mAdapter.clearData();
                    })
                    .setNegativeButton("NO", (dialog, which) -> {})
                    .setCancelable(false)
                    .show();
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EVENT_HANDLER
                && (resultCode == RESULT_CODE_CREATE_EVENT || resultCode == RESULT_CODE_EDIT_EVENT)){
            assert data != null;
            Event event = (Event) data.getSerializableExtra("event");
            if (resultCode == RESULT_CODE_CREATE_EVENT){
                mAdapter.addEvent(event);
            }
            else {
                mAdapter.editEvent(event);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select an action");
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Edit");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (Objects.equals(item.getTitle(), "Delete")){
            mAdapter.removeEvent(contexItemAdapterPos);
        }
        else{
            Event event = mAdapter.getEvent(contexItemAdapterPos);
            Intent intent = new Intent(this, EventHandlerActivity.class);
            intent.putExtra("requestId", RESULT_CODE_EDIT_EVENT);
            intent.putExtra("event", event);
            startActivityForResult(intent, REQUEST_CODE_EVENT_HANDLER);
        }
        return true;
    }

    public int getContexItemAdapterPos() {
        return contexItemAdapterPos;
    }

    public void setContexItemAdapterPos(int value) {
        this.contexItemAdapterPos = value;
    }
}