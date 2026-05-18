package com.example.payrollsystemui.repository;

import com.example.payrollsystemui.database.DatabaseManager;
import com.example.payrollsystemui.model.User;
import com.example.payrollsystemui.model.UserRole;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AccountRepository {
	public Optional<User> findByEmployeeId(String employeeId) throws SQLException {
		String sql = "SELECT employee_id, password_hash, role FROM users WHERE employee_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}
				return Optional.of(new User(rs.getString("employee_id"), rs.getString("password_hash"),
						UserRole.valueOf(rs.getString("role"))));
			}
		}
	}

	public void createAccount(String employeeId, String plainPassword, UserRole role) throws SQLException {
		String sql = "INSERT INTO users (employee_id, password_hash, role) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			ps.setString(2, BCrypt.hashpw(plainPassword, BCrypt.gensalt(10)));
			ps.setString(3, role.name());
			ps.executeUpdate();
		}
	}

	public void deleteAccount(String employeeId) throws SQLException {
		String sql = "DELETE FROM users WHERE employee_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			ps.executeUpdate();
		}
	}
}
