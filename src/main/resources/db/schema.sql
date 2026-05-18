CREATE TABLE IF NOT EXISTS users (
    employee_id VARCHAR(20) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS employees (
    employee_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    department VARCHAR(80) NOT NULL,
    position_title VARCHAR(80),
    employment_type VARCHAR(30) NOT NULL,
    basic_rate DECIMAL(12,2) NOT NULL,
    work_schedule VARCHAR(30) NOT NULL DEFAULT '08:00 - 17:00',
    sick_leave_balance DECIMAL(6,1) NOT NULL DEFAULT 0,
    vacation_leave_balance DECIMAL(6,1) NOT NULL DEFAULT 0,
    emergency_leave_balance DECIMAL(6,1) NOT NULL DEFAULT 0,
    company_loan_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    sss_loan_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    pagibig_loan_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (employee_id) REFERENCES users(employee_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    attendance_date DATE NOT NULL,
    time_in TIME,
    time_out TIME,
    hours_worked DECIMAL(6,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    late_minutes INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_attendance (employee_id, attendance_date),
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payroll_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL,
    cutoff_label VARCHAR(60) NOT NULL,
    cutoff_start DATE NOT NULL,
    cutoff_end DATE NOT NULL,
    days_worked DECIMAL(6,2) NOT NULL DEFAULT 0,
    filed_ot_hours DECIMAL(6,2) NOT NULL DEFAULT 0,
    sick_leave_used DECIMAL(6,2) NOT NULL DEFAULT 0,
    vacation_leave_used DECIMAL(6,2) NOT NULL DEFAULT 0,
    emergency_leave_used DECIMAL(6,2) NOT NULL DEFAULT 0,
    absent_days DECIMAL(6,2) NOT NULL DEFAULT 0,
    company_loan_deduction DECIMAL(12,2) NOT NULL DEFAULT 0,
    sss_loan_deduction DECIMAL(12,2) NOT NULL DEFAULT 0,
    pagibig_loan_deduction DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payslip_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(20) NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES payroll_submissions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS active_clock_sessions (
    employee_id VARCHAR(20) PRIMARY KEY,
    clock_in_time TIMESTAMP NOT NULL,
    attendance_date DATE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);
