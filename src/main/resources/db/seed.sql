-- Default password for all demo accounts: admin123 (BCrypt)
-- Hash generated with jbcrypt cost 10

DELETE FROM payslip_snapshots;
DELETE FROM payroll_submissions;
DELETE FROM active_clock_sessions;
DELETE FROM attendance;
DELETE FROM employees;
DELETE FROM users;

INSERT INTO users (employee_id, password_hash, role) VALUES
('ADMIN001', 'PLACEHOLDER', 'ADMIN'),
('EMP001', 'PLACEHOLDER', 'EMPLOYEE'),
('EMP002', 'PLACEHOLDER', 'EMPLOYEE'),
('EMP003', 'PLACEHOLDER', 'EMPLOYEE'),
('EMP004', 'PLACEHOLDER', 'EMPLOYEE'),
('EMP005', 'PLACEHOLDER', 'EMPLOYEE');

INSERT INTO employees (employee_id, full_name, department, position_title, employment_type, basic_rate, work_schedule,
    sick_leave_balance, vacation_leave_balance, emergency_leave_balance,
    company_loan_balance, sss_loan_balance, pagibig_loan_balance, status) VALUES
('ADMIN001', 'Maria Santos', 'Management', 'HR Admin', 'REGULAR', 55000.00, '08:00 - 17:00', 15, 15, 3, 0, 0, 0, 'ACTIVE'),
('EMP001', 'Juan Dela Cruz', 'IT', 'Software Engineer', 'REGULAR', 35000.00, '08:00 - 17:00', 12, 10, 3, 20000, 5000, 0, 'ACTIVE'),
('EMP002', 'Ana Reyes', 'Finance', 'Accountant', 'REGULAR', 32000.00, '08:00 - 17:00', 15, 15, 3, 0, 0, 0, 'ACTIVE'),
('EMP003', 'Pedro Gomez', 'IT', 'Junior Developer', 'PROBATIONARY', 22000.00, '08:00 - 17:00', 5, 5, 1, 0, 0, 0, 'ACTIVE'),
('EMP004', 'Lisa Tan', 'Operations', 'Coordinator', 'CONTRACTUAL', 28000.00, '08:00 - 17:00', 10, 10, 2, 5000, 0, 0, 'ACTIVE'),
('EMP005', 'Mark Rivera', 'IT', 'Support Specialist', 'PART_TIME', 18000.00, '08:00 - 12:00', 5, 5, 1, 0, 2000, 0, 'ACTIVE');

INSERT INTO payroll_submissions (employee_id, cutoff_label, cutoff_start, cutoff_end, days_worked, filed_ot_hours,
    sick_leave_used, vacation_leave_used, emergency_leave_used, absent_days,
    company_loan_deduction, sss_loan_deduction, pagibig_loan_deduction, status) VALUES
('EMP001', '2026-04 1st Cutoff (1-15)', '2026-04-01', '2026-04-15', 10, 4, 0, 1, 0, 0, 1500, 0, 0, 'PENDING'),
('EMP002', '2026-04 1st Cutoff (1-15)', '2026-04-01', '2026-04-15', 10, 0, 0, 0, 0, 0, 0, 0, 0, 'PENDING');

INSERT INTO attendance (employee_id, attendance_date, time_in, time_out, hours_worked, status, late_minutes) VALUES
('EMP001', '2026-05-12', '08:00:00', '19:30:00', 11.50, 'PRESENT', 0),
('EMP001', '2026-05-13', '08:22:00', '17:05:00', 8.72, 'LATE', 22),
('EMP001', '2026-05-14', '08:00:00', '17:00:00', 9.00, 'PRESENT', 0),
('EMP001', '2026-05-15', '08:00:00', '17:00:00', 9.00, 'PRESENT', 0),
('EMP001', '2026-05-16', '08:00:00', '17:00:00', 9.00, 'PRESENT', 0),
('EMP002', '2026-05-12', '08:00:00', '17:00:00', 9.00, 'PRESENT', 0),
('EMP002', '2026-05-13', '08:00:00', '17:00:00', 9.00, 'PRESENT', 0),
('EMP003', '2026-05-12', '08:15:00', '17:00:00', 8.75, 'LATE', 15),
('EMP004', '2026-05-12', '08:00:00', '17:00:00', 9.00, 'PRESENT', 0),
('EMP005', '2026-05-12', '08:00:00', '12:00:00', 4.00, 'PRESENT', 0);
