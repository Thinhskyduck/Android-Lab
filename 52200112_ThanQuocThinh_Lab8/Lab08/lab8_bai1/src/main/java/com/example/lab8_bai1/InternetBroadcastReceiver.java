package com.example.lab8_bai1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

// CREDIT:
// FILE NÀY ĐƯỢC TRIỂN KHAI TRONG DỰ ÁN CUỐI KỲ QUIZFLASH CỦA NHÓM INNOVATECH
// CÁC THÀNH VIÊN TRONG NHÓM BAO GỒM: 52200007 - Đặng Văn Trọng, 52200088 - Nguyễn Thành Tiến, 52200097 - Nguyễn Vĩnh Hưng

public class InternetBroadcastReceiver extends BroadcastReceiver implements InternetChangeListener {

    //  Đây là hàm tĩnh dùng để kiểm tra kết nối mạng tại thời điểm gọi
    //  Gọi hàm này bằng cú pháp -> InternetBroadcaseReceiver.isConnectedNow(this)
    //  Hàm trả về kiểu dữ liệu boolean đại diện cho việc có kết nối mạng hay không.
    //  Hàm chỉ trả về dữ liệu mạng ở thời điểm gọi, không có tác dụng thông báo khi kết nối mạng thay đổi.

    public static boolean isConnectedNow(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null){
            return networkInfo.isConnectedOrConnecting();
        }
        return false;
    }

    //   Bên dưới là code để thực hiện việc kiểm tra internet có thay đổi trong toàn bộ thời gian hoạt động trên một activity.
    //   Code sử dụng BroadcastReceiver, và để hoạt động được phải đi kèm với interface InternetChangeListener.
    //   Để triển khai code, làm theo các bước sau:

    /*
        1. Ngay trong class của activity cần triển khai, thêm vào code bên dưới:

            private final InternetBroadcastReceiver internetBroadcastReceiver =
                    new InternetBroadcastReceiver(connectivity ->  {
                // Your code here
            });

        > Giải thích:
            - Tạo đối tượng hằng của lớp này, truyền vào interface bằng phương pháp lambda.
            - Code được thực thi tại đoạn -> // Your code here
        -----------------

        2. Override hàm onStart() của activity và thêm vào hai dòng code sau:

            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(internetBroadcastReceiver, filter);

        > Giải thích:
            - Tạo IntentFilter cho activity, điều này có thể được thực hiện ngoài file manifest (static)
                nhưng nếu làm trong code thì sẽ dynamic và dùng được ở các API đời sau.
            - Đăng ký BroadcastReceiver cho đối tượng khởi tạo của class này.

        -----------------

        3. Override hàm onStop() của activity và thêm vào dòng code sau:
            unregisterReceiver(internetBroadcastReceiver);

        > Giải thích: Hủy đăng ký BroadcastReceiver khi ngừng dùng activity.

        -----------------

        4. Triển khai code tại khu vực '// Your code here' ở bước 1.
        Với connectivity là một biến boolean đại diện cho tình trạng kết nối mạng (có hoặc không).
    */

    private final InternetChangeListener listener;

    public InternetBroadcastReceiver(InternetChangeListener listener){
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
            );
            listener.onInternetChange(!noConnectivity);
        }
    }

    @Override
    public void onInternetChange(boolean connectivity) {
        // Your code here
    }

}