# Hướng dẫn sử dụng hình ảnh

## ⚠️ QUAN TRỌNG: Đặt hình ảnh vào thư mục PUBLIC

**Vui lòng đặt file hình ảnh vào**: `frontend/public/images/airplane-sky.jpg`

**KHÔNG đặt vào** `src/assets/images/` (thư mục này chỉ để tham khảo)

## Các hình ảnh cần thiết

### 1. airplane-sky.jpg (BẮT BUỘC)
- **Mô tả**: Hình máy bay trắng bay trên bầu trời xanh với mây trắng
- **Kích thước đề xuất**: 1920x1080px hoặc lớn hơn (tỷ lệ 16:9)
- **Định dạng**: JPG, PNG, hoặc WebP
- **Vị trí file**: `frontend/public/images/airplane-sky.jpg`
- **Đường dẫn trong code**: `/images/airplane-sky.jpg`
- **Được sử dụng tại**:
  - ✅ Home page (hero background)
  - ✅ Login page (background)
  - ✅ Register page (background)

### 2. vinpearl-resort.jpg (TÙY CHỌN)
- **Mô tả**: Hình công viên giải trí Vinpearl
- **Kích thước đề xuất**: 1920x1080px hoặc lớn hơn
- **Có thể sử dụng cho**: 
  - Các trang khác
  - Làm background phụ
  - Hoặc banner quảng cáo

## Cách thêm hình ảnh

### Bước 1: Chuẩn bị hình ảnh
- Đảm bảo hình ảnh có chất lượng tốt
- Nén hình ảnh nếu cần (khuyến nghị < 2MB)
- Đặt tên file chính xác: `airplane-sky.jpg`

### Bước 2: Đặt file vào thư mục PUBLIC
```
frontend/public/images/airplane-sky.jpg
```

**LƯU Ý**: Đặt vào `public/images/` chứ KHÔNG phải `src/assets/images/`

### Bước 3: Kiểm tra
- Chạy `npm run dev` để xem kết quả
- Hình ảnh sẽ tự động được tối ưu bởi Vite
- Nếu hình không hiển thị, kiểm tra:
  - Tên file có đúng không
  - Đường dẫn trong CSS có đúng không
  - File có tồn tại trong thư mục không

## Cấu trúc thư mục

```
frontend/
├── public/
│   └── images/
│       ├── README.md
│       └── airplane-sky.jpg ← THÊM FILE NÀY VÀO ĐÂY
└── src/
    └── assets/
        └── images/
            └── README.md (file này - chỉ để tham khảo)
```

## Lưu ý quan trọng

- ✅ **Tên file phải chính xác**: `airplane-sky.jpg` (chữ thường, có dấu gạch ngang)
- ✅ Hình ảnh sẽ tự động được tối ưu bởi Vite khi build
- ✅ Nên sử dụng định dạng JPG hoặc WebP để tối ưu kích thước
- ✅ Kích thước file nên < 2MB để tải nhanh
- ✅ Nếu muốn đổi tên file, cần cập nhật đường dẫn trong các file JSX:
  - `pages/Home.jsx` (dòng: `const airplaneImage = '/images/airplane-sky.jpg';`)
  - `pages/Login.jsx`
  - `pages/Register.jsx`

## Troubleshooting

**Hình ảnh không hiển thị?**
1. ✅ Kiểm tra file có trong `frontend/public/images/airplane-sky.jpg` không
2. ✅ Kiểm tra tên file có đúng `airplane-sky.jpg` không (chữ thường)
3. ✅ Kiểm tra console browser có lỗi 404 không
4. ✅ Thử restart dev server: `npm run dev`
5. ✅ Kiểm tra đường dẫn trong code có đúng `/images/airplane-sky.jpg` không

**Hình ảnh quá lớn?**
- Sử dụng công cụ nén hình ảnh online
- Hoặc chuyển sang định dạng WebP
