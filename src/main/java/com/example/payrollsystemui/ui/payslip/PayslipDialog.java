package com.example.payrollsystemui.ui.payslip;

import com.example.payrollsystemui.model.PayslipDetails;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class PayslipDialog {
	private PayslipDialog() {
	}

	public static void show(PayslipDetails details) {
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.DECORATED);
		stage.setTitle("Payslip - " + details.cutoffLabel());

		HBox columns = new HBox(16);
		columns.setPadding(new Insets(20));
		columns.getChildren().addAll(buildColumn("Regular and Overtime Pay", new String[][]{
				{"Rate", MoneyFormat.format(details.rate())}, {"No. of Days", String.valueOf(details.daysWorked())},
				{"Regular OT", MoneyFormat.format(details.regularOtPay())},
				{"Special Holidays", MoneyFormat.format(details.specialHolidayPay())},
				{"NSD", MoneyFormat.format(details.nsdPay())}, {"Total OT", MoneyFormat.format(details.totalOtPay())},
				{"ECOLA", MoneyFormat.format(details.ecola())}, {"Allowance", MoneyFormat.format(details.allowance())},
				{"Other Pay", MoneyFormat.format(details.otherPay())},
				{"Gross Pay", MoneyFormat.format(details.grossPay())}}),
				buildColumn("Employee Contribution",
						new String[][]{{"SSS", MoneyFormat.format(details.sssEmployee())},
								{"PhilHealth", MoneyFormat.format(details.philHealthEmployee())},
								{"Tax", MoneyFormat.format(details.tax())},
								{"Pag-IBIG Fund", MoneyFormat.format(details.pagibigEmployee())},
								{"Pag-IBIG Loan", MoneyFormat.format(details.pagibigLoan())},
								{"SSS Loan", MoneyFormat.format(details.sssLoan())},
								{"Deduction (Submissions)", MoneyFormat.format(details.filedDeductions())},
								{"Other Deduction", MoneyFormat.format(details.absenceLateDeduction())},
								{"Total Deduction", MoneyFormat.format(details.totalDeduction())},
								{"Net Pay", MoneyFormat.format(details.netPay())}}),
				buildColumn("Employer Contribution",
						new String[][]{{"SSS", MoneyFormat.format(details.sssEmployer())},
								{"PhilHealth", MoneyFormat.format(details.philHealthEmployer())},
								{"Pag-IBIG Fund", MoneyFormat.format(details.pagibigEmployer())},
								{"ECC", MoneyFormat.format(details.ecc())}}));

		for (var col : columns.getChildren()) {
			HBox.setHgrow(col, Priority.ALWAYS);
		}

		Label header = new Label(details.employeeName() + " (" + details.employeeId() + ")");
		header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
		Label cutoff = new Label(details.cutoffLabel());
		cutoff.getStyleClass().add("page-subtitle");
		VBox root = new VBox(12, header, cutoff, columns);
		root.setPadding(new Insets(16));
		root.getStylesheets().add(PayslipDialog.class.getResource("/theme/theme.css").toExternalForm());

		ScrollPane scroll = new ScrollPane(root);
		scroll.setFitToWidth(true);
		javafx.scene.Scene scene = new javafx.scene.Scene(scroll, 960, 640);
		stage.setScene(scene);
		stage.showAndWait();
	}

	private static VBox buildColumn(String title, String[][] rows) {
		VBox col = new VBox(8);
		col.getStyleClass().add("card");
		Label header = new Label(title);
		header.setStyle("-fx-font-weight: bold; -fx-text-fill: #3b82f6;");
		col.getChildren().add(header);
		GridPane grid = new GridPane();
		grid.setHgap(12);
		grid.setVgap(6);
		int r = 0;
		for (String[] row : rows) {
			Label key = new Label(row[0]);
			key.getStyleClass().add("page-subtitle");
			Label val = new Label(row[1]);
			if (row[0].contains("Gross") || row[0].contains("Net")) {
				val.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e;");
			}
			grid.addRow(r++, key, val);
		}
		col.getChildren().add(grid);
		return col;
	}
}
