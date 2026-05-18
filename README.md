# ABC Company — Payroll System UI

A role-based payroll management desktop application built with **JavaFX 21** and **MySQL**.

---

## Requirements

Make sure you have these installed before running:

| Tool | Version |
|---|---|
| Java (JDK) | 21 or higher |
| MySQL Server | 8.x |
| Maven | Bundled via `mvnw` (no install needed) |

---

## 1. Set Up the Database

1. Open **MySQL Workbench** or any MySQL client and log in.
2. Create the database:
   ```sql
   CREATE DATABASE payroll_db;
   ```
3. The app will auto-create all the tables on first run.

---

## 2. Configure the Database Connection

Open this file:

```
src/main/resources/application.properties
```

Update the values to match your MySQL setup:

```properties
db.url=jdbc:mysql://localhost:3306/payroll_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila
db.user=root
db.password=your_password_here
```

> **Note:** The default config already points to `localhost:3306` with user `root`. Just change the password.

---

## 3. Run the Application

Open a terminal in the project root folder, then run:

```bash
# Windows
.\mvnw.cmd javafx:run

# Mac / Linux
./mvnw javafx:run
```

The app window will open. That's it!

---

## Default Login Accounts

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Employee | *(use the employee ID set by admin)* | `admin123` (default) |

> Passwords are hashed with BCrypt — change them after first login.

---

## Project Structure (Quick Overview)

```
src/main/java/com/example/payrollsystemui/
├── app/          # App startup, session management
├── database/     # DB connection (HikariCP pool)
├── model/        # Data models (Employee, User, PayrollSubmission, etc.)
├── repository/   # SQL queries per entity
├── service/      # Business logic (payroll calculation, validation)
└── ui/
    ├── admin/    # Admin screens (Dashboard, Employees, Payroll, Reports)
    ├── employee/ # Employee screens (Payslip, OT filing, Leave)
    ├── login/    # Login screen
    └── components/ # Shared UI components
```

---

## Useful Commands

```bash
# Run the app
.\mvnw.cmd javafx:run

# Fix code formatting (run this before committing)
.\mvnw.cmd spotless:apply

# Full build + all checks (Spotless, Checkstyle, SpotBugs)
.\mvnw.cmd verify
```

---

## Troubleshooting

**App won't start / "Database connection failed"**
- Make sure MySQL is running.
- Double-check `db.user` and `db.password` in `application.properties`.
- Make sure `payroll_db` database exists.

**Build fails with Spotless error**
- Run `.\mvnw.cmd spotless:apply` to auto-fix formatting, then try again.

**Port 3306 not found**
- Check if MySQL is listening on a different port and update `db.url` accordingly.
