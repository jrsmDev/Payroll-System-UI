package com.example.payrollsystemui.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceRecord(long id, String employeeId, String employeeName, LocalDate date, LocalTime timeIn,
		LocalTime timeOut, double hoursWorked, String status, int lateMinutes) {
}
