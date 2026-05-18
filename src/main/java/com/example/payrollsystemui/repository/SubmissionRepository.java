package com.example.payrollsystemui.repository;

import com.example.payrollsystemui.database.DatabaseManager;
import com.example.payrollsystemui.model.PayrollSubmission;
import com.example.payrollsystemui.model.SubmissionStatus;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmissionRepository {
	public List<PayrollSubmission> findAll(SubmissionStatus filter) throws SQLException {
		StringBuilder sql = new StringBuilder("""
				SELECT s.*, e.full_name AS employee_name FROM payroll_submissions s
				JOIN employees e ON s.employee_id = e.employee_id
				""");
		if (filter != null) {
			sql.append(" WHERE s.status = ?");
		}
		sql.append(" ORDER BY s.submitted_at DESC");
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql.toString())) {
			if (filter != null) {
				ps.setString(1, filter.name());
			}
			try (ResultSet rs = ps.executeQuery()) {
				List<PayrollSubmission> list = new ArrayList<>();
				while (rs.next()) {
					list.add(map(rs));
				}
				return list;
			}
		}
	}

	public List<PayrollSubmission> findByEmployee(String employeeId) throws SQLException {
		String sql = """
				SELECT s.*, e.full_name AS employee_name FROM payroll_submissions s
				JOIN employees e ON s.employee_id = e.employee_id
				WHERE s.employee_id = ? ORDER BY s.submitted_at DESC
				""";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			try (ResultSet rs = ps.executeQuery()) {
				List<PayrollSubmission> list = new ArrayList<>();
				while (rs.next()) {
					list.add(map(rs));
				}
				return list;
			}
		}
	}

	public Optional<PayrollSubmission> findById(long id) throws SQLException {
		String sql = """
				SELECT s.*, e.full_name AS employee_name FROM payroll_submissions s
				JOIN employees e ON s.employee_id = e.employee_id WHERE s.id = ?
				""";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}
				return Optional.of(map(rs));
			}
		}
	}

	public long create(PayrollSubmission submission) throws SQLException {
		String sql = """
				INSERT INTO payroll_submissions (employee_id, cutoff_label, cutoff_start, cutoff_end, days_worked,
				filed_ot_hours, sick_leave_used, vacation_leave_used, emergency_leave_used, absent_days,
				company_loan_deduction, sss_loan_deduction, pagibig_loan_deduction, status)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
				""";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
			bindSubmission(ps, submission);
			ps.executeUpdate();
			try (ResultSet keys = ps.getGeneratedKeys()) {
				keys.next();
				return keys.getLong(1);
			}
		}
	}

	public void approve(long submissionId) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				PayrollSubmission submission = findById(submissionId)
						.orElseThrow(() -> new SQLException("Submission not found"));
				if (submission.status() != SubmissionStatus.PENDING) {
					throw new SQLException("Submission is not pending");
				}
				try (PreparedStatement ps = conn
						.prepareStatement("SELECT * FROM employees WHERE employee_id = ? FOR UPDATE")) {
					ps.setString(1, submission.employeeId());
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							throw new SQLException("Employee not found");
						}
						double sick = rs.getDouble("sick_leave_balance") - submission.sickLeaveUsed();
						double vacation = rs.getDouble("vacation_leave_balance") - submission.vacationLeaveUsed();
						double emergency = rs.getDouble("emergency_leave_balance") - submission.emergencyLeaveUsed();
						double companyLoan = rs.getDouble("company_loan_balance") - submission.companyLoanDeduction();
						double sssLoan = rs.getDouble("sss_loan_balance") - submission.sssLoanDeduction();
						double pagibigLoan = rs.getDouble("pagibig_loan_balance") - submission.pagibigLoanDeduction();
						try (PreparedStatement update = conn.prepareStatement("""
								UPDATE employees SET sick_leave_balance=?, vacation_leave_balance=?,
								emergency_leave_balance=?, company_loan_balance=?, sss_loan_balance=?,
								pagibig_loan_balance=? WHERE employee_id=?
								""")) {
							update.setDouble(1, Math.max(0, sick));
							update.setDouble(2, Math.max(0, vacation));
							update.setDouble(3, Math.max(0, emergency));
							update.setDouble(4, Math.max(0, companyLoan));
							update.setDouble(5, Math.max(0, sssLoan));
							update.setDouble(6, Math.max(0, pagibigLoan));
							update.setString(7, submission.employeeId());
							update.executeUpdate();
						}
					}
				}
				try (PreparedStatement ps = conn.prepareStatement(
						"UPDATE payroll_submissions SET status='APPROVED', reviewed_at=NOW() WHERE id=?")) {
					ps.setLong(1, submissionId);
					ps.executeUpdate();
				}
				conn.commit();
			} catch (SQLException ex) {
				conn.rollback();
				throw ex;
			} finally {
				conn.setAutoCommit(true);
			}
		}
	}

	public void reject(long submissionId) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"UPDATE payroll_submissions SET status='REJECTED', reviewed_at=NOW() WHERE id=? AND status='PENDING'")) {
			ps.setLong(1, submissionId);
			ps.executeUpdate();
		}
	}

	public int countByStatus(SubmissionStatus status) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn
						.prepareStatement("SELECT COUNT(*) AS c FROM payroll_submissions WHERE status=?")) {
			ps.setString(1, status.name());
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt("c");
			}
		}
	}

	private void bindSubmission(PreparedStatement ps, PayrollSubmission s) throws SQLException {
		ps.setString(1, s.employeeId());
		ps.setString(2, s.cutoffLabel());
		ps.setDate(3, Date.valueOf(s.cutoffStart()));
		ps.setDate(4, Date.valueOf(s.cutoffEnd()));
		ps.setDouble(5, s.daysWorked());
		ps.setDouble(6, s.filedOtHours());
		ps.setDouble(7, s.sickLeaveUsed());
		ps.setDouble(8, s.vacationLeaveUsed());
		ps.setDouble(9, s.emergencyLeaveUsed());
		ps.setDouble(10, s.absentDays());
		ps.setDouble(11, s.companyLoanDeduction());
		ps.setDouble(12, s.sssLoanDeduction());
		ps.setDouble(13, s.pagibigLoanDeduction());
	}

	private PayrollSubmission map(ResultSet rs) throws SQLException {
		Timestamp submitted = rs.getTimestamp("submitted_at");
		return new PayrollSubmission(rs.getLong("id"), rs.getString("employee_id"), rs.getString("employee_name"),
				rs.getString("cutoff_label"), rs.getDate("cutoff_start").toLocalDate(),
				rs.getDate("cutoff_end").toLocalDate(), rs.getDouble("days_worked"), rs.getDouble("filed_ot_hours"),
				rs.getDouble("sick_leave_used"), rs.getDouble("vacation_leave_used"),
				rs.getDouble("emergency_leave_used"), rs.getDouble("absent_days"),
				rs.getDouble("company_loan_deduction"), rs.getDouble("sss_loan_deduction"),
				rs.getDouble("pagibig_loan_deduction"), SubmissionStatus.valueOf(rs.getString("status")),
				submitted == null ? null : submitted.toLocalDateTime());
	}
}
