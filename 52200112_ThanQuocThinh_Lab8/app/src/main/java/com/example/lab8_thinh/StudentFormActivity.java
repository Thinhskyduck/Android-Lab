package com.example.lab8_thinh;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StudentFormActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPhone;
    Button btnSave;
    Student student = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_form);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);

        if (getIntent().hasExtra("student")) {
            student = (Student) getIntent().getSerializableExtra("student");
            if (student != null) {
                edtName.setText(student.name);
                edtEmail.setText(student.email);
                edtPhone.setText(student.phone);
                Log.d("StudentFormActivity", "Editing student ID: " + student.id);
            } else {
                Log.e("StudentFormActivity", "Student object is null");
            }
        }

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Không được bỏ trống trường nào!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (student == null) {
                addStudent(name, email, phone);
            } else {
                updateStudent(student.id, name, email, phone);
            }
        });
    }

    private void addStudent(String name, String email, String phone) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("phone", phone)
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/api/add-student.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(callbackHandler("Thêm thành công"));
    }

    private void updateStudent(int id, String name, String email, String phone) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("name", name)
                .add("email", email)
                .add("phone", phone)
                .build();

        Request request = new Request.Builder()
                .url("http://10.0.2.2/api/update-student.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(callbackHandler("Cập nhật thành công"));
    }

    private Callback callbackHandler(String successMessage) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(StudentFormActivity.this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("StudentFormActivity", "Network error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    Log.d("StudentFormActivity", "API Response: " + responseData);
                    JSONObject res = new JSONObject(responseData);
                    if (res.getBoolean("status")) {
                        runOnUiThread(() -> {
                            Toast.makeText(StudentFormActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // Trả về kết quả thành công
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            String errorMessage = res.optString("message", "Thao tác thất bại");
                            Toast.makeText(StudentFormActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            Log.w("StudentFormActivity", "API Error: " + errorMessage);
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(StudentFormActivity.this, "Lỗi xử lý JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("StudentFormActivity", "JSON error: " + e.getMessage());
                    });
                }
            }
        };
    }
}