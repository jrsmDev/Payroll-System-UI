package com.example.payrollsystemui.model;

public class PartTimeEmployee extends Employee {
	public PartTimeEmployee(String employeeId, String fullName, String department, String positionTitle,
			double basicRate, String workSchedule, double sickLeaveBalance, double vacationLeaveBalance,
			double emergencyLeaveBalance, double companyLoanBalance, double sssLoanBalance, double pagibigLoanBalance,
			String status) {
		super(employeeId, fullName, department, positionTitle, EmploymentType.PART_TIME, basicRate, workSchedule,
				sickLeaveBalance, vacationLeaveBalance, emergencyLeaveBalance, companyLoanBalance, sssLoanBalance,
				pagibigLoanBalance, status);
	}

	@Override
	public double getDailyRate() {
		return basicRate / 11.0;
	}

	@Override
	public double getHourlyRate() {
		return getDailyRate() / 4.0;
	}

	@Override
	public double overtimeMultiplier() {
		return 1.25;
	}
}
