# Dịch Việt – Trung (mã nguồn Android)

Ứng dụng Android cơ bản để dịch câu tiếng Việt sang tiếng Trung giản thể, hiển thị Pinyin, đọc câu tiếng Trung bằng Android TextToSpeech và phóng to chữ bằng cách xoay ngang màn hình.

## Cách tạo APK trên GitHub, không cần Android Studio

1. Tải và giải nén file ZIP này.
2. Đăng nhập GitHub và tạo repository mới, ví dụ `VietTrungDich`. Có thể để Private.
3. Trên trang repository, bấm **Add file → Upload files**.
4. Tải lên toàn bộ *nội dung bên trong* thư mục đã giải nén (bao gồm thư mục `.github`; nếu giao diện web không hiện thư mục ẩn, tạo file `.github/workflows/build-apk.yml` bằng cách Add file → Create new file).
5. Bấm **Commit changes**.
6. Mở tab **Actions** của repository. Chọn workflow **Build Android APK**. Nếu được hỏi, bấm **Enable workflows**.
7. Chờ job có dấu tích xanh. Mở lần chạy đó, kéo xuống **Artifacts**, tải `VietTrungDich-debug-apk`.
8. Giải nén file artifact đã tải về. Bên trong có `app-debug.apk`. Chuyển file đó vào điện thoại Android và bấm để cài đặt.

## Lưu ý
- Đây là bản debug để thử nghiệm cá nhân, chưa phải bản phát hành ký số để đưa lên Google Play.
- Dịch trực tuyến cần Internet; endpoint dịch không chính thức có thể thay đổi hoặc giới hạn truy cập.
- Phát âm sử dụng giọng Text-to-Speech của điện thoại. Nếu chưa có tiếng Trung, cài dữ liệu giọng nói tiếng Trung giản thể trong cài đặt Text-to-Speech.
- Khi Android yêu cầu, chỉ cho phép cài ứng dụng từ nguồn mà anh tin cậy. Không chia sẻ APK debug với người lạ.
