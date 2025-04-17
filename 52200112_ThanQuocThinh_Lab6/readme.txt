Project được thiết kế để chạy từ API 24 trở lên nhưng tốt nhất là Android 8 API 26.

Bài 1:
Em có sử dụng thư viện ngoài để nhập màu, thầy cần sync lại file graddle để có thể chắc chắn rằng việc chạy ứng dụng không bị lỗi.

Bài 2:
Đọc ghi internal là cố định.
Đọc ghi external có thể được truy cập thông qua bất kỳ văn bản của file nào trong bộ nhớ ngoài.

Bài 4:
Có sự khác nhau trong quyền truy cập bộ nhớ giữa các đời Android khác nhau.
Android cao cần quyền truy cập vô toàn bộ file trong cài đặt, trong khi các đời Android thấp chỉ cần cấp quyền truy cập bộ nhớ ngoài.
Project đã được test và chắc chắn hoạt động trên Android 7 và Android 14.