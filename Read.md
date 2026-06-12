Khối bắt buộc: Quản trị & Nền tảng
UC-01: Đăng nhập & Khởi tạo Token (JWT)
Tác nhân: Admin, Staff, Customer
Mô tả: Khách hàng cung cấp thông tin. Hệ thống xác thực bằng Spring Security và trả về chuỗi JWT.
Luồng chính: 1. POST JSON (username, password) tới /api/auth/login.
AuthenticationManager đối chiếu DB.
Trả về HTTP 200 OK cùng AccessToken và RefreshToken.
Luồng ngoại lệ: Sai thông tin -> BadCredentialsException -> HTTP 401. Tài khoản bị khóa -> HTTP 403.

UC-02: Quản trị danh mục Khách hàng
Tác nhân: Admin, Staff
Mô tả: Thao tác CRUD lên danh sách khách hàng. Bắt buộc sử dụng kỹ thuật JPQL Constructor Projection để yêu cầu Database chỉ truy vấn đúng các trường dữ liệu cần thiết, tuyệt đối không tải toàn bộ thực thể (Entity) lên bộ nhớ RAM nhằm tối ưu hóa hiệu năng hệ thống.
Luồng chính:
Admin hoặc Staff gọi GET/PUT tới /api/v1/users.
Spring Security Filter xác nhận Role hợp lệ.
Controller gọi Service.
Tầng Repository thực thi câu lệnh JPQL sử dụng từ khóa new kết hợp đường dẫn Class DTO để ánh xạ trực tiếp dữ liệu từ các cột của bảng CSDL.
Trả về đối tượng Page<UserResponseDto> gọn nhẹ với mã HTTP 200 OK .
Luồng ngoại lệ: Sai quyền -> HTTP 403. Lỗi Validation -> HTTP 400.

UC-03: Đăng xuất & Thu hồi Token
Tác nhân: Admin, Staff, Customer
Mô tả: Đưa AccessToken vào Blacklist.
Luồng chính: POST tới /api/auth/logout. Lưu JWT vào Database. Trả về HTTP 200 OK.
Khối nâng cao: Nghiệp vụ & Tích hợp

UC-04: Chuyển tiền & Ghi nhật ký kiểm toán (AOP)
Tác nhân: Customer
Mô tả: Khách hàng thực hiện chuyển tiền. Hành động này được log lại bằng AOP độc lập với logic trừ tiền. Đòi hỏi cấu hình @Transactional bắt buộc ở Service.
Luồng chính: 1. Client gửi JSON (targetAccountId, amount) tới /api/transactions/transfer.
Service trừ tiền tài khoản nguồn, cộng tiền tài khoản đích và tạo bản ghi Transaction.
Kích hoạt AOP (@AfterReturning), trích xuất thông tin ghi dòng log: [AUDIT] Account A transferred Amount to Account B.
Controller trả về HTTP 200 OK.
Luồng ngoại lệ: Không đủ số dư -> Quăng InsufficientBalanceException -> HTTP 409 Conflict. AOP bắt lỗi qua @AfterThrowing ghi log giao dịch thất bại.

UC-05: Định danh điện tử (eKYC) & Tích hợp lưu trữ đám mây
Tác nhân: Customer
Mô tả: Tải lên hình ảnh CCCD/Passport (MultipartFile). Hệ thống đẩy lên Cloudinary/AWS S3 qua SDK, lưu URL vào CSDL.
Luồng chính: 1. POST multipart/form-data chứa file vật lý tới /api/v1/kyc/upload.
Service gọi hàm upload của nền tảng đám mây.
Nhận về Secure URL, cập nhật vào bảng KycProfile và chuyển trạng thái status là PENDING .
Trả về HTTP 200 OK kèm DTO.
Luồng ngoại lệ: Sai định dạng file -> HTTP 400. Lỗi kết nối đám mây -> HTTP 503 hoặc 500.

UC-06: Xem sao kê lịch sử giao dịch
Tác nhân: Customer
Mô tả: Khách hàng thực hiện truy vấn danh sách các giao dịch biến động số dư (gồm cả chuyển đi và nhận về) của một tài khoản cụ thể theo thời gian. Hệ thống bắt buộc phải truy vấn dựa trên cả hai trường khóa ngoại (from_account_id và to_account_id) để trả về kết quả đồng bộ và chính xác nhất.
Luồng chính:
Customer gọi GET tới api lấy về danh sách lịch sử giao dịch kèm tham số phân trang.
Spring Security Filter kiểm tra tính hợp lệ của AccessToken và xác nhận quyền sở hữu tài khoản của Customer đó.
Controller tiếp nhận mã số tài khoản (accountId) và gọi xuống tầng Service.
Tầng Repository thực thi câu lệnh SQL/JPQL sử dụng phép toán OR để quét tất cả bản ghi có tài khoản này tham gia với tư cách là bên gửi hoặc bên nhận.
Tầng Service ánh xạ dữ liệu sang DTO và tự động tính toán thuộc tính loại giao dịch (Nếu accountId trùng với from_account_id thì đánh dấu là "Giao dịch trừ tiền - DEBIT", nếu trùng với to_account_id thì đánh dấu là "Giao dịch cộng tiền - CREDIT").
Trả về danh sách sao kê có phân trang xếp theo thứ tự thời gian mới nhất với mã HTTP 200 OK.
