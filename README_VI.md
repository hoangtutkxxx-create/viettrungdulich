# Ứng dụng Dịch Việt – Trung (bộ mã nguồn APK)

Ứng dụng Android đơn giản gồm:
- Nhập câu tiếng Việt và dịch sang tiếng Trung giản thể.
- Nút phát âm tiếng Trung bằng giọng đọc có sẵn trên Android.
- Nút phóng to: xoay màn hình ngang và tăng cỡ chữ; nhấn lại để về dọc.

## Lưu ý
- Dịch máy cần Internet và dùng dịch vụ MyMemory; dịch vụ miễn phí có thể giới hạn hoặc ngừng hoạt động.
- Pinyin được tạo bằng thư viện pinyin4j; với chữ đa âm, phiên âm tự động đôi khi có thể chưa đúng ngữ cảnh.
- Phát âm phụ thuộc việc điện thoại có cài dữ liệu giọng Trung Quốc giản thể trong Text-to-Speech.
- Đây là APK debug để thử nghiệm, chưa phải bản phát hành ký số.

## Cách tạo APK bằng GitHub trên điện thoại
1. Mở https://github.com/ và tạo tài khoản miễn phí.
2. Tạo repository mới tên `VietTrungAPK`, chọn **Public**, bấm **Create repository**.
3. Trên GitHub, mở **Add file → Upload files**. Nếu dùng điện thoại, có thể dễ hơn khi bật **Trang web cho máy tính** trong menu trình duyệt.
4. Giải nén file ZIP này bằng ứng dụng quản lý tệp. Tải lên toàn bộ NỘI DUNG đã giải nén, giữ nguyên cấu trúc thư mục, đặc biệt `.github/workflows/build-apk.yml`. Không tải riêng file ZIP lên.
5. Bấm **Commit changes**.
6. Mở tab **Actions** trong repository. Chọn **Build Android APK** nếu được hỏi, rồi bấm **Run workflow**. Nếu workflow chạy tự động sau commit, mở lần chạy mới nhất.
7. Chờ đến khi có dấu tích xanh. Mở lần chạy đó, kéo xuống **Artifacts**, tải `VietTrung-debug-APK`.
8. Giải nén file artifact đã tải để lấy `app-debug.apk`. Mở APK để cài đặt. Android có thể yêu cầu cho phép cài ứng dụng từ trình duyệt/tệp.

## Nếu không thấy thư mục .github
Trong trình quản lý tệp, bật hiển thị tệp ẩn nếu cần. Đảm bảo đường dẫn là `.github/workflows/build-apk.yml`, không phải `github/workflows/...`.
