# Addendum — TASK-002 (Clarifications, non-breaking)
**Date**: 2026-04-22
**Author**: @PMO (draft)

## Mục đích
Addendum này ghi các làm rõ nhỏ (non-breaking) cho `docs/specs/TASK-002.md` nhằm tránh hiểu lầm khi các agent đang phát triển. Tài liệu chính không bị ghi đè; đây là phần bổ sung để dev/QA tham khảo.

## Các làm rõ chính
1) UI library (FE)
- Khuyến nghị: **Ant Design** làm thư viện component chính (Tree, Modal, Form). Nếu đội FE chọn **Tailwind + shadcn/ui**, vui lòng ghi rõ mapping component.
- Tác động: non-breaking (chỉ ảnh hưởng FE implementation).

2) Organization Tree — lazy-loading API
- Yêu cầu: API phải hỗ trợ lazy-loading children để xử lý cây sâu.
- Gợi ý API:
  - `GET /api/v1/org-units?parentId={id}&pageSize={n}` → trả danh sách con trực tiếp kèm `hasChildren` flag.
  - `GET /api/v1/org-units/{id}/subtree?depth={n}` → (tùy chọn) trả subtree cho export/administration.
- AC: Khi mở trang Organization, client chỉ load top-level; expand nút sẽ gọi endpoint trên; UI hiển thị loading và xử lý `hasChildren`.
- Tác động: non-breaking (mở rộng API read endpoints).

3) Delivery of initial credentials
- Quyết định tạm thời: **out-of-scope** cho automated email/SMS trong MVP. Khi `AddStudentToClass` thành công, API trả về thông tin đăng nhập (username, tempPassword) cho ADMIN (response body). Nếu muốn tự động gửi, cần task riêng (DevOps/Notifier).
- Tác động: non-breaking (change giao thức vận hành).

4) Audit / Transfer history
- AC bổ sung: khi `Staff.unit_id` thay đổi, tạo audit record: `{ staff_id, previous_unit_id, new_unit_id, changed_by, changed_at }`.
- Lưu ở bảng `staff_unit_transfer_audit` hoặc chung audit table.
- Tác động: non-breaking (thêm bảng audit).

5) HTTP status / error mapping (bổ sung)
- Đề xuất mapping chuẩn:
  - 409 Conflict → username already exists
  - 422 Unprocessable Entity → validation errors
  - 400 Bad Request → malformed payload
  - 401 Unauthorized → missing/invalid token
  - 403 Forbidden → insufficient role/permission
  - 423 Locked → account locked (optional)
- AC: API phải tuân thủ mapping để FE/QA có thể kiểm thử.

6) Tests (QA)
- Thêm test cases: lazy-loading tree, AddStudentToClass rollback scenarios, transfer-audit verification, credential returned in response.

7) Outbox / Notifications
- Vẫn giữ outbox write on enrollment success (already in ARCHITECTURE.md). Notification worker/email sending is optional and to be created as separate task if requested.

## Affected tasks (gợi ý)
- `TASK-005` (Backend): bổ sung endpoints lazy-loading, return credentials on AddStudentToClass, create transfer audit table, adhere to HTTP mapping.
- `TASK-006` (Frontend): sử dụng AntD (hoặc ghi rõ mapping), implement lazy-loading Tree, AddStudent modal shows returned credentials for admin to copy.
- `TASK-007`/`TASK-009` (QA): bổ sung test scenarios.

## Action items & recommended workflow
1. Đây là **non-breaking clarification**, không yêu cầu pause nếu thay đổi nhỏ.
2. @Java-BE: thêm endpoint suggestions và AC testing; confirm if any schema additions require migration (e.g., audit table). Nếu migration cần, báo PMO để schedule.
3. @React-FE: phối hợp với BE về API params (`parentId`, `pageSize`, `hasChildren`) và quyết định UI library.
4. @QA-Tester: bổ sung cases trên test plan.
5. @PMO: nếu BE xác nhận cần migration/DB change → tổ chức short sync và block/resume tasks per ACP.

## Files
- Addendum (this file): `docs/specs/TASK-002.addendum.md`
- Original spec: `docs/specs/TASK-002.md`

---

*End of addendum (draft).*