package com.example.lab8_bai1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddStudent extends AppCompatActivity {

    public static final int REQUEST_UPDATE = 224591;
    public static final int REQUEST_ADD = 249284;
    public static final MediaType JSON = MediaType.parse("multipart/form-data; charset=utf-8");

    private EditText editTextName, editTextEmail, editTextPhone;
    private Button btnSave;

    private int currentRequest;
    private Student currentStudent;
    private AppDb database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);
        initViews();
        loadData();
        initListeners();
        initDb();
    }

    private void initViews(){
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        btnSave = findViewById(R.id.btnSave);
    }

    @SuppressLint("SetTextI18n")
    private void loadData(){
        // Lấy dữ liệu từ MainActivity gửi qua
        Intent intent = getIntent();
        currentRequest = intent.getIntExtra("requestCode", REQUEST_ADD);
        Student s = (Student) intent.getSerializableExtra("student");
        // Nếu dữ liệu không rỗng thì ghi vào currentStudent và hiển thị lên EditText
        if (s != null){
            currentStudent = s;
            editTextName.setText(s.getName());
            editTextEmail.setText(s.getEmail());
            editTextPhone.setText(s.getPhone());
        }
        // Nếu requestCode là REQUEST_UPDATE thì đổi tên nút thành "UPDATE"
        if (currentRequest == REQUEST_UPDATE){
            btnSave.setText("UPDATE");
        }
    }

    private void initListeners(){
        btnSave.setOnClickListener(view -> {
            String name = editTextName.getText().toString();
            String email = editTextEmail.getText().toString();
            String phone = editTextPhone.getText().toString();
            if (validate(name, email, phone)){
                if (currentRequest == REQUEST_ADD) {
                    addStudent(name, email, phone);
                } else {
                    updateStudent(currentStudent.getId(), name, email, phone);
                }
            }
        });
    }

    private void initDb(){
        database = Room.databaseBuilder(getApplicationContext(), AppDb.class, "mydb")
                .allowMainThreadQueries()
                .build();
    }

    private boolean validate(String name, String email, String phone){
        // Kiểm tra dữ liệu tên
        if (name.isEmpty()){
            Toast.makeText(this, "Please enter student's name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (name.length() < 3){
            Toast.makeText(this, "Name must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Kiểm tra dữ liệu email
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter student's email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Kiểm tra dữ liệu phone
        if (phone.isEmpty()){
            Toast.makeText(this, "Please enter student's phone", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!phone.matches("[0-9]+")){
            Toast.makeText(this, "Phone must be a number", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!phone.startsWith("0")){
            Toast.makeText(this, "Phone must start with digit '0'", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (phone.length() < 10 || phone.length() > 11){
            Toast.makeText(this, "Phone must be 10 or 11 digits. Current: " + phone.length(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addStudent(String name, String email, String phone) {
        // Ghi log để kiểm tra dữ liệu
        Log.d("addStudent", name + email + phone);

        // Nếu không có kết nối mạng thì lưu vào database và trở về MainActivity
        if (!InternetBroadcastReceiver.isConnectedNow(this)){
            Student s = new Student(0, name, email, phone);
            database.getStudentDAO().insert(s);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("student", s);
            AddStudent.this.setResult(RESULT_OK, resultIntent);
            Toast.makeText(AddStudent.this, "Add student successfully in offline mode", Toast.LENGTH_SHORT).show();
            AddStudent.this.finish();
            return;
        }

        // Tạo client để gửi request
        OkHttpClient client = new OkHttpClient();
        String createStudentURL = MainActivity.domainApi + "add-student.php";
        // Tạo form body để gửi request
        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("phone", phone)
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
                Log.d("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response)
                    throws IOException {
                try {
                    String responseData = response.body().string();
                    Log.d("onResponse", responseData);
                    JSONObject json = new JSONObject(responseData);
                    boolean status = json.getBoolean("status");
                    int id = json.getInt("data");
                    runOnUiThread(() -> {
                        Intent resultIntent = new Intent();
                        // Kiểm tra nếu status là true thì truyền dữ liệu về MainActivity
                        if (status) {
                            resultIntent.putExtra("student", new Student(id, name, email, phone));
                            AddStudent.this.setResult(RESULT_OK, resultIntent);
                            Toast.makeText(AddStudent.this, "Add student successfully", Toast.LENGTH_SHORT).show();
                        }
                        // Ngược lại truyền kết quả trả về là RESULT_CANCELED
                        else {
                            AddStudent.this.setResult(RESULT_CANCELED, resultIntent);
                            Toast.makeText(AddStudent.this, "Add student failed", Toast.LENGTH_SHORT).show();
                        }
                        AddStudent.this.finish();
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", e.getMessage());
                }
            }
        });
    }

    private void updateStudent(int id, String name, String email, String phone){
        // Ghi log để kiểm tra dữ liệu
        Log.d("updateStudent",  currentStudent + " -> "+ name + email + phone);

        // Nếu không có kết nối mạng thì lưu vào database và trở về MainActivity
        if (!InternetBroadcastReceiver.isConnectedNow(this)){
            currentStudent.update(name, email, phone);
            database.getStudentDAO().update(currentStudent);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("student", currentStudent);
            AddStudent.this.setResult(RESULT_OK, resultIntent);
            Toast.makeText(AddStudent.this, "Update student successfully in offline mode", Toast.LENGTH_SHORT).show();
            AddStudent.this.finish();
            return;
        }

        // Tạo client để gửi request
        OkHttpClient client = new OkHttpClient();
        String updateStudentURL = MainActivity.domainApi + "update-student.php";
        // Tạo form body để gửi request
        RequestBody formBody = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("name", name)
                .add("email", email)
                .add("phone", phone)
                .build();
        // Tạo request lên server
        Request request = new Request.Builder()
                .url(updateStudentURL)
                .post(formBody)
                .build();
        // Gửi request lên server và xử lý kết quả trả về
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response)
                    throws IOException {
                try {
                    String responseData = response.body().string();
                    Log.d("onResponse", responseData);
                    JSONObject json = new JSONObject(responseData);
                    boolean status = json.getBoolean("status");
                    runOnUiThread(() -> {
                        Intent resultIntent = new Intent();
                        // Kiểm tra nếu status là true thì truyền dữ liệu về MainActivity
                        if (status) {
                            currentStudent.update(name, email, phone);
                            resultIntent.putExtra("student", currentStudent);
                            AddStudent.this.setResult(RESULT_OK, resultIntent);
                            Toast.makeText(AddStudent.this, "Update student successfully", Toast.LENGTH_SHORT).show();
                        }
                        // Ngược lại truyền kết quả trả về là RESULT_CANCELED
                        else {
                            AddStudent.this.setResult(RESULT_CANCELED, resultIntent);
                            Toast.makeText(AddStudent.this, "Update student failed", Toast.LENGTH_SHORT).show();
                        }
                        AddStudent.this.finish();
                    });
                } catch (JSONException e) {
                    Log.d("onResponse", e.getMessage());
                }
            }
        });
    }
}