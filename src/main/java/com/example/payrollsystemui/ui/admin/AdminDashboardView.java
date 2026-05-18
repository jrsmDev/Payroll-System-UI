package com.example.payrollsystemui.ui.admin;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.PayrollSubmission;
import com.example.payrollsystemui.model.SubmissionStatus;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.ui.components.StatCard;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminDashboardView extends VBox {
	public AdminDashboardView() {
		setSpacing(20);
		setPadding(new Insets(28));
		try {
			var payroll = AppContext.payroll();
			String welcome = Session.employee() != null ? Session.employee().getFullName() : "Admin";
			getChildren().add(new PageHeader("Admin Dashboard",
					"Welcome back, " + welcome + ". Here's your payroll overview.", null));

			HBox stats = new HBox(16);
			stats.getChildren().addAll(
					new StatCard("ACTIVE EMPLOYEES", String.valueOf(payroll.countActiveEmployees()), null),
					new StatCard("PENDING APPROVALS", String.valueOf(payroll.countPendingApprovals()),
							"stat-value-orange"),
					new StatCard("APPROVED THIS PERIOD", String.valueOf(payroll.countApprovedSubmissions()),
							"stat-value-blue"),
					new StatCard("TOTAL NET PAY", MoneyFormat.format(payroll.totalApprovedNetPay()),
							"stat-value-green"));
			for (var node : stats.getChildren()) {
				HBox.setHgrow(node, Priority.ALWAYS);
			}

			HBox bottom = new HBox(16);
			bottom.getChildren().addAll(buildSubmissionsCard(), buildEmployeesCard());
			HBox.setHgrow(bottom.getChildren().get(0), Priority.ALWAYS);
			HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);

			getChildren().addAll(stats, bottom);
		} catch (Exception ex) {
			getChildren().add(new Label("Failed to load dashboard: " + ex.getMessage()));
		}
	}

	private VBox buildSubmissionsCard() {
		VBox card = new VBox(12);
		card.getStyleClass().add("card");
		Label title = new Label("Recent Submissions");
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #388bfd;");
		javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
		card.getChildren().addAll(title, sep);
		try {
			java.util.List<PayrollSubmission> pending = AppContext.payroll().listSubmissions(SubmissionStatus.PENDING)
					.stream().limit(5).toList();
			if (pending.isEmpty()) {
				Label empty = new Label("No pending submissions.");
				empty.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 12px;");
				card.getChildren().add(empty);
			} else {
				for (PayrollSubmission s : pending) {
					HBox row = new HBox(10);
					row.setAlignment(Pos.CENTER_LEFT);
					row.setPadding(new Insets(4, 0, 4, 0));
					VBox text = new VBox(3);
					Label name = new Label(s.employeeName());
					name.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
					Label cutoff = new Label(s.cutoffLabel());
					cutoff.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 11px;");
					text.getChildren().addAll(name, cutoff);
					javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
					HBox.setHgrow(spacer, Priority.ALWAYS);
					Label badge = new Label("PENDING");
					badge.getStyleClass().add("badge-pending");
					row.getChildren().addAll(text, spacer, badge);
					card.getChildren().add(row);
				}
			}
		} catch (Exception ex) {
			Label err = new Label("Error: " + (ex.getMessage() != null ? ex.getMessage() : "unknown"));
			err.setStyle("-fx-text-fill: #f85149;");
			card.getChildren().add(err);
		}
		return card;
	}

	private VBox buildEmployeesCard() {
		VBox card = new VBox(12);
		card.getStyleClass().add("card");
		Label title = new Label("Employee Summary");
		title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #388bfd;");
		javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
		card.getChildren().addAll(title, sep);
		try {
			java.util.List<Employee> employees = AppContext.payroll().listEmployees().stream().limit(6).toList();
			if (employees.isEmpty()) {
				Label empty = new Label("No employees found.");
				empty.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 12px;");
				card.getChildren().add(empty);
			} else {
				for (Employee e : employees) {
					HBox row = new HBox(10);
					row.setAlignment(Pos.CENTER_LEFT);
					row.setPadding(new Insets(3, 0, 3, 0));
					String initial = e.getFullName().isEmpty() ? "?" : e.getFullName().substring(0, 1).toUpperCase();
					Label avatar = new Label(initial);
					avatar.setStyle("-fx-background-color: #1f6feb; -fx-background-radius: 16;"
							+ " -fx-min-width: 30; -fx-min-height: 30; -fx-max-width: 30; -fx-max-height: 30;"
							+ " -fx-alignment: center; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
					VBox text = new VBox(2);
					Label name = new Label(e.getFullName());
					name.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
					Label dept = new Label(e.getDepartment() + " · " + e.getEmploymentType().displayName());
					dept.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 11px;");
					text.getChildren().addAll(name, dept);
					HBox.setHgrow(text, Priority.ALWAYS);
					Label badge = new Label("Active");
					badge.getStyleClass().add("badge-active");
					row.getChildren().addAll(avatar, text, badge);
					card.getChildren().add(row);
				}
			}
		} catch (Exception ex) {
			Label err = new Label("Error: " + (ex.getMessage() != null ? ex.getMessage() : "unknown"));
			err.setStyle("-fx-text-fill: #f85149;");
			card.getChildren().add(err);
		}
		return card;
	}

	private static Label subtitle(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("page-subtitle");
		return label;
	}
}