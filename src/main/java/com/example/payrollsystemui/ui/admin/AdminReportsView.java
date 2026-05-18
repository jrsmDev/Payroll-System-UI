package com.example.payrollsystemui.ui.admin;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.model.EmploymentType;
import com.example.payrollsystemui.model.PayslipDetails;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.ui.components.StatCard;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Admin Reports screen — shows summary stats, charts, and a government contribution table
public class AdminReportsView extends VBox {

	public AdminReportsView() {
		setSpacing(16);
		setPadding(new Insets(28));
		try {
			var payroll = AppContext.payroll();

			// Page title at the top
			getChildren()
					.add(new PageHeader("Payroll Reports", "Analytics and government contribution summaries", null));

			// Row of 4 summary stat cards
			HBox stats = new HBox(16);
			stats.getChildren().addAll(
					new StatCard("Total Employees", String.valueOf(payroll.countActiveEmployees()), null),
					new StatCard("Total Net Pay Processed", MoneyFormat.format(payroll.totalApprovedNetPay()),
							"stat-value-green"),
					new StatCard("Approved Payrolls", String.valueOf(payroll.countApprovedSubmissions()),
							"stat-value-blue"),
					new StatCard("Avg Basic Rate", MoneyFormat.format(payroll.averageBasicRate()), null));

			// Make each stat card stretch equally across the row
			for (var n : stats.getChildren()) {
				HBox.setHgrow(n, Priority.ALWAYS);
			}

			// Charts row: net pay by department (placeholder) + pie chart for employee
			// types
			HBox charts = new HBox(16);
			charts.getChildren().addAll(buildNetPayCard(), buildPieCard(payroll.employmentTypeDistribution()));
			HBox.setHgrow(charts.getChildren().get(0), Priority.ALWAYS);
			HBox.setHgrow(charts.getChildren().get(1), Priority.ALWAYS);

			// Government contribution table at the bottom
			VBox tableCard = buildContributionTable();
			tableCard.getStyleClass().add("card");

			getChildren().addAll(stats, charts, tableCard);
		} catch (Exception ex) {
			// If anything fails to load, show a plain error message instead of crashing
			getChildren().add(new Label("Failed to load reports: " + ex.getMessage()));
		}
	}

	// Placeholder card for the "Net Pay by Department" chart (not yet implemented)
	private VBox buildNetPayCard() {
		VBox card = new VBox(12);
		card.getStyleClass().add("card");
		Label title = new Label("Net Pay by Department");
		title.setStyle("-fx-font-weight: bold; -fx-text-fill: #e6edf3; -fx-font-size: 13px;");
		Label empty = new Label("No approved payroll data yet");
		empty.getStyleClass().add("page-subtitle");
		empty.setAlignment(Pos.CENTER);
		VBox.setVgrow(empty, Priority.ALWAYS);
		card.getChildren().addAll(title, empty);
		return card;
	}

	// Builds the pie chart showing how many employees are Regular, Contractual,
	// etc.
	private VBox buildPieCard(java.util.Map<EmploymentType, Long> dist) {
		VBox card = new VBox(12);
		card.getStyleClass().add("card");
		Label title = new Label("Employee Type Distribution");
		title.setStyle("-fx-font-weight: bold; -fx-text-fill: #e6edf3; -fx-font-size: 13px;");
		PieChart chart = new PieChart();
		// Add one pie slice per employment type
		dist.forEach((type, count) -> chart.getData().add(new PieChart.Data(type.displayName(), count)));
		chart.setLegendVisible(true);
		chart.setLabelsVisible(false); // Keep it clean — labels are in the legend instead
		card.getChildren().addAll(title, chart);
		return card;
	}

	// Builds the government contribution table (SSS, PhilHealth, Pag-IBIG per
	// employee)
	private VBox buildContributionTable() {
		Label title = new Label("Monthly Government Contribution Summary");
		title.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 12 0; -fx-text-fill: #e6edf3; -fx-font-size: 13px;");

		TableView<ContributionRow> table = new TableView<>();
		try {
			// Populate the table with approved payroll data
			for (PayslipDetails p : AppContext.payroll().governmentContributionReport()) {
				table.getItems().add(new ContributionRow(p.employeeName(), p.employeeId(), p.rate(), p.sssEmployee(),
						p.philHealthEmployee(), p.pagibigEmployee(), p.sssEmployer(), p.sssEmployer() + p.ecc()));
			}
		} catch (Exception ex) {
			// If no data yet, just leave the table empty
		}

		// Define table columns — EE = Employee share, ER = Employer share
		TableColumn<ContributionRow, String> emp = new TableColumn<>("EMPLOYEE");
		emp.setCellValueFactory(new PropertyValueFactory<>("employee"));

		TableColumn<ContributionRow, String> rate = new TableColumn<>("BASIC RATE");
		rate.setCellValueFactory(new PropertyValueFactory<>("rateDisplay"));

		TableColumn<ContributionRow, String> sssEe = new TableColumn<>("SSS (EE)");
		sssEe.setCellValueFactory(new PropertyValueFactory<>("sssEeDisplay"));

		TableColumn<ContributionRow, String> ph = new TableColumn<>("PHILHEALTH (EE)");
		ph.setCellValueFactory(new PropertyValueFactory<>("phEeDisplay"));

		TableColumn<ContributionRow, String> pag = new TableColumn<>("PAG-IBIG (EE)");
		pag.setCellValueFactory(new PropertyValueFactory<>("pagEeDisplay"));

		TableColumn<ContributionRow, String> sssEr = new TableColumn<>("SSS (ER)");
		sssEr.setCellValueFactory(new PropertyValueFactory<>("sssErDisplay"));

		TableColumn<ContributionRow, String> totalEr = new TableColumn<>("TOTAL ER");
		totalEr.setCellValueFactory(new PropertyValueFactory<>("totalErDisplay"));

		table.getColumns().addAll(emp, rate, sssEe, ph, pag, sssEr, totalEr);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		return new VBox(8, title, table);
	}

	// Data model for one row in the contribution table.
	// All monetary values are pre-formatted as strings (e.g., "₱1,250.00") for
	// display.
	public static class ContributionRow {
		private final String employee; // "Full Name\nEMP-001"
		private final String rateDisplay; // Basic monthly rate
		private final String sssEeDisplay; // SSS deduction from employee
		private final String phEeDisplay; // PhilHealth deduction from employee
		private final String pagEeDisplay; // Pag-IBIG deduction from employee
		private final String sssErDisplay; // SSS contribution from employer
		private final String totalErDisplay; // Total employer cost (SSS + ECC)

		public ContributionRow(String name, String id, double rate, double sssEe, double phEe, double pagEe,
				double sssEr, double totalEr) {
			// Combine name and ID so the EMPLOYEE column shows both lines
			this.employee = name + "\n" + id;
			this.rateDisplay = MoneyFormat.format(rate);
			this.sssEeDisplay = MoneyFormat.format(sssEe);
			this.phEeDisplay = MoneyFormat.format(phEe);
			this.pagEeDisplay = MoneyFormat.format(pagEe);
			this.sssErDisplay = MoneyFormat.format(sssEr);
			this.totalErDisplay = MoneyFormat.format(totalEr);
		}

		// Getters below — required by PropertyValueFactory to bind columns to fields
		public String getEmployee() {
			return employee;
		}

		public String getRateDisplay() {
			return rateDisplay;
		}

		public String getSssEeDisplay() {
			return sssEeDisplay;
		}

		public String getPhEeDisplay() {
			return phEeDisplay;
		}

		public String getPagEeDisplay() {
			return pagEeDisplay;
		}

		public String getSssErDisplay() {
			return sssErDisplay;
		}

		public String getTotalErDisplay() {
			return totalErDisplay;
		}
	}
}
