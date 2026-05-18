package com.example.payrollsystemui.repository;

import com.example.payrollsystemui.database.DatabaseManager;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.EmployeeFactory;
import com.example.payrollsystemui.model.EmploymentType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeRepository {
	public List<Employee> findAll() throws SQLException {
		String sql = "SELECT * FROM employees ORDER BY full_name";
		List<Employee> list = new ArrayList<>();
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				list.add(EmployeeFactory.fromResultSet(rs));
			}
		}
		return list;
	}

	public Optional<Employee> findById(String employeeId) throws SQLException {
		String sql = "SELECT * FROM employees WHERE employee_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}
				return Optional.of(EmployeeFactory.fromResultSet(rs));
			}
		}
	}

	public void insert(Employee employee) throws SQLException {
		String sql = """
				INSERT INTO employees (employee_id, full_name, department, position_title, employment_type,
				basic_rate, work_schedule, sick_leave_balance, vacation_leave_balance, emergency_leave_balance,
				company_loan_balance, sss_loan_balance, pagibig_loan_balance, status)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			bindEmployee(ps, employee);
			ps.executeUpdate();
		}
	}

	public void updateBalances(String employeeId, double sick, double vacation, double emergency, double companyLoan,
			double sssLoan, double pagibigLoan) throws SQLException {
		String sql = """
				UPDATE employees SET sick_leave_balance=?, vacation_leave_balance=?, emergency_leave_balance=?,
				company_loan_balance=?, sss_loan_balance=?, pagibig_loan_balance=? WHERE employee_id=?
				""";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setDouble(1, sick);
			ps.setDouble(2, vacation);
			ps.setDouble(3, emergency);
			ps.setDouble(4, companyLoan);
			ps.setDouble(5, sssLoan);
			ps.setDouble(6, pagibigLoan);
			ps.setString(7, employeeId);
			ps.executeUpdate();
		}
	}

	public void delete(String employeeId) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE employee_id=?")) {
			ps.setString(1, employeeId);
			ps.executeUpdate();
		}
	}

	public int countActive() throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT COUNT(*) AS c FROM employees WHERE status='ACTIVE' AND employee_id <> 'ADMIN001'");
				ResultSet rs = ps.executeQuery()) {
			rs.next();
			return rs.getInt("c");
		}
	}

	public double averageBasicRate() throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement(
						"SELECT AVG(basic_rate) AS avg_rate FROM employees WHERE employee_id <> 'ADMIN001'");
				ResultSet rs = ps.executeQuery()) {
			rs.next();
			return rs.getDouble("avg_rate");
		}
	}

	private void bindEmployee(PreparedStatement ps, Employee e) throws SQLException {
		ps.setString(1, e.getEmployeeId());
		ps.setString(2, e.getFullName());
		ps.setString(3, e.getDepartment());
		ps.setString(4, e.getPositionTitle());
		ps.setString(5, e.getEmploymentType().name());
		ps.setDouble(6, e.getBasicRate());
		ps.setString(7, e.getWorkSchedule());
		ps.setDouble(8, e.getSickLeaveBalance());
		ps.setDouble(9, e.getVacationLeaveBalance());
		ps.setDouble(10, e.getEmergencyLeaveBalance());
		ps.setDouble(11, e.getCompanyLoanBalance());
		ps.setDouble(12, e.getSssLoanBalance());
		ps.setDouble(13, e.getPagibigLoanBalance());
		ps.setString(14, e.getStatus());
	}

	public Employee createNew(String employeeId, String fullName, String department, String positionTitle,
			EmploymentType type, double basicRate, String workSchedule, double sick, double vacation, double emergency,
			double companyLoan, double sssLoan, double pagibigLoan) {
		return EmployeeFactory.fromRow(employeeId, fullName, department, positionTitle, type, basicRate, workSchedule,
				sick, vacation, emergency, companyLoan, sssLoan, pagibigLoan, "ACTIVE");
	}
}
