PMO Notice — TASK-002 Addendum
Date: 2026-04-22

Kính gửi các agent liên quan,

Tôi vừa tạo một addendum ngắn cho `TASK-002` (non-breaking clarifications) để làm rõ một vài điểm nhỏ tác động tới FE/BE/QA. Vui lòng xem file:

- Addendum: [docs/specs/TASK-002.addendum.md](docs/specs/TASK-002.addendum.md)

Tóm tắt hành động mong đợi:
- @Java-BE: review phần "Org Tree lazy-loading" và "Transfer audit". Xác nhận nếu cần migration DB (trường hợp cần migration, báo PMO để schedule).
- @React-FE: xác nhận thư viện UI (Ant Design?) và điều chỉnh Tree component để gọi `parentId` API.
- @QA-Tester: thêm test cases cho lazy-loading và enrollment rollback.

Tình trạng: đây là **clarification non-breaking** — không cần pause công việc hiện tại trừ khi BE xác nhận thay đổi DB/schema.

Xin mọi người **acknowledge** bằng cách tạo một entry nhỏ vào `.github/copilot-agents/LOGS.json` hoặc phản hồi vào `inbox/PMO/`.

Cảm ơn,
@PMO