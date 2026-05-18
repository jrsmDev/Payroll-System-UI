package com.example.payrollsystemui.util;

import com.example.payrollsystemui.model.PayrollPeriod;
import com.example.payrollsystemui.repository.AttendanceRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

public final class MockAttendanceGenerator {
	private final AttendanceRepository attendanceRepository = new AttendanceRepository();
	private final Random random = new Random();

	public void generate(String employeeId, PayrollPeriod.Period period) throws SQLException {
		LocalDate date = period.start();
		while (!date.isAfter(period.end())) {
			if (date.getDayOfWeek().getValue() <= 5) {
				int late = random.nextInt(3) == 0 ? 15 : 0;
				LocalTime in = LocalTime.of(8, late);
				LocalTime out = LocalTime.of(17, 0);
				double hours = 9.0 - (late / 60.0);
				String status = late > 0 ? "LATE" : "PRESENT";
				attendanceRepository.upsertAttendance(employeeId, date, in, out, hours, status, late);
			}
			date = date.plusDays(1);
		}
	}
}
