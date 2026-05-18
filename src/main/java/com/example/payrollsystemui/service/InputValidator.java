package com.example.payrollsystemui.service;

import com.example.payrollsystemui.model.Employee;

public final class InputValidator {
	private InputValidator() {
	}

	public static void requireNonBlank(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " is required.");
		}
	}

	public static double requireNonNegative(String text, String field) {
		try {
			double val = Double.parseDouble(text.trim());
			if (val < 0) {
				throw new IllegalArgumentException(field + " cannot be negative.");
			}
			return val;
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(field + " must be a valid number.");
		}
	}

	public static void validateSubmission(Employee employee, double sickUsed, double vacationUsed, double emergencyUsed,
			double companyLoan, double sssLoan, double pagibigLoan) {
		if (sickUsed > employee.getSickLeaveBalance()) {
			throw new IllegalArgumentException("Sick leave used exceeds balance.");
		}
		if (vacationUsed > employee.getVacationLeaveBalance()) {
			throw new IllegalArgumentException("Vacation leave used exceeds balance.");
		}
		if (emergencyUsed > employee.getEmergencyLeaveBalance()) {
			throw new IllegalArgumentException("Emergency leave used exceeds balance.");
		}
		if (companyLoan > employee.getCompanyLoanBalance()) {
			throw new IllegalArgumentException("Company loan deduction exceeds balance.");
		}
		if (sssLoan > employee.getSssLoanBalance()) {
			throw new IllegalArgumentException("SSS loan deduction exceeds balance.");
		}
		if (pagibigLoan > employee.getPagibigLoanBalance()) {
			throw new IllegalArgumentException("Pag-IBIG loan deduction exceeds balance.");
		}
	}
}
