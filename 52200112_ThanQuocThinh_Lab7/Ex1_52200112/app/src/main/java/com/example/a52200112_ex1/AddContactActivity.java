package com.example.a52200112_ex1;

import android.Manifest; // Thêm import
import android.content.ContentProviderOperation;
import android.content.pm.PackageManager; // Thêm import
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull; // Thêm import
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; // Thêm import
import androidx.core.content.ContextCompat; // Thêm import

import java.util.ArrayList;
import android.content.OperationApplicationException;

public class AddContactActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone;
    private Button buttonSave;
    private static final int WRITE_CONTACTS_PERMISSION_CODE = 101; // Mã yêu cầu quyền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> {
            // Kiểm tra quyền trước khi lưu
            checkPermissionAndSaveContact();
        });
    }

    private void checkPermissionAndSaveContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CONTACTS},
                    WRITE_CONTACTS_PERMISSION_CODE);
        } else {
            // Nếu đã có quyền, tiến hành lưu
            saveContact();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_CONTACTS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Người dùng đã cấp quyền, tiến hành lưu
                saveContact();
            } else {
                // Người dùng từ chối cấp quyền
                Toast.makeText(this, "Bạn cần cấp quyền ghi danh bạ để thêm liên hệ", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveContact() {
        String name = editTextName.getText().toString().trim(); // Thêm trim() để loại bỏ khoảng trắng thừa
        String phone = editTextPhone.getText().toString().trim(); // Thêm trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Tạo Raw Contact mới
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null) // Sử dụng tài khoản cục bộ, không đồng bộ
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Thêm tên vào Raw Contact
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0) // Tham chiếu đến Raw Contact vừa tạo (index 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());

        // Thêm số điện thoại vào Raw Contact
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0) // Tham chiếu đến Raw Contact vừa tạo (index 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) // Loại số điện thoại
                .build());

        try {
            // Thực hiện batch operations
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(this, "Thêm liên hệ thành công", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK); // Đặt kết quả thành công
            finish(); // Đóng Activity và quay lại MainActivity
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi thêm liên hệ: " + e.getMessage(), Toast.LENGTH_LONG).show(); // Hiển thị chi tiết lỗi hơn
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi quyền: Không có quyền ghi danh bạ.", Toast.LENGTH_LONG).show(); // Bắt lỗi SecurityException cụ thể
        }
    }
}