package com.example.a52200112_ex1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // Thêm import này
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> addContactLauncher;
    private ActivityResultLauncher<String> requestCallPermissionLauncher; // Launcher cho quyền gọi điện

    private RecyclerView recyclerView;
    private SearchView searchView;
    private List<Contact> contactList = new ArrayList<>();
    private ContactAdapter adapter;
    private String pendingPhoneNumberToCall = null; // Lưu số điện thoại đang chờ quyền

    private static final int READ_CONTACTS_PERMISSION_CODE = 100; // Đặt tên hằng số cho rõ ràng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Launcher để nhận kết quả từ AddContactActivity
        addContactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Đợi một chút để hệ thống lưu contact rồi load lại
                        new Handler().postDelayed(this::loadContacts, 500); // Giảm delay nếu muốn
                    }
                }
        );

        // Launcher để yêu cầu quyền CALL_PHONE
        requestCallPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Quyền đã được cấp, thực hiện cuộc gọi với số đã lưu
                        if (pendingPhoneNumberToCall != null) {
                            performCall(pendingPhoneNumberToCall);
                            pendingPhoneNumberToCall = null; // Xóa số đang chờ
                        }
                    } else {
                        // Quyền bị từ chối
                        Toast.makeText(this, "Bạn đã từ chối cấp quyền gọi điện", Toast.LENGTH_SHORT).show();
                        pendingPhoneNumberToCall = null; // Xóa số đang chờ
                    }
                }
        );


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Danh bạ");
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        // Truyền lambda expression để xử lý sự kiện click gọi
        adapter = new ContactAdapter(this, contactList, this::tryToMakeCall); // Đổi thành tryToMakeCall
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        checkPermissionAndLoadContacts(); // Kiểm tra quyền đọc danh bạ
        setupSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addContact();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterContacts(newText);
                return true;
            }
        });
    }

    private void filterContacts(String text) {
        List<Contact> filtered = new ArrayList<>();
        for (Contact c : contactList) {
            // Kiểm tra null trước khi gọi toLowerCase
            if ((c.name != null && c.name.toLowerCase().contains(text.toLowerCase())) ||
                    (c.phone != null && c.phone.contains(text))) {
                filtered.add(c);
            }
        }
        adapter.filterList(filtered);
    }

    private void addContact() {
        // Kiểm tra quyền ghi danh bạ trước khi mở AddContactActivity (nếu Activity đó cần ghi)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Bạn có thể yêu cầu quyền WRITE_CONTACTS ở đây nếu AddContactActivity cần
            Toast.makeText(this, "Cần quyền ghi danh bạ để thêm liên hệ", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, 101); // Code khác 100
            // Hoặc không mở AddContactActivity nếu không có quyền
            // return;
        }
        Intent intent = new Intent(this, AddContactActivity.class);
        addContactLauncher.launch(intent);
    }

    // Bước 1: Hiển thị dialog xác nhận
    private void tryToMakeCall(String phone) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận cuộc gọi")
                .setMessage("Gọi đến số: " + phone + "?")
                .setPositiveButton("Gọi", (dialog, which) -> {
                    // Bước 2: Kiểm tra quyền sau khi người dùng xác nhận
                    checkCallPermissionAndProceed(phone);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    pendingPhoneNumberToCall = null; // Đảm bảo xóa nếu hủy
                })
                .show();
    }

    // Bước 2: Kiểm tra quyền và yêu cầu nếu cần
    private void checkCallPermissionAndProceed(String phone) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền -> Thực hiện cuộc gọi ngay
            performCall(phone);
        } else {
            // Chưa có quyền -> Lưu số và yêu cầu quyền
            pendingPhoneNumberToCall = phone; // Lưu số điện thoại lại
            // (Tùy chọn) Hiển thị giải thích tại sao cần quyền nếu người dùng từ chối trước đó
            // if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) { ... }
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE); // Yêu cầu quyền
        }
    }

    // Bước 3: Thực hiện cuộc gọi (chỉ gọi khi đã có quyền)
    private void performCall(String number) {
        if (number != null && !number.trim().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));
            try {
                // Kiểm tra lại lần nữa cho chắc (thường không cần thiết nếu logic đúng)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    Toast.makeText(this, "Lỗi: Không có quyền gọi điện.", Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Log.e("MakeCall", "SecurityException during call", e);
                Toast.makeText(this, "Lỗi bảo mật khi gọi điện.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("MakeCall", "Exception during call", e);
                Toast.makeText(this, "Không thể thực hiện cuộc gọi.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Số điện thoại không hợp lệ.", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermissionAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền đọc danh bạ
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSION_CODE); // Sử dụng hằng số
        } else {
            // Đã có quyền, tải danh bạ
            loadContacts();
        }
    }

    private void loadContacts() {
        // Thêm kiểm tra quyền đọc ở đây một lần nữa cho chắc chắn
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LoadContacts", "Attempted to load contacts without permission.");
            // Có thể hiển thị thông báo hoặc không làm gì cả
            Toast.makeText(this, "Cần quyền đọc danh bạ để hiển thị.", Toast.LENGTH_SHORT).show();
            return; // Không tải nếu không có quyền
        }

        contactList.clear(); // Xóa danh sách cũ trước khi tải mới
        Cursor cursor = null; // Khởi tạo null để đóng trong finally
        try {
            cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, // Hoặc chỉ định các cột cần thiết: new String[]{DISPLAY_NAME, NUMBER}
                    null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                // Kiểm tra index hợp lệ một lần trước vòng lặp
                if (nameIndex == -1 || phoneIndex == -1) {
                    Log.e("LoadContacts", "Could not get column indices for Name or Phone.");
                    return; // Không thể tiếp tục nếu thiếu cột
                }

                do {
                    String name = cursor.getString(nameIndex);
                    String phone = cursor.getString(phoneIndex);
                    // Có thể thêm kiểm tra null hoặc trống cho name/phone nếu muốn
                    if (name != null && phone != null) {
                        contactList.add(new Contact(name, phone));
                    }
                } while (cursor.moveToNext());

                Log.d("MainActivity", "Loaded " + contactList.size() + " contacts.");
            } else {
                Log.d("MainActivity", "Cursor is null or empty");
            }
        } catch (Exception e) {
            Log.e("LoadContacts", "Error loading contacts", e);
            Toast.makeText(this, "Lỗi khi tải danh bạ", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close(); // Luôn đóng cursor
            }
        }
        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView sau khi tải xong (hoặc có lỗi)
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Quan trọng: Gọi lại lớp cha

        if (requestCode == READ_CONTACTS_PERMISSION_CODE) { // Xử lý kết quả quyền đọc danh bạ
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đọc danh bạ được cấp -> Tải danh bạ
                loadContacts();
            } else {
                // Quyền đọc danh bạ bị từ chối
                Toast.makeText(this, "Bạn đã từ chối cấp quyền đọc danh bạ", Toast.LENGTH_SHORT).show();
                // Có thể hiển thị màn hình trống hoặc thông báo yêu cầu quyền lại
            }
        }
        // Không cần xử lý quyền CALL_PHONE ở đây nữa vì đã dùng ActivityResultLauncher
        // Không cần xử lý quyền WRITE_CONTACTS ở đây nếu bạn yêu cầu nó trong addContact() và không cần làm gì đặc biệt sau khi cấp quyền.
    }
}