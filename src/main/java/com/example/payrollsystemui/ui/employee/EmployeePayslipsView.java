package com.example.payrollsystemui.ui.employee;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.model.PayrollSubmission;
import com.example.payrollsystemui.model.SubmissionStatus;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.ui.components.StatCard;
import com.example.payrollsystemui.ui.payslip.PayslipDialog;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class EmployeePayslipsView extends VBox {
	public EmployeePayslipsView() {
		setSpacing(16);
		setPadding(new Insets(28));
		getChildren().add(new PageHeader("My Payslips", "View and download your approved payslips", null));
		try {
			String id = Session.employee().getEmployeeId();
			var submissions = AppContext.payroll().listEmployeeSubmissions(id);
			long approved = submissions.stream().filter(s -> s.status() == SubmissionStatus.APPROVED).count();
			long pending = submissions.stream().filter(s -> s.status() == SubmissionStatus.PENDING).count();
			double net = 0;
			for (PayrollSubmission s : submissions) {
				if (s.status() == SubmissionStatus.APPROVED) {
					net += AppContext.payroll().buildPayslip(s.id()).netPay();
				}
			}
			HBox stats = new HBox(16);
			stats.getChildren().addAll(new StatCard("Total Payslips", String.valueOf(approved), null),
					new StatCard("Total Net Pay Received", MoneyFormat.format(net), "stat-value-green"),
					new StatCard("Pending", String.valueOf(pending), "stat-value-orange"));
			for (var n : stats.getChildren()) {
				HBox.setHgrow(n, Priority.ALWAYS);
			}
			getChildren().add(stats);
			for (PayrollSubmission s : submissions) {
				getChildren().add(buildRow(s));
			}
		} catch (Exception ex) {
			getChildren().add(new Label(ex.getMessage()));
		}
	}

	private VBox buildRow(PayrollSubmission s) {
		VBox card = new VBox();
		card.getStyleClass().add("card");
		HBox row = new HBox(16);
		row.setAlignment(Pos.CENTER_LEFT);
		Label icon = new Label("⏱");
		icon.setStyle(
				"-fx-background-color: rgba(249,115,22,0.2); -fx-background-radius: 24; -fx-padding: 12; -fx-font-size: 18;");
		VBox text = new VBox(4);
		text.getChildren().addAll(new Label(s.cutoffLabel()),
				subtitle("Days: " + s.daysWorked() + "  OT: " + s.filedOtHours() + "h"));
		Label badge = new Label(s.status().name());
		badge.getStyleClass().add(s.status() == SubmissionStatus.PENDING ? "badge-pending" : "badge-active");
		row.getChildren().addAll(icon, text, badge);
		HBox.setHgrow(text, Priority.ALWAYS);
		card.getChildren().add(row);
		if (s.status() == SubmissionStatus.APPROVED) {
			card.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
				try {
					PayslipDialog.show(AppContext.payroll().buildPayslip(s.id()));
				} catch (Exception ex) {
					new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, ex.getMessage())
							.showAndWait();
				}
			});
			card.setStyle("-fx-cursor: hand;");
		}
		return card;
	}

	private Label subtitle(String t) {
		Label l = new Label(t);
		l.getStyleClass().add("page-subtitle");
		return l;
	}
}
