package com.example.payrollsystemui.model;

public record PayslipDetails(String employeeName, String employeeId, String cutoffLabel, double rate, double daysWorked,
		double regularOtPay, double specialHolidayPay, double nsdPay, double totalOtPay, double ecola, double allowance,
		double otherPay, double grossPay, double sssEmployee, double philHealthEmployee, double tax,
		double pagibigEmployee, double pagibigLoan, double sssLoan, double filedDeductions, double absenceLateDeduction,
		double totalDeduction, double netPay, double sssEmployer, double philHealthEmployer, double pagibigEmployer,
		double ecc) {
}
