package com.example.payrollsystemui.service;

import com.example.payrollsystemui.model.AttendanceRecord;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.EmploymentType;
import com.example.payrollsystemui.model.GovernmentContribution;
import com.example.payrollsystemui.model.PayrollPeriod;
import com.example.payrollsystemui.model.PayrollSubmission;
import com.example.payrollsystemui.model.PayslipDetails;
import com.example.payrollsystemui.model.SubmissionStatus;
import com.example.payrollsystemui.model.UserRole;
import com.example.payrollsystemui.repository.AccountRepository;
import com.example.payrollsystemui.repository.AttendanceRepository;
import com.example.payrollsystemui.repository.EmployeeRepository;
import com.example.payrollsystemui.repository.SubmissionRepository;
import com.example.payrollsystemui.util.MockAttendanceGenerator;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PayrollService {
	private final AccountRepository accountRepository = new AccountRepository();
	private final EmployeeRepository employeeRepository = new EmployeeRepository();
	private final AttendanceRepository attendanceRepository = new AttendanceRepository();
	private final SubmissionRepository submissionRepository = new SubmissionRepository();
	private final MockAttendanceGenerator mockAttendanceGenerator = new MockAttendanceGenerator();

	public List<Employee> listEmployees() throws SQLException {
		return employeeRepository.findAll().stream().filter(e -> !"ADMIN001".equals(e.getEmployeeId())).toList();
	}

	public List<Employee> listAllEmployeesIncludingAdmin() throws SQLException {
		return employeeRepository.findAll();
	}

	public Optional<Employee> findEmployee(String id) throws SQLException {
		return employeeRepository.findById(id);
	}

	public void addEmployee(String employeeId, String fullName, String department, String position, EmploymentType type,
			double basicRate, String workSchedule, double sick, double vacation, double emergency, double companyLoan,
			double sssLoan, double pagibigLoan, String defaultPassword) throws SQLException {
		Employee employee = employeeRepository.createNew(employeeId, fullName, department, position, type, basicRate,
				workSchedule, sick, vacation, emergency, companyLoan, sssLoan, pagibigLoan);
		accountRepository.createAccount(employeeId, defaultPassword, UserRole.EMPLOYEE);
		employeeRepository.insert(employee);
	}

	public void deleteEmployee(String employeeId) throws SQLException {
		accountRepository.deleteAccount(employeeId);
	}

	public List<AttendanceRecord> listAttendance(String search, LocalDate date) throws SQLException {
		return attendanceRepository.findAll(search, date);
	}

	public List<AttendanceRecord> listEmployeeAttendance(String employeeId, int year, int month) throws SQLException {
		return attendanceRepository.findByEmployee(employeeId, year, month);
	}

	public boolean isClockedIn(String employeeId) throws SQLException {
		return attendanceRepository.getActiveClockIn(employeeId).isPresent();
	}

	public void clockIn(String employeeId) throws SQLException {
		if (isClockedIn(employeeId)) {
			throw new IllegalStateException("Already clocked in.");
		}
		attendanceRepository.startClockSession(employeeId, LocalDateTime.now());
	}

	public void clockOut(String employeeId) throws SQLException {
		LocalDateTime clockIn = attendanceRepository.getActiveClockIn(employeeId)
				.orElseThrow(() -> new IllegalStateException("Not clocked in."));
		LocalDateTime now = LocalDateTime.now();
		LocalDate date = clockIn.toLocalDate();
		LocalTime in = clockIn.toLocalTime();
		LocalTime out = now.toLocalTime();
		double hours = Duration.between(clockIn, now).toMinutes() / 60.0;
		int lateMinutes = in.isAfter(LocalTime.of(8, 0))
				? (int) Duration.between(LocalTime.of(8, 0), in).toMinutes()
				: 0;
		String status = lateMinutes > 0 ? "LATE" : "PRESENT";
		attendanceRepository.upsertAttendance(employeeId, date, in, out, hours, status, lateMinutes);
		attendanceRepository.clearClockSession(employeeId);
	}

	public List<PayrollSubmission> listSubmissions(SubmissionStatus filter) throws SQLException {
		return submissionRepository.findAll(filter);
	}

	public List<PayrollSubmission> listEmployeeSubmissions(String employeeId) throws SQLException {
		return submissionRepository.findByEmployee(employeeId);
	}

	public void fileSubmission(String employeeId, PayrollPeriod.Period period, double daysWorked, double filedOt,
			double sick, double vacation, double emergency, double absent, double companyLoan, double sssLoan,
			double pagibigLoan) throws SQLException {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new SQLException("Employee not found"));
		InputValidator.validateSubmission(employee, sick, vacation, emergency, companyLoan, sssLoan, pagibigLoan);
		PayrollSubmission submission = new PayrollSubmission(0, employeeId, employee.getFullName(), period.label(),
				period.start(), period.end(), daysWorked, filedOt, sick, vacation, emergency, absent, companyLoan,
				sssLoan, pagibigLoan, SubmissionStatus.PENDING, LocalDateTime.now());
		submissionRepository.create(submission);
	}

	public void approveSubmission(long id) throws SQLException {
		submissionRepository.approve(id);
	}

	public void rejectSubmission(long id) throws SQLException {
		submissionRepository.reject(id);
	}

	public PayslipDetails buildPayslip(long submissionId) throws SQLException {
		PayrollSubmission submission = submissionRepository.findById(submissionId)
				.orElseThrow(() -> new SQLException("Submission not found"));
		if (submission.status() != SubmissionStatus.APPROVED) {
			throw new IllegalStateException("Payslip available only for approved submissions.");
		}
		Employee employee = employeeRepository.findById(submission.employeeId())
				.orElseThrow(() -> new SQLException("Employee not found"));
		double grossEstimate = employee.calculateBasicPay(submission.daysWorked())
				+ employee.calculateOvertimePay(submission.filedOtHours());
		GovernmentContribution gov = ContributionCalculator.compute(employee.getBasicRate(), grossEstimate);
		return employee.buildPayslip(submission.cutoffLabel(), submission.daysWorked(), submission.filedOtHours(),
				submission.absentDays(), submission.companyLoanDeduction(), submission.sssLoanDeduction(),
				submission.pagibigLoanDeduction(), gov);
	}

	public void generateMockAttendance(String employeeId, PayrollPeriod.Period period) throws SQLException {
		mockAttendanceGenerator.generate(employeeId, period);
	}

	public int countPendingApprovals() throws SQLException {
		return submissionRepository.countByStatus(SubmissionStatus.PENDING);
	}

	public int countApprovedSubmissions() throws SQLException {
		return submissionRepository.countByStatus(SubmissionStatus.APPROVED);
	}

	public int countActiveEmployees() throws SQLException {
		return employeeRepository.countActive();
	}

	public double averageBasicRate() throws SQLException {
		return employeeRepository.averageBasicRate();
	}

	public double totalApprovedNetPay() throws SQLException {
		double total = 0;
		for (PayrollSubmission s : submissionRepository.findAll(SubmissionStatus.APPROVED)) {
			total += buildPayslip(s.id()).netPay();
		}
		return total;
	}

	public Map<EmploymentType, Long> employmentTypeDistribution() throws SQLException {
		Map<EmploymentType, Long> map = new HashMap<>();
		for (Employee e : listEmployees()) {
			map.merge(e.getEmploymentType(), 1L, Long::sum);
		}
		return map;
	}

	public List<PayslipDetails> governmentContributionReport() throws SQLException {
		return submissionRepository.findAll(SubmissionStatus.APPROVED).stream().map(s -> {
			try {
				return buildPayslip(s.id());
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}).toList();
	}

	public AttendanceSummary attendanceSummary(String employeeId, int year, int month) throws SQLException {
		List<AttendanceRecord> records = attendanceRepository.findByEmployee(employeeId, year, month);
		double hours = records.stream().mapToDouble(AttendanceRecord::hoursWorked).sum();
		int lateDays = attendanceRepository.countLateDays(employeeId, year, month);
		return new AttendanceSummary(records.size(), hours, lateDays);
	}

	public record AttendanceSummary(int daysPresent, double totalHours, int lateDays) {
	}
}
