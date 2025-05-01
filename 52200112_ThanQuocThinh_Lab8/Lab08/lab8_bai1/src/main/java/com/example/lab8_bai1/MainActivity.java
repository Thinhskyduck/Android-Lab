package com.example.lab8_bai1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static String domainApi = "http://192.168.1.3:80/api/";
    private static AlertDialog alertDialog;

    private int selectedPosition = 0;
    private boolean currentConnectivity = false;
    private AppDb database;

    ArrayList<Student> students;
    ArrayAdapter<Student> adapter;
    ListView listView;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        registerForContextMenu(listView);
        initAdapter();
        initListeners();
        initDb();
        loadStudents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetBroadcastReceiver, filter);
        if (!database.isOpen()) initDb();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(internetBroadcastReceiver);
        database.close();
    }

    private void initViews(){
        currentConnectivity = InternetBroadcastReceiver.isConnectedNow(this);
        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.button);
    }

    private void initAdapter(){
        students = new ArrayList<Student>();
        adapter = new ArrayAdapter<Student>(this, android.R.layout.two_line_list_item, android.R.id.text1, students){
            @SuppressLint("ClickableViewAccessibility")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);

                TextView text1, text2;
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);

                text1.setText(students.get(position).name);
                text2.setText(students.get(position).email);

                // Tạo sự kiện double click để sửa thông tin sinh viên
                itemView.setOnClickListener(new DoubleClickListener() {
                    @Override
                    public void onDoubleClick(View v) {
                        createRequestEditStudent(position);
                    }
                });

                // Tạo sự kiện long click để hiện context menu sửa hoặc xóa sinh viên
                itemView.setOnLongClickListener(v -> {
                    openContextMenu(v);
                    return true;
                });

                return itemView;
            }
        };
        listView.setAdapter(adapter);
    }

    private void initListeners(){
        btnAdd.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddStudent.class);
            startActivityForResult(intent, AddStudent.REQUEST_ADD);
        });
    }

    private void initDb(){
        database = Room.databaseBuilder(getApplicationContext(), AppDb.class, "mydb")
                .allowMainThreadQueries()
                .build();
    }

    private void loadStudents() {
        // Kiểm tra kết nối internet, nếu không có thì hoạt động ở chế độ offline, load dữ liệu từ database cục bộ
        if (!currentConnectivity) {
            Toast.makeText(MainActivity.this,
                    "No internet connection. Currently working on offline mode.",
                    Toast.LENGTH_SHORT).show();
            List<Student> listStudents = database.getStudentDAO().getAllStudents();
            students.addAll(listStudents);
            adapter.notifyDataSetChanged();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(domainApi + "get-students.php").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("onFailure", Objects.requireNonNull(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response)
                    throws IOException {
                try {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    Log.d("onResponse", responseData);
                    JSONObject json = new JSONObject(responseData);
                    boolean status = json.getBoolean("status");
                    JSONArray data = json.getJSONArray("data");
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        Student student = new Student(
                                item.getInt("id"),
                                item.getString("name"),
                                item.getString("email"),
                                item.getString("phone")
                        );
                        students.add(student);
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (JSONException e) {
                    Log.d("onResponse", Objects.requireNonNull(e.getMessage()));
                }
            }
        });
    }

    private void createRequestEditStudent(int position){
        selectedPosition = position;
        Intent intent = new Intent(MainActivity.this, AddStudent.class);
        intent.putExtra("student", students.get(position));
        startActivityForResult(intent, AddStudent.REQUEST_UPDATE);
    }

    private void createRequestDeleteStudent(int position){
        selectedPosition = position;

        // Kiểm tra kết nối internet, nếu không có thì xóa sinh viên trong database cục bộ
        if (!currentConnectivity){
            database.getStudentDAO().delete(students.get(position));
            students.remove(position);
            adapter.notifyDataSetChanged();
            return;
        }

        // Tạo client để gửi request
        OkHttpClient client = new OkHttpClient();
        // Tạo form body để gửi request
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(students.get(position).id))
                .build();
        // Tạo request lên server
        Request request = new Request.Builder()
                .url(domainApi + "delete-student.php")
                .post(formBody)
                .build();
        // Gửi request lên server và xử lý kết quả trả về
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("onFailure", Objects.requireNonNull(e.getMessage()));
                Toast.makeText(MainActivity.this, "Delete student failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                try {
                    assert response.body() != null;
                    String responseData = response.body().string();
                    Log.d("onResponse", responseData);
                    JSONObject json = new JSONObject(responseData);
                    boolean status = json.getBoolean("status");
                    runOnUiThread(() -> {
                        if (status) {
                            students.remove(position);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Delete student successfully", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Delete student failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", Objects.requireNonNull(e.getMessage()));
                }
            }
        });
    }

    @Override
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        intent.putExtra("requestCode", requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddStudent.REQUEST_UPDATE && resultCode == RESULT_OK) {
            students.get(selectedPosition).update(
                    (Student) Objects.requireNonNull(data.getSerializableExtra("student"))
            );
            adapter.notifyDataSetChanged();
        }
        else if (requestCode == AddStudent.REQUEST_ADD && resultCode == RESULT_OK) {
            students.add(
                    (Student) Objects.requireNonNull(data.getSerializableExtra("student"))
            );
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        assert info != null;
        int position = info.position;
        if (item.getItemId() == R.id.menuEdit){
            createRequestEditStudent(position);
        }
        else if (item.getItemId() == R.id.menuDelete){
            new AlertDialog.Builder(this)
                .setTitle("Delete Student " + position)
                .setMessage("Are you sure you want to delete this student?")
                .setPositiveButton("Yes", (dialogInterface, i) -> createRequestDeleteStudent(position))
                .setNegativeButton("No", null)
                .show();
        }
        return true;
    }

    private void setOfflineMode(){
        students.clear();
        adapter.notifyDataSetChanged();
        if (!database.isOpen()) initDb();
        loadStudents();
    }

    private void saveLocalDataToServer(){
        List<Student> listStudents = database.getStudentDAO().getAllStudents();
        if (!listStudents.isEmpty()){
            // Thông báo có kết nối mạng và đang tải dữ liệu cục bộ server
            Toast.makeText(this, "Network detected. Updating local data to server...", Toast.LENGTH_SHORT).show();
            // Số lượng sinh viên cần gửi lên server, xử lý bất đồng bộ
            AtomicInteger pending = new AtomicInteger(listStudents.size());
            // Tạo client để gửi request
            OkHttpClient client = new OkHttpClient();
            String createStudentURL = MainActivity.domainApi + "add-student.php";
            for (Student student: listStudents) {
                Log.d("addStudentBackground", student.toString());
                // Tạo form body để gửi request
                RequestBody formBody = new FormBody.Builder()
                        .add("name", student.getName())
                        .add("email", student.getEmail())
                        .add("phone", student.getPhone())
                        .build();
                // Tạo request lên server
                Request request = new Request.Builder()
                        .url(createStudentURL)
                        .post(formBody)
                        .build();
                // Gửi request lên server và xử lý kết quả trả về
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d("onFailureBackground", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, final Response response)
                            throws IOException {
                        try {
                            String responseData = response.body().string();
                            Log.d("onResponseBackground", responseData);
                            JSONObject json = new JSONObject(responseData);
                            boolean status = json.getBoolean("status");
                            String message = json.getString("message");
                            runOnUiThread(() -> {
                                if (status) {
                                    database.getStudentDAO().delete(student);
                                } else {
                                    Toast.makeText(MainActivity.this, "Save student " + student.getName() + " to server failed", Toast.LENGTH_SHORT).show();
                                    Log.e("onResponseBackgroundFailed", message);
                                }
                                if (pending.addAndGet(-1) == 0) {
                                    Toast.makeText(MainActivity.this, "Loading data from server", Toast.LENGTH_SHORT).show();
                                    students.clear();
                                    loadStudents();
                                }
                            });
                        } catch (JSONException e) {
                            Log.d("onResponseBackground", e.getMessage());
                        }
                    }
                });
            }
        }
        else {
            students.clear();
            loadStudents();
        }
    }

    private final InternetBroadcastReceiver internetBroadcastReceiver =
        new InternetBroadcastReceiver(connectivity ->  {
            if (connectivity && !currentConnectivity) {
                currentConnectivity = true;
                if (alertDialog != null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle("You are back online")
                        .setMessage("We have detected a change in your network connection status.\n" +
                                "The application will now switch to online mode.\n" +
                                "All previously offline data will be synced to the server.")
                        .setPositiveButton("Got it", (dialogInterface, i) -> saveLocalDataToServer())
                        .create();
                alertDialog.show();
            }
            else if (!connectivity && currentConnectivity){
                currentConnectivity = false;
                if (alertDialog != null && alertDialog.isShowing()){
                    alertDialog.dismiss();
                }
                alertDialog = new AlertDialog.Builder(this)
                    .setTitle("No internet connection")
                    .setMessage("We have detected a change in your network connection status.\n" +
                            "The application will now switch to offline mode.\n" +
                            "All previously online data will be disabled.\n" +
                            "You can add new data in offline mode, and the system will synchronize it once you are back online.")
                    .setPositiveButton("Got it", (dialogInterface, i) -> setOfflineMode())
                    .create();
                alertDialog.show();
            }
            currentConnectivity = connectivity;
        });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuAbout){
            new AlertDialog.Builder(this)
                .setTitle("Chào thầy")
                .setMessage("Em đã tìm ra nguyên nhân làm cho nguyên buổi vừa rồi em truy cập vô API không được :>\n" +
                        "Nguyên nhân là do XAMPP của em set port là 81 chứ không phải port mặc định 80 của HTTP.\n" +
                        "Do đó trong địa chỉ gọi đến API ngoài địa chỉ IP của máy em ra thì em phải thêm vào :81 đằng sau nữa.\n" +
                        "Để gửi request và lấy response từ API trên máy thầy, thầy vui lòng truy cập vào lớp MainActivity của module lab8_bai1 và chỉnh sửa lại địa chỉ request lên API tại biến domainApi ở dòng 49.\n" +
                        "Nếu không nhận, có thể do XAMPP chưa được bật hoặc port bị sai, hoặc do app đang ở chế độ offline.\n" +
                        "Em xin chân thành cảm ơn thầy.")
                .setPositiveButton("Close", null)
                .show();
        }
        return true;
    }
}