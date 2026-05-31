# 🏆 Hệ Thống Đấu Giá Trực Tuyến

> **Bài tập lớn — Lập trình nâng cao**  
> Trường Đại học Công nghệ — ĐHQGHN

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-red?logo=apachemaven)](https://maven.apache.org/)

---

## 📖 Mục lục

1. [Mô tả bài toán](#mô-tả-bài-toán)
2. [Công nghệ & Môi trường](#công-nghệ--môi-trường)
3. [Yêu cầu cài đặt](#yêu-cầu-cài-đặt)
4. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
5. [Hướng dẫn chạy](#hướng-dẫn-chạy)
6. [Chức năng đã hoàn thành](#chức-năng-đã-hoàn-thành)
7. [Báo cáo & Demo](#báo-cáo--demo)

---

## Mô tả bài toán

Hệ thống đấu giá trực tuyến theo kiến trúc **Client–Server**, cho phép nhiều người dùng đồng thời tham gia đấu giá sản phẩm theo thời gian thực. Hệ thống hỗ trợ ba vai trò: **Bidder** (người đặt giá), **Seller** (người đăng sản phẩm) và **Admin** (quản trị viên).

Phạm vi hệ thống bao gồm:
- Quản lý tài khoản, xác thực và phân quyền người dùng
- Quản lý sản phẩm đa loại (Electronics, Art, Fashion, Furniture)
- Tạo và điều hành phiên đấu giá với đồng hồ đếm ngược
- Đặt giá realtime, lịch sử bid, biểu đồ diễn biến giá
- Ví điện tử với cơ chế tạm khóa/hoàn tiền tự động
- Trang hồ sơ cá nhân (User Profile) và quản lý ví (Wallet)
- Thông báo sự kiện và nhật ký hoạt động

---

## Công nghệ & Môi trường

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Java | 17 | Ngôn ngữ chính |
| JavaFX | 21 | Giao diện đồ họa (GUI + FXML) |
| MySQL | 8.0 | Cơ sở dữ liệu |
| HikariCP | 5.1.0 | Connection Pool thread-safe |
| BCrypt | 0.10.2 | Mã hóa mật khẩu |
| JUnit 5 | 5.11.4 | Unit testing |
| Maven | 3.8+ | Build & dependency management |

**Giao tiếp mạng:** TCP Socket (cổng mặc định `9999`), giao thức text tự định nghĩa.

---

## Yêu cầu cài đặt

Trước khi chạy, đảm bảo máy đã cài đặt:

- **JDK 17** trở lên — [Tải tại Adoptium](https://adoptium.net/)
- **Maven 3.8+** — [Tải tại Apache Maven](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** — [Tải tại MySQL](https://dev.mysql.com/downloads/)

Kiểm tra phiên bản:

```bash
java -version
mvn -version
mysql --version
```

---

## Cấu trúc thư mục

```
BaiTapLonLTNC/
├── src/
│   ├── main/
│   │   ├── java/com/auction/
│   │   │   ├── client/                        # Phía Client (JavaFX)
│   │   │   │   ├── AuctionFxApp.java           # Entry point client
│   │   │   │   ├── controller/                 # FXML Controllers
│   │   │   │   │   ├── LoginController.java
│   │   │   │   │   ├── RegisterController.java
│   │   │   │   │   ├── MainAuctionController.java
│   │   │   │   │   ├── AuctionRoomController.java
│   │   │   │   │   ├── CreateAuctionController.java
│   │   │   │   │   ├── ProfileController.java      # Hồ sơ & Ví
│   │   │   │   │   ├── SellerProductsController.java
│   │   │   │   │   ├── NotificationController.java
│   │   │   │   │   ├── ActivityLogController.java
│   │   │   │   │   └── AdminDashboardController.java
│   │   │   │   ├── service/
│   │   │   │   │   └── NetworkClient.java          # Kết nối TCP, Observer pattern
│   │   │   │   └── viewmodel/
│   │   │   │       └── AuctionRow.java
│   │   │   │
│   │   │   ├── server/                        # Phía Server
│   │   │   │   ├── network/
│   │   │   │   │   ├── AuctionServer.java      # ThreadPool + broadcast
│   │   │   │   │   └── ClientHandler.java      # Router lệnh
│   │   │   │   ├── dao/
│   │   │   │   │   ├── UserDAO.java
│   │   │   │   │   ├── AuctionDAO.java
│   │   │   │   │   └── ItemDAO.java
│   │   │   │   └── util/
│   │   │   │       └── DatabaseUtil.java       # HikariCP Singleton
│   │   │   │
│   │   │   ├── common/                        # Dùng chung Client + Server
│   │   │   │   ├── models/                    # User, Item và các lớp con
│   │   │   │   ├── dto/
│   │   │   │   │   └── BidResult.java
│   │   │   │   ├── exception/                 # Custom exceptions
│   │   │   │   ├── pattern/
│   │   │   │   │   └── ItemFactory.java       # Factory Method Pattern
│   │   │   │   └── util/
│   │   │   │
│   │   │   └── domain/                        # Nghiệp vụ đấu giá
│   │   │       ├── AuctionSession.java         # Core logic (synchronized)
│   │   │       ├── AuctionManager.java         # Singleton
│   │   │       ├── AuctionStatus.java          # Enum trạng thái
│   │   │       └── BidTransaction.java         # Immutable value object
│   │   │
│   │   └── resources/
│   │       ├── config/
│   │       │   ├── application.properties      # Cấu hình DB (gitignore)
│   │       │   └── schema.sql
│   │       └── fxml/                           # Giao diện JavaFX
│   │
│   └── test/java/com/auction/                 # JUnit 5 Tests
│
├── .github/workflows/
│   └── ci.yml                                 # GitHub Actions CI/CD
├── pom.xml
└── README.md
```

---

## Hướng dẫn chạy

### Bước 1 — Clone dự án

```bash
git clone https://github.com/YOUR_ORG/BaiTapLonLTNC.git
cd BaiTapLonLTNC
```

### Bước 2 — Cấu hình cơ sở dữ liệu

Tạo file `src/main/resources/config/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/auction_system?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

> ⚠️ File này đã được thêm vào `.gitignore`. **Không commit mật khẩu lên GitHub.**

### Bước 3 — Thiết lập Database (MySQL Workbench)

Nhóm sử dụng **MySQL Workbench** để khởi tạo schema. Thực hiện theo các bước sau:

1. Mở **MySQL Workbench** và kết nối vào MySQL Server local
2. Mở tab **Query** (File → New Query Tab hoặc `Ctrl+T`)
3. Dán toàn bộ đoạn SQL bên dưới vào editor rồi nhấn **Execute** (⚡ hoặc `Ctrl+Shift+Enter`)

```sql
DROP DATABASE IF EXISTS auction_system;
CREATE DATABASE auction_system;
USE auction_system;

CREATE TABLE users (
    id            VARCHAR(50)  PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    role          VARCHAR(20)  NOT NULL,
    balance       DOUBLE       DEFAULT 0,
    frozen_amount DOUBLE       DEFAULT 0
);

CREATE TABLE items (
    id          VARCHAR(50)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    init_price  DOUBLE       NOT NULL,
    category    VARCHAR(50)  NOT NULL
);

CREATE TABLE auction_sessions (
    auction_id          VARCHAR(50) PRIMARY KEY,
    item_id             VARCHAR(50) NOT NULL,
    seller_id           VARCHAR(50) NOT NULL,
    start_time          DATETIME    NOT NULL,
    end_time            DATETIME    NOT NULL,
    status              VARCHAR(20) NOT NULL,
    winner_id           VARCHAR(50),
    current_highest_bid DOUBLE      DEFAULT 0
);

CREATE TABLE bid_transactions (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    auction_id  VARCHAR(50) NOT NULL,
    bidder_id   VARCHAR(50) NOT NULL,
    bid_amount  DOUBLE      NOT NULL,
    bid_time    DATETIME    NOT NULL
);
```

> ✅ Sau khi chạy thành công, database `auction_system` và 4 bảng sẽ được tạo tự động.  
> Ngoài ra, có thể chạy lệnh trên qua MySQL CLI: `mysql -u root -p < schema.sql`

### Bước 4 — Build dự án



```bash
mvn clean compile
```

### Bước 5 — Chạy Server

Mở một terminal và chạy:

```bash
# Linux / macOS
mvn exec:java -Dexec.mainClass="com.auction.server.network.AuctionServer"

# Windows (Command Prompt)
mvn exec:java -Dexec.mainClass="com.auction.server.network.AuctionServer"

# Windows (PowerShell)
mvn exec:java "-Dexec.mainClass=com.auction.server.network.AuctionServer"
```

Server sẽ tự động khởi tạo database, tạo bảng và lắng nghe ở cổng **9999**.

> ✅ Chờ đến khi thấy thông báo `Server started on port 9999` trước khi chạy Client.

### Bước 6 — Chạy Client

Mở **terminal mới** (giữ nguyên terminal Server) và chạy:

```bash
# Linux / macOS / Windows (tất cả đều dùng lệnh này)
mvn javafx:run
```

Có thể mở nhiều cửa sổ Client cùng lúc bằng cách chạy lại lệnh trên ở các terminal khác nhau.

### Bước 7 — Chạy Tests (tùy chọn)

```bash
mvn test
```

---

## Chức năng đã hoàn thành

### Chức năng bắt buộc ✅

| Chức năng | Trạng thái | Ghi chú |
|-----------|:----------:|---------|
| Đăng ký tài khoản | ✅ | Kiểm tra trùng username/email |
| Đăng nhập / Đăng xuất | ✅ | BCrypt password hashing |
| Phân quyền Bidder / Seller / Admin | ✅ | Giao diện thay đổi theo vai trò |
| Thêm & xem sản phẩm | ✅ | Seller tạo item, hỗ trợ nhiều danh mục |
| Tạo phiên đấu giá | ✅ | Giá khởi điểm, thời gian, mô tả |
| Đặt giá (bid) realtime | ✅ | Kiểm tra hợp lệ, cập nhật winner ngay |
| Tự động đóng phiên hết giờ | ✅ | ScheduledExecutorService |
| Xem danh sách phiên đang mở | ✅ | Lọc, cập nhật realtime |
| Xử lý ngoại lệ | ✅ | Custom exceptions, try-with-resources |
| Giao diện JavaFX + FXML | ✅ | Nhiều màn hình, điều hướng mượt |
| Kiến trúc Client–Server TCP | ✅ | ThreadPool 50 threads, broadcast |
| Lưu trữ MySQL + HikariCP | ✅ | Connection pool an toàn với đa luồng |
| Unit Test JUnit 5 | ✅ | 31 test cases, không phụ thuộc DB |

### Chức năng nâng cao ✅

| Chức năng | Trạng thái | Chi tiết |
|-----------|:----------:|---------|
| **Hồ sơ người dùng (User Profile)** | ✅ | Xem thông tin cá nhân, avatar, vai trò |
| **Ví điện tử (Wallet)** | ✅ | Số dư, số tiền đang tạm khóa, nạp tiền |
| **Freeze / Release tiền** | ✅ | Tạm khóa khi đặt cọc, hoàn tiền khi bị vượt |
| **Lịch sử đặt giá của tôi** | ✅ | Bảng theo dõi các lần bid và kết quả |
| **Biểu đồ giá realtime** | ✅ | LineChart cập nhật sau mỗi bid |
| **Anti-sniping** | ✅ | Bid trong 30s cuối → gia hạn thêm 60s |
| **Thông báo sự kiện** | ✅ | Nhận thông báo khi bị vượt giá, thắng phiên |
| **Nhật ký hoạt động** | ✅ | Ghi lại lịch sử hành động của người dùng |
| **Dashboard Admin** | ✅ | Thống kê, quản lý người dùng và phiên đấu |
| **Sản phẩm của Seller** | ✅ | Seller xem và quản lý sản phẩm đã đăng |
| **Đặt giá tự động (Auto-bid)** | ✅ | Cài maxBid + step, tự động đặt khi bị vượt |
| **Broadcast không polling** | ✅ | Observer pattern, cập nhật đẩy từ server |

---

## Báo cáo & Demo

| Tài nguyên | Liên kết |
|------------|----------|
| 📄 Báo cáo PDF | [Xem báo cáo](https://drive.google.com/file/d/10uW3nB_3mwFE6k6cLPAVbH6j8IIzZ6H5/view?usp=sharing) |
| 🎥 Video demo | [Xem video](https://drive.google.com/file/d/1SyozoCyFZr-OsIggEwWsHUvz7D3AwrHn/view?usp=sharing) |

