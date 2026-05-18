package com.example.payrollsystemui.ui.employee;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.PayrollPeriod;
import com.example.payrollsystemui.service.InputValidator;
import com.example.payrollsystemui.ui.components.PageHeader;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class EmployeeFilePayrollView extends VBox {
	public EmployeeFilePayrollView() {
		setSpacing(16);
		setPadding(new Insets(28));
		getChildren().add(new PageHeader("File Payroll", "Submit your payroll for the current cutoff period", null));

		Employee employee = Session.employee();
		ComboBox<PayrollPeriod.Period> cutoff = new ComboBox<>(
				FXCollections.observableArrayList(PayrollPeriod.recentPeriods(6)));
		cutoff.getSelectionModel().selectFirst();
		cutoff.setMaxWidth(Double.MAX_VALUE);

		TextField ot = new TextField("0");
		TextField sick = new TextField("0");
		TextField vacation = new TextField("0");
		TextField emergency = new TextField("0");
		TextField absent = new TextField("0");
		TextField companyLoan = new TextField("0");
		TextField sssLoan = new TextField("0");
		TextField pagibigLoan = new TextField("0");
		TextField daysWorked = new TextField("10");

		getChildren().addAll(section("CUTOFF PERIOD", cutoff),
				section("FILING OF OVERTIME (OT)", field("OT Hours Worked This Cutoff", ot),
						helper("OT rate: 1.25x your hourly rate. File exact hours worked beyond 8hrs/day.")),
				section("LEAVE USED", row(
						field("Sick Leave (" + (int) employee.getSickLeaveBalance() + " avail)", sick),
						field("Vacation Leave (" + (int) employee.getVacationLeaveBalance() + " avail)",
								vacation),
						field("Emergency Leave (" + (int) employee.getEmergencyLeaveBalance() + " avail)", emergency)),
						field("Absent Days (without pay)", absent)),
				section("LOAN DEDUCTIONS (PHP)",
						row(field("Company Loan (Bal: "
								+ com.example.payrollsystemui.util.MoneyFormat.format(employee.getCompanyLoanBalance())
								+ ")", companyLoan),
								field("SSS Loan (Bal: " + com.example.payrollsystemui.util.MoneyFormat
										.format(employee.getSssLoanBalance()) + ")", sssLoan),
								field("Pag-IBIG Loan (Bal: " + com.example.payrollsystemui.util.MoneyFormat
										.format(employee.getPagibigLoanBalance()) + ")", pagibigLoan))),
				field("Days Worked This Cutoff", daysWorked));

		Button submit = new Button("File Payroll Submission");
		submit.getStyleClass().add("primary-button");
		submit.setMaxWidth(Double.MAX_VALUE);
		submit.setOnAction(e -> {
			try {
				PayrollPeriod.Period period = cutoff.getValue();
				if (period == null) {
					throw new IllegalArgumentException("Select a cutoff period.");
				}
				double sickVal = InputValidator.requireNonNegative(sick.getText(), "Sick leave");
				double vacVal = InputValidator.requireNonNegative(vacation.getText(), "Vacation leave");
				double elVal = InputValidator.requireNonNegative(emergency.getText(), "Emergency leave");
				double comp = InputValidator.requireNonNegative(companyLoan.getText(), "Company loan");
				double sss = InputValidator.requireNonNegative(sssLoan.getText(), "SSS loan");
				double pag = InputValidator.requireNonNegative(pagibigLoan.getText(), "Pag-IBIG loan");
				InputValidator.validateSubmission(employee, sickVal, vacVal, elVal, comp, sss, pag);
				AppContext.payroll().fileSubmission(employee.getEmployeeId(), period,
						InputValidator.requireNonNegative(daysWorked.getText(), "Days worked"),
						InputValidator.requireNonNegative(ot.getText(), "OT hours"), sickVal, vacVal, elVal,
						InputValidator.requireNonNegative(absent.getText(), "Absent days"), comp, sss, pag);
				new Alert(Alert.AlertType.INFORMATION, "Payroll submission filed.").showAndWait();
			} catch (Exception ex) {
				new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
			}
		});
		getChildren().add(submit);
	}

	private VBox section(String title, javafx.scene.Node... nodes) {
		VBox box = new VBox(10);
		box.getStyleClass().add("card");
		Label header = new Label(title);
		header.getStyleClass().add("section-header-blue");
		box.getChildren().add(header);
		box.getChildren().addAll(nodes);
		return box;
	}

	private VBox field(String label, TextField input) {
		VBox box = new VBox(6);
		Label l = new Label(label);
		l.getStyleClass().add("page-subtitle");
		input.getStyleClass().add("text-field");
		box.getChildren().addAll(l, input);
		return box;
	}

	private Label helper(String text) {
		Label l = new Label(text);
		l.getStyleClass().add("page-subtitle");
		return l;
	}

	private HBox row(VBox... cols) {
		HBox row = new HBox(12, cols);
		for (VBox col : cols) {
			HBox.setHgrow(col, Priority.ALWAYS);
		}
		return row;
	}
}
