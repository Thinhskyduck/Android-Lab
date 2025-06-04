package com.example.lab8_thinh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab8_thinh.StudentAdapter;
import com.example.lab8_thinh.Student;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    StudentAdapter studentAdapter;
    List<Student> studentList = new ArrayList<>();
    private BroadcastReceiver networkReceiver;

    // Định nghĩa ActivityResultLauncher để nhận kết quả từ StudentFormActivity
    private final ActivityResultLauncher<Intent> studentFormLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Làm mới danh sách sinh viên khi StudentFormActivity trả về kết quả thành công
                    fetchStudentsFromAPI();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);

        // Set up Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(this, studentList, new StudentAdapter.OnItemClickListener() {
            @Override
            public void onDoubleClick(Student student) {
                Intent intent = new Intent(MainActivity.this, StudentFormActivity.class);
                intent.putExtra("student", student);
                studentFormLauncher.launch(intent); // Sử dụng launcher thay vì startActivity
            }

            @Override
            public void onLongClick(View view, Student student) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenu().add("Edit");
                popupMenu.getMenu().add("Delete");

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Edit")) {
                        Intent intent = new Intent(MainActivity.this, StudentFormActivity.class);
                        intent.putExtra("student", student);
                        studentFormLauncher.launch(intent); // Sử dụng launcher
                    } else if (item.getTitle().equals("Delete")) {
                        showDeleteConfirmDialog(student.id);
                    }
                    return true;
                });

                popupMenu.show();
            }
        });

        recyclerView.setAdapter(studentAdapter);

        // Handle FAB click to add new student
        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StudentFormActivity.class);
            studentFormLauncher.launch(intent); // Sử dụng launcher
        });

        // Check internet and fetch students
        if (isNetworkAvailable()) {
            fetchStudentsFromAPI();
        } else {
            showNoInternetDialog();
        }

        // Register BroadcastReceiver for network changes
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isNetworkAvailable()) {
                    fetchStudentsFromAPI();
                    Toast.makeText(MainActivity.this, "Internet restored, fetching students", Toast.LENGTH_SHORT).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void fetchStudentsFromAPI() {
        OkHttpClient client = new OkHttpClient();
        String url = "http://10.0.2.2/api/get-students.php";

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Connection error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);

                    if (jsonObject.getBoolean("status")) {
                        JSONArray dataArray = jsonObject.getJSONArray("data");
                        studentList.clear();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            Student student = new Student();
                            student.id = obj.getInt("id");
                            student.name = obj.getString("name");
                            student.email = obj.getString("email");
                            student.phone = obj.getString("phone");
                            studentList.add(student);
                        }

                        runOnUiThread(() -> studentAdapter.notifyDataSetChanged());
                    }
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "JSON error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void deleteStudent(int studentId) {
        OkHttpClient client = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(studentId))
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/api/delete-student.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error deleting student", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    fetchStudentsFromAPI();
                });
            }
        });
    }

    private void showDeleteConfirmDialog(int studentId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Delete", (dialog, which) -> deleteStudent(studentId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }
}