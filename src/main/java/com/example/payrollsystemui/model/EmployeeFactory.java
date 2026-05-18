package com.example.payrollsystemui.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class EmployeeFactory {
	private EmployeeFactory() {
	}

	public static Employee fromResultSet(ResultSet rs) throws SQLException {
		EmploymentType type = EmploymentType.fromDb(rs.getString("employment_type"));
		return fromRow(rs.getString("employee_id"), rs.getString("full_name"), rs.getString("department"),
				rs.getString("position_title"), type, rs.getDouble("basic_rate"), rs.getString("work_schedule"),
				rs.getDouble("sick_leave_balance"), rs.getDouble("vacation_leave_balance"),
				rs.getDouble("emergency_leave_balance"), rs.getDouble("company_loan_balance"),
				rs.getDouble("sss_loan_balance"), rs.getDouble("pagibig_loan_balance"), rs.getString("status"));
	}

	public static Employee fromRow(String employeeId, String fullName, String department, String positionTitle,
			EmploymentType type, double basicRate, String workSchedule, double sickLeave, double vacationLeave,
			double emergencyLeave, double companyLoan, double sssLoan, double pagibigLoan, String status) {
		return switch (type) {
			case REGULAR -> new RegularEmployee(employeeId, fullName, department, positionTitle, basicRate,
					workSchedule, sickLeave, vacationLeave, emergencyLeave, companyLoan, sssLoan, pagibigLoan, status);
			case CONTRACTUAL -> new ContractualEmployee(employeeId, fullName, department, positionTitle, basicRate,
					workSchedule, sickLeave, vacationLeave, emergencyLeave, companyLoan, sssLoan, pagibigLoan, status);
			case PART_TIME -> new PartTimeEmployee(employeeId, fullName, department, positionTitle, basicRate,
					workSchedule, sickLeave, vacationLeave, emergencyLeave, companyLoan, sssLoan, pagibigLoan, status);
			case PROBATIONARY -> new ProbationaryEmployee(employeeId, fullName, department, positionTitle, basicRate,
					workSchedule, sickLeave, vacationLeave, emergencyLeave, companyLoan, sssLoan, pagibigLoan, status);
		};
	}
}
