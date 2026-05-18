package com.example.payrollsystemui.repository;

import com.example.payrollsystemui.database.DatabaseManager;
import com.example.payrollsystemui.model.AttendanceRecord;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceRepository {
	public List<AttendanceRecord> findAll(String search, LocalDate filterDate) throws SQLException {
		StringBuilder sql = new StringBuilder("""
				SELECT a.id, a.employee_id, e.full_name, a.attendance_date, a.time_in, a.time_out,
				a.hours_worked, a.status, a.late_minutes
				FROM attendance a JOIN employees e ON a.employee_id = e.employee_id
				WHERE 1=1
				""");
		List<Object> params = new ArrayList<>();
		if (search != null && !search.isBlank()) {
			sql.append(" AND (e.full_name LIKE ? OR a.employee_id LIKE ?)");
			String like = "%" + search.trim() + "%";
			params.add(like);
			params.add(like);
		}
		if (filterDate != null) {
			sql.append(" AND a.attendance_date = ?");
			params.add(Date.valueOf(filterDate));
		}
		sql.append(" ORDER BY a.attendance_date DESC, e.full_name");
		return queryRecords(sql.toString(), params);
	}

	public List<AttendanceRecord> findByEmployee(String employeeId, int year, int month) throws SQLException {
		String sql = """
				SELECT a.id, a.employee_id, e.full_name, a.attendance_date, a.time_in, a.time_out,
				a.hours_worked, a.status, a.late_minutes
				FROM attendance a JOIN employees e ON a.employee_id = e.employee_id
				WHERE a.employee_id = ? AND YEAR(a.attendance_date)=? AND MONTH(a.attendance_date)=?
				ORDER BY a.attendance_date DESC
				""";
		List<Object> params = List.of(employeeId, year, month);
		return queryRecords(sql, params);
	}

	public void upsertAttendance(String employeeId, LocalDate date, LocalTime timeIn, LocalTime timeOut, double hours,
			String status, int lateMinutes) throws SQLException {
		String sql = """
				INSERT INTO attendance (employee_id, attendance_date, time_in, time_out, hours_worked, status, late_minutes)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				ON DUPLICATE KEY UPDATE time_in=VALUES(time_in), time_out=VALUES(time_out),
				hours_worked=VALUES(hours_worked), status=VALUES(status), late_minutes=VALUES(late_minutes)
				""";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			ps.setDate(2, Date.valueOf(date));
			ps.setTime(3, timeIn == null ? null : Time.valueOf(timeIn));
			ps.setTime(4, timeOut == null ? null : Time.valueOf(timeOut));
			ps.setDouble(5, hours);
			ps.setString(6, status);
			ps.setInt(7, lateMinutes);
			ps.executeUpdate();
		}
	}

	public Optional<LocalDateTime> getActiveClockIn(String employeeId) throws SQLException {
		String sql = "SELECT clock_in_time FROM active_clock_sessions WHERE employee_id=?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}
				return Optional.of(rs.getTimestamp("clock_in_time").toLocalDateTime());
			}
		}
	}

	public void startClockSession(String employeeId, LocalDateTime clockIn) throws SQLException {
		String sql = "INSERT INTO active_clock_sessions (employee_id, clock_in_time, attendance_date) VALUES (?, ?, ?)"
				+ " ON DUPLICATE KEY UPDATE clock_in_time=VALUES(clock_in_time), attendance_date=VALUES(attendance_date)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(clockIn));
			ps.setDate(3, Date.valueOf(clockIn.toLocalDate()));
			ps.executeUpdate();
		}
	}

	public void clearClockSession(String employeeId) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement ps = conn.prepareStatement("DELETE FROM active_clock_sessions WHERE employee_id=?")) {
			ps.setString(1, employeeId);
			ps.executeUpdate();
		}
	}

	public double sumHoursForEmployeeInPeriod(String employeeId, LocalDate start, LocalDate end) throws SQLException {
		String sql = "SELECT COALESCE(SUM(hours_worked),0) AS total FROM attendance WHERE employee_id=? AND attendance_date BETWEEN ? AND ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			ps.setDate(2, Date.valueOf(start));
			ps.setDate(3, Date.valueOf(end));
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getDouble("total");
			}
		}
	}

	public int countLateDays(String employeeId, int year, int month) throws SQLException {
		String sql = """
				SELECT COUNT(*) AS c FROM attendance
				WHERE employee_id=? AND YEAR(attendance_date)=? AND MONTH(attendance_date)=? AND status='LATE'
				""";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, employeeId);
			ps.setInt(2, year);
			ps.setInt(3, month);
			try (ResultSet rs = ps.executeQuery()) {
				rs.next();
				return rs.getInt("c");
			}
		}
	}

	private List<AttendanceRecord> queryRecords(String sql, List<Object> params) throws SQLException {
		List<AttendanceRecord> list = new ArrayList<>();
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.size(); i++) {
				ps.setObject(i + 1, params.get(i));
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(map(rs));
				}
			}
		}
		return list;
	}

	private AttendanceRecord map(ResultSet rs) throws SQLException {
		return new AttendanceRecord(rs.getLong("id"), rs.getString("employee_id"), rs.getString("full_name"),
				rs.getDate("attendance_date").toLocalDate(),
				rs.getTime("time_in") == null ? null : rs.getTime("time_in").toLocalTime(),
				rs.getTime("time_out") == null ? null : rs.getTime("time_out").toLocalTime(),
				rs.getDouble("hours_worked"), rs.getString("status"), rs.getInt("late_minutes"));
	}
}
