---
name: performance-optimization
description: Kỹ năng tối ưu hóa tốc độ phản hồi, bộ nhớ và tiêu thụ pin cho ứng dụng Android.
metadata:
  author: Android-Expert
  version: "1.0"
---

# QUY TẮC TỐI ƯU HIỆU NĂNG

## 1. UI & Compose Optimization

*   **Recomposition:** Sử dụng `@Stable` và `@Immutable` cho các Data Class dùng trong UI để giảm thiểu Recomposition thừa.
*   **Lazy List:** Luôn cung cấp key cho các item trong `LazyColumn` để tối ưu việc tái sử dụng.

## 2. Resource Management

*   **Memory Leaks:** Kiểm tra các callback, listener trong `ViewModel`. Đảm bảo chúng được clear khi `ViewModel` bị hủy.
*   **Image Loading:** Sử dụng thư viện (Coil/Glide) với cấu hình kích thước ảnh phù hợp (không load ảnh 4K vào ImageView 100dp).

## 3. Background Task

*   **Coroutines:** Sử dụng đúng `Dispatchers` (`IO` cho network/disk, `Default` cho tính toán nặng).

# WORKFLOW CHO AGENT

Khi review hoặc viết code phức tạp:

1.  Phân tích xem có đoạn code nào chạy trên Main Thread gây block UI không.
2.  Kiểm tra xem cấu trúc dữ liệu có gây lãng phí bộ nhớ không.
3.  Đề xuất sử dụng Baseline Profiles để tăng tốc độ khởi động App.