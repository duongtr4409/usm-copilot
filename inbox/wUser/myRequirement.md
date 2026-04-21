# Software Requirement Specification (SRS): Academy Management System (AMS)

## 1. Tổng quan hệ thống
Xây dựng hệ thống quản lý học viện đa phân hệ, hỗ trợ quản lý nhân sự, cơ cấu tổ chức theo cấp bậc và tự động hóa quy trình cấp phát tài khoản học viên.

## 2. Phân hệ QUẢN LÝ (Admin Portal)

### 2.1. Quản lý Cơ cấu Tổ chức & Đơn vị
- **Cấu trúc:** Dữ liệu dạng cây (Parent-Child) không giới hạn cấp độ.
- **Loại đơn vị:** `Phòng`, `Ban`, `Văn Phòng`, `Khoa`, `Trung Tâm`, `Lớp`.
- **Chức năng chính:** - CRUD (Thêm, Sửa, Xóa) đơn vị.
    - Hiển thị danh sách đơn vị dưới dạng **Tree-view**.
- **Quy tắc gán nhân sự (Business Logic):**
    - **Đơn vị hành chính (Phòng, Ban, VP, Khoa, TT):** Một Cán bộ/Giảng viên chỉ thuộc **duy nhất 1** đơn vị này. Nếu chuyển đơn vị, hệ thống phải cập nhật lại mối quan hệ cũ.
    - **Đơn vị loại "Lớp":**
        - **Thêm học viên:** Khi thêm học viên vào lớp, hệ thống thực hiện đồng thời: (1) Tạo hồ sơ học viên, (2) Tự động tạo tài khoản tương ứng (Role: Student).
        - **Ban cán sự:** Chọn từ danh sách học viên đã thuộc lớp đó.
        - **Người phụ trách:** Chọn từ danh sách Cán bộ/Giảng viên. Một người có thể phụ trách **nhiều lớp** và đồng thời vẫn có thể thuộc 1 đơn vị hành chính.

### 2.2. Quản lý Cán bộ & Giảng viên
- **Đối tượng:** Gồm 2 loại (Cán bộ, Giảng viên).
- **Chức năng:** CRUD thông tin hồ sơ nhân sự tổng quát.

### 2.3. Quản lý Tài khoản & Phân quyền (RBAC)
- **Hệ thống quyền:** `ADMIN` và `STUDENT`.
- **Chức năng:** Đăng nhập, đăng xuất, quản lý thông tin tài khoản người dùng.

### 2.4. Quản lý Đào tạo & Tin tức
- **Học viên:** Quản lý lý lịch và bảng điểm cá nhân.
- **Tin tức:** Soạn thảo (Rich Text Editor), quản lý danh sách và đăng tải bài viết.

## 3. Phân hệ HỌC VIÊN (Student Portal)
- **Đăng nhập:** Sử dụng tài khoản được cấp tự động khi được thêm vào lớp.
- **Chức năng:**
    - Xem thông tin lý lịch cá nhân.
    - Xem bảng điểm cá nhân.
    - Xem trang tin tức của học viện.

## 4. Yêu cầu Kỹ thuật

### 4.1. Database 
- Thiết kế bảng `OrganizationUnit` với `parent_id` tự liên kết.
- Bảng `Staff` cần có khóa ngoại đến `OrganizationUnit` (cho đơn vị hành chính).
- Bảng `ClassAssignment` (nhiều - nhiều) để quản lý Staff phụ trách nhiều lớp.
- Sử dụng **Database Transaction** khi thực hiện "Thêm học viên vào lớp" để đảm bảo tính nhất quán giữa hồ sơ và tài khoản.

### 4.2. Logic
- Triển khai Validation: `Staff.unit_id` chỉ được trỏ tới đơn vị NOT 'Lớp'.
- Xây dựng Workflow: `AddStudentToClass` -> `CreateAccount` -> `CreateProfile` -> `Enrollment`.

### 4.3. UI
- Sử dụng thư viện UI (Ant Design/Tailwind) để render Tree-view cho cơ cấu tổ chức.
- Form thêm học viên vào lớp phải bao gồm các trường: Username, Password ban đầu, và Thông tin lý lịch.