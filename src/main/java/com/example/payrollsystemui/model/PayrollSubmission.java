package com.example.payrollsystemui.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PayrollSubmission(long id, String employeeId, String employeeName, String cutoffLabel,
		LocalDate cutoffStart, LocalDate cutoffEnd, double daysWorked, double filedOtHours, double sickLeaveUsed,
		double vacationLeaveUsed, double emergencyLeaveUsed, double absentDays, double companyLoanDeduction,
		double sssLoanDeduction, double pagibigLoanDeduction, SubmissionStatus status, LocalDateTime submittedAt) {
}
