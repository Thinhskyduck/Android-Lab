package com.example.lab6_bai1;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.internal.TextWatcherAdapter;

import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton btnSave;
    private AppCompatTextView tvDisplay;
    private AppCompatEditText txtTextColor, txtBgColor;
    private SharedPreferences myPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        loadData();
        initListeners();
    }

    private void initViews() {
        btnSave = findViewById(R.id.btnSave);
        tvDisplay = findViewById(R.id.tvDisplay);
        txtTextColor = findViewById(R.id.txtTextColor);
        txtBgColor = findViewById(R.id.txtBgColor);
    }

    @SuppressLint("SetTextI18n")
    private void loadData(){
        myPref = getSharedPreferences(getStringRes(R.string.app_pref), MODE_PRIVATE);
        // Number of access
        int noAccess = myPref.getInt(getStringRes(R.string.pref_no_access), 0) + 1;
        tvDisplay.setText(Integer.toString(noAccess));
        // Text Color
        String textColor = myPref.getString(getStringRes(R.string.pref_txt_color), getStringRes(R.color.white));
        txtTextColor.setText(textColor);
        // Background Color
        String bgColor = myPref.getString(getStringRes(R.string.pref_bg_color), getStringRes(R.color.teal_200));
        txtBgColor.setText(bgColor);
        // Load color
        tvDisplay.setTextColor(Color.parseColor(Objects.requireNonNull(txtTextColor.getText()).toString()));
        tvDisplay.setBackgroundColor(Color.parseColor(Objects.requireNonNull(txtBgColor.getText()).toString()));
    }

    private void initListeners(){
        // Nút lưu dữ liệu vào SharedPreferences
        btnSave.setOnClickListener(v -> {
            saveData();
            Toast.makeText(MainActivity.this, "Data has been saved", Toast.LENGTH_SHORT).show();
        });
        // Text Color - Mở hộp chọn màu sắc
        txtTextColor.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            txtTextColor.clearFocus();
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,
                    Color.parseColor(Objects.requireNonNull(txtTextColor.getText()).toString()),
                    new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    txtTextColor.setText("#" + Integer.toHexString(color).substring(2));
                    tvDisplay.setTextColor(color);
                }
                @Override
                public void onCancel(AmbilWarnaDialog dialog) { }
            });
            dialog.show();
        });
        // Background Color - Mở dialog chọn màu sắc
        txtBgColor.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) return;
            txtBgColor.clearFocus();
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,
                    Color.parseColor(Objects.requireNonNull(txtBgColor.getText()).toString()),
                    new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    txtBgColor.setText("#" + Integer.toHexString(color).substring(2));
                    tvDisplay.setBackgroundColor(color);
                }
                @Override
                public void onCancel(AmbilWarnaDialog dialog) { }
            });
            dialog.show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData(){
        SharedPreferences.Editor myPrefEdit = myPref.edit();
        myPrefEdit.putInt(getStringRes(R.string.pref_no_access),
                Integer.parseInt(tvDisplay.getText().toString()));
        myPrefEdit.putString(getStringRes(R.string.pref_txt_color),
                Objects.requireNonNull(txtTextColor.getText()).toString());
        myPrefEdit.putString(getStringRes(R.string.pref_bg_color),
                Objects.requireNonNull(txtBgColor.getText()).toString());
        myPrefEdit.apply();
    }

    private String getStringRes(int resId){
        return getResources().getString(resId);
    }

}