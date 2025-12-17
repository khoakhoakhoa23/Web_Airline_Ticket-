# Hướng dẫn cài đặt Node.js và npm

## Cách 1: Cài đặt bằng Winget (Khuyến nghị - Windows 10/11)

Mở PowerShell với quyền Administrator và chạy:

```powershell
winget install OpenJS.NodeJS.LTS
```

Sau khi cài đặt xong:
1. Đóng và mở lại PowerShell
2. Kiểm tra cài đặt:
   ```powershell
   node --version
   npm --version
   ```

## Cách 2: Tải từ trang chủ Node.js

1. Truy cập: https://nodejs.org/
2. Tải bản **LTS** (Long Term Support) - khuyến nghị
3. Chạy file installer và làm theo hướng dẫn
4. Đảm bảo chọn option "Add to PATH" trong quá trình cài đặt
5. Sau khi cài xong, đóng và mở lại PowerShell
6. Kiểm tra:
   ```powershell
   node --version
   npm --version
   ```

## Cách 3: Sử dụng Chocolatey (nếu đã cài Chocolatey)

```powershell
choco install nodejs-lts
```

## Sau khi cài đặt xong

1. **Đóng và mở lại PowerShell** (quan trọng!)
2. Kiểm tra cài đặt:
   ```powershell
   node --version
   npm --version
   ```
3. Nếu vẫn không nhận, thử:
   ```powershell
   $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
   ```
4. Hoặc restart máy tính

## Kiểm tra cài đặt thành công

Chạy các lệnh sau:
```powershell
node --version    # Nên hiển thị: v20.x.x hoặc tương tự
npm --version     # Nên hiển thị: 10.x.x hoặc tương tự
```

## Sau khi cài Node.js xong

1. Vào thư mục frontend:
   ```powershell
   cd frontend
   ```

2. Cài đặt dependencies:
   ```powershell
   npm install
   ```

3. Chạy frontend:
   ```powershell
   npm run dev
   ```

## Troubleshooting

### Nếu vẫn không nhận npm sau khi cài:
1. Kiểm tra PATH environment variable:
   - Mở System Properties → Environment Variables
   - Kiểm tra trong PATH có đường dẫn đến Node.js không (thường là `C:\Program Files\nodejs\`)
   - Nếu không có, thêm vào

2. Restart máy tính

3. Thử cài lại Node.js và đảm bảo chọn "Add to PATH"

### Nếu gặp lỗi permission:
- Chạy PowerShell với quyền Administrator
- Hoặc sử dụng cách 2 (tải từ trang chủ)

