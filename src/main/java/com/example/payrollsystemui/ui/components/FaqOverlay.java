package com.example.payrollsystemui.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.StageStyle;

public final class FaqOverlay {
	private FaqOverlay() {
	}

	public static void attach(StackPane root) {
		Button faq = new Button("?");
		faq.getStyleClass().add("faq-button");
		StackPane.setAlignment(faq, Pos.BOTTOM_RIGHT);
		StackPane.setMargin(faq, new Insets(0, 24, 24, 0));
		faq.setOnAction(e -> showFaq());
		root.getChildren().add(faq);
	}

	private static void showFaq() {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.initStyle(StageStyle.UTILITY);
		alert.setTitle("Payroll System FAQ");
		alert.setHeaderText("ABC Company Payroll Workflow");
		alert.setContentText("""
				1. Login with Employee ID and password.
				2. Employees clock in/out on the dashboard.
				3. File payroll per cutoff with OT hours, leave, and loan deductions.
				4. Admin reviews pending submissions and approves or rejects.
				5. Approved submissions unlock the official 3-column payslip.
				OT pay uses filed OT hours, not automatic punch overtime.
				""");
		alert.getDialogPane().getStylesheets().add(FaqOverlay.class.getResource("/theme/theme.css").toExternalForm());
		alert.getDialogPane().getStyleClass().add("my-dialog");
		alert.showAndWait();
	}
}
