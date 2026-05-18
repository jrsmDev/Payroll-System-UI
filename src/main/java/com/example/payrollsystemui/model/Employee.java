package com.example.payrollsystemui.model;

public abstract class Employee {
	protected final String employeeId;
	protected final String fullName;
	protected final String department;
	protected final String positionTitle;
	protected final EmploymentType employmentType;
	protected final double basicRate;
	protected final String workSchedule;
	protected double sickLeaveBalance;
	protected double vacationLeaveBalance;
	protected double emergencyLeaveBalance;
	protected double companyLoanBalance;
	protected double sssLoanBalance;
	protected double pagibigLoanBalance;
	protected final String status;

	protected Employee(String employeeId, String fullName, String department, String positionTitle,
			EmploymentType employmentType, double basicRate, String workSchedule, double sickLeaveBalance,
			double vacationLeaveBalance, double emergencyLeaveBalance, double companyLoanBalance, double sssLoanBalance,
			double pagibigLoanBalance, String status) {
		this.employeeId = employeeId;
		this.fullName = fullName;
		this.department = department;
		this.positionTitle = positionTitle;
		this.employmentType = employmentType;
		this.basicRate = basicRate;
		this.workSchedule = workSchedule;
		this.sickLeaveBalance = sickLeaveBalance;
		this.vacationLeaveBalance = vacationLeaveBalance;
		this.emergencyLeaveBalance = emergencyLeaveBalance;
		this.companyLoanBalance = companyLoanBalance;
		this.sssLoanBalance = sssLoanBalance;
		this.pagibigLoanBalance = pagibigLoanBalance;
		this.status = status;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDepartment() {
		return department;
	}

	public String getPositionTitle() {
		return positionTitle;
	}

	public EmploymentType getEmploymentType() {
		return employmentType;
	}

	public double getBasicRate() {
		return basicRate;
	}

	public String getWorkSchedule() {
		return workSchedule;
	}

	public double getSickLeaveBalance() {
		return sickLeaveBalance;
	}

	public double getVacationLeaveBalance() {
		return vacationLeaveBalance;
	}

	public double getEmergencyLeaveBalance() {
		return emergencyLeaveBalance;
	}

	public double getCompanyLoanBalance() {
		return companyLoanBalance;
	}

	public double getSssLoanBalance() {
		return sssLoanBalance;
	}

	public double getPagibigLoanBalance() {
		return pagibigLoanBalance;
	}

	public String getStatus() {
		return status;
	}

	public abstract double getDailyRate();

	public abstract double getHourlyRate();

	public abstract double overtimeMultiplier();

	public double calculateBasicPay(double daysWorked) {
		return getDailyRate() * daysWorked;
	}

	public double calculateOvertimePay(double filedOtHours) {
		return getHourlyRate() * overtimeMultiplier() * filedOtHours;
	}

	public double calculateAbsenceDeduction(double absentDays) {
		return getDailyRate() * absentDays;
	}

	public PayslipDetails buildPayslip(String cutoffLabel, double daysWorked, double filedOtHours, double absentDays,
			double companyLoanDed, double sssLoanDed, double pagibigLoanDed, GovernmentContribution gov) {
		double rate = getDailyRate();
		double regularOt = calculateOvertimePay(filedOtHours);
		double specialHoliday = 0;
		double nsd = 0;
		double totalOt = regularOt + specialHoliday + nsd;
		double ecola = employmentType == EmploymentType.REGULAR ? 500 : 0;
		double allowance = 0;
		double otherPay = 0;
		double basic = calculateBasicPay(daysWorked);
		double gross = basic + totalOt + ecola + allowance + otherPay;
		double absenceLate = calculateAbsenceDeduction(absentDays);
		double filedDeductions = companyLoanDed + sssLoanDed + pagibigLoanDed;
		double totalDeduction = gov.sssEmployee() + gov.philHealthEmployee() + gov.tax() + gov.pagibigEmployee()
				+ pagibigLoanDed + sssLoanDed + filedDeductions + absenceLate;
		double net = gross - totalDeduction;
		return new PayslipDetails(fullName, employeeId, cutoffLabel, rate, daysWorked, regularOt, specialHoliday, nsd,
				totalOt, ecola, allowance, otherPay, gross, gov.sssEmployee(), gov.philHealthEmployee(), gov.tax(),
				gov.pagibigEmployee(), pagibigLoanDed, sssLoanDed, filedDeductions, absenceLate, totalDeduction, net,
				gov.sssEmployer(), gov.philHealthEmployer(), gov.pagibigEmployer(), gov.ecc());
	}
}
