package com.example.payrollsystemui.ui.employee;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.PayrollSubmission;
import com.example.payrollsystemui.model.SubmissionStatus;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.ui.components.StatCard;
import com.example.payrollsystemui.util.DateTimeUtil;
import com.example.payrollsystemui.util.MoneyFormat;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class EmployeeDashboardView extends VBox {
	private final Button clockButton = new Button();

	public EmployeeDashboardView() {
		setSpacing(20);
		setPadding(new Insets(28));
		setStyle("-fx-background-color: #0d1117;");

		Employee employee = Session.employee();

		// ── Header ────────────────────────────────────────────────
		getChildren().add(new PageHeader(DateTimeUtil.formatLongDate(LocalDate.now()),
				employee.getFullName() + " · " + employee.getDepartment() + " · " + employee.getPositionTitle(),
				buildClockButton()));

		// ── Stat cards row ────────────────────────────────────────
		HBox stats = new HBox(16);
		StatCard sickCard = new StatCard("Sick Leave", String.valueOf((int) employee.getSickLeaveBalance()), null);
		StatCard vacCard = new StatCard("Vacation Leave", String.valueOf((int) employee.getVacationLeaveBalance()),
				null);
		StatCard emergCard = new StatCard("Emergency Leave", String.valueOf((int) employee.getEmergencyLeaveBalance()),
				null);
		StatCard pendCard = pendingCard(employee.getEmployeeId());

		for (StatCard c : new StatCard[]{sickCard, vacCard, emergCard, pendCard}) {
			HBox.setHgrow(c, Priority.ALWAYS);
			stats.getChildren().add(c);
		}

		// ── Bottom two-column cards ───────────────────────────────
		HBox bottom = new HBox(16);
		VBox profileCard = buildProfile(employee);
		VBox submissionsCard = buildSubmissions(employee.getEmployeeId());
		HBox.setHgrow(profileCard, Priority.ALWAYS);
		HBox.setHgrow(submissionsCard, Priority.ALWAYS);
		bottom.getChildren().addAll(profileCard, submissionsCard);

		getChildren().addAll(stats, bottom);
		updateClockState();
	}

	/** Clock In / Out button shown in the page header. */
	private Button buildClockButton() {
		FontIcon icon = new FontIcon(FontAwesomeSolid.CLOCK);
		icon.setIconSize(14);
		icon.setIconColor(javafx.scene.paint.Color.WHITE);
		clockButton.setGraphic(icon);
		clockButton.getStyleClass().add("primary-button-blue");
		clockButton.setOnAction(e -> toggleClock());
		return clockButton;
	}

	private StatCard pendingCard(String employeeId) {
		try {
			long pending = AppContext.payroll().listEmployeeSubmissions(employeeId).stream()
					.filter(s -> s.status() == SubmissionStatus.PENDING).count();
			return new StatCard("Pending Submissions", String.valueOf(pending), "stat-value-orange");
		} catch (Exception ex) {
			return new StatCard("Pending Submissions", "0", "stat-value-orange");
		}
	}

	private VBox buildProfile(Employee e) {
		VBox card = new VBox(14);
		card.getStyleClass().add("card");

		Label title = new Label("My Profile");
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #388bfd;");

		Separator sep = new Separator();
		sep.setStyle("-fx-background-color: #21262d;");

		GridPane grid = new GridPane();
		grid.setHgap(24);
		grid.setVgap(10);
		addProfileRow(grid, 0, "Employee ID", e.getEmployeeId());
		addProfileRow(grid, 1, "Work Schedule", e.getWorkSchedule());
		addProfileRow(grid, 2, "Basic Rate", MoneyFormat.format(e.getBasicRate()));
		addProfileRow(grid, 3, "Daily Rate", MoneyFormat.format(e.getDailyRate()));
		addProfileRow(grid, 4, "Company Loan Balance", MoneyFormat.format(e.getCompanyLoanBalance()));
		addProfileRow(grid, 5, "SSS Loan Balance", MoneyFormat.format(e.getSssLoanBalance()));
		addProfileRow(grid, 6, "Pag-IBIG Loan Balance", MoneyFormat.format(e.getPagibigLoanBalance()));

		card.getChildren().addAll(title, sep, grid);
		return card;
	}

	private void addProfileRow(GridPane grid, int row, String key, String value) {
		Label k = new Label(key);
		k.setStyle("-fx-font-size: 12px; -fx-text-fill: #7d8590;");

		Label v = new Label(value);
		v.setStyle("-fx-font-size: 13px; -fx-text-fill: #e6edf3;");
		v.setAlignment(Pos.CENTER_RIGHT);
		GridPane.setHgrow(v, Priority.ALWAYS);
		grid.addRow(row, k, v);
	}

	private VBox buildSubmissions(String employeeId) {
		VBox card = new VBox(14);
		card.getStyleClass().add("card");

		Label title = new Label("Recent Payroll Submissions");
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #388bfd;");

		Separator sep = new Separator();
		sep.setStyle("-fx-background-color: #21262d;");

		card.getChildren().addAll(title, sep);

		try {
			java.util.List<PayrollSubmission> submissions = AppContext.payroll().listEmployeeSubmissions(employeeId)
					.stream().limit(5).toList();
			if (submissions.isEmpty()) {
				Label empty = new Label("No submissions yet.");
				empty.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 12px;");
				card.getChildren().add(empty);
			} else {
				for (PayrollSubmission s : submissions) {
					HBox row = new HBox(10);
					row.setAlignment(Pos.CENTER_LEFT);
					row.setPadding(new Insets(4, 0, 4, 0));

					VBox text = new VBox(3);
					Label cutoff = new Label(s.cutoffLabel());
					cutoff.setStyle("-fx-font-size: 13px; -fx-text-fill: #e6edf3;");
					Label dateRange = new Label(s.cutoffStart() + "  –  " + s.cutoffEnd());
					dateRange.setStyle("-fx-font-size: 11px; -fx-text-fill: #7d8590;");
					text.getChildren().addAll(cutoff, dateRange);

					Region spacer = new Region();
					HBox.setHgrow(spacer, Priority.ALWAYS);

					Label badge = new Label(s.status().name());
					badge.getStyleClass().add(switch (s.status()) {
						case PENDING -> "badge-pending";
						case APPROVED -> "badge-approved";
						case REJECTED -> "badge-rejected";
					});

					row.getChildren().addAll(text, spacer, badge);
					card.getChildren().add(row);
				}
			}
		} catch (Exception ex) {
			Label err = new Label("Could not load submissions.");
			err.setStyle("-fx-text-fill: #f85149; -fx-font-size: 12px;");
			card.getChildren().add(err);
		}
		return card;
	}

	private void toggleClock() {
		try {
			String id = Session.employee().getEmployeeId();
			if (AppContext.payroll().isClockedIn(id)) {
				AppContext.payroll().clockOut(id);
			} else {
				AppContext.payroll().clockIn(id);
			}
			updateClockState();
		} catch (Exception ex) {
			new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
		}
	}

	private void updateClockState() {
		try {
			boolean in = AppContext.payroll().isClockedIn(Session.employee().getEmployeeId());
			clockButton.setText(in ? "  Clock Out" : "  Clock In");
		} catch (Exception ex) {
			clockButton.setText("  Clock In");
		}
	}
}
