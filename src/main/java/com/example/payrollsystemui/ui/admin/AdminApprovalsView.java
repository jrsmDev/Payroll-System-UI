package com.example.payrollsystemui.ui.admin;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.model.PayrollSubmission;
import com.example.payrollsystemui.model.SubmissionStatus;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminApprovalsView extends VBox {
	private final TableView<PayrollSubmission> table = new TableView<>();
	private SubmissionStatus currentFilter = SubmissionStatus.PENDING;

	public AdminApprovalsView() {
		setSpacing(16);
		setPadding(new Insets(28));
		getChildren().add(new PageHeader("Payroll Approvals", "Review and approve employee payroll submissions", null));
		getChildren().add(buildTabs());
		setupTable();
		VBox card = new VBox(table);
		card.getStyleClass().add("card");
		VBox.setVgrow(card, Priority.ALWAYS);
		getChildren().add(card);
		refresh();
	}

	private HBox buildTabs() {
		ToggleGroup group = new ToggleGroup();
		HBox tabs = new HBox(8);
		tabs.getChildren().add(createTab(group, "PENDING", SubmissionStatus.PENDING, true));
		tabs.getChildren().add(createTab(group, "ALL", null, false));
		tabs.getChildren().add(createTab(group, "APPROVED", SubmissionStatus.APPROVED, false));
		tabs.getChildren().add(createTab(group, "REJECTED", SubmissionStatus.REJECTED, false));
		return tabs;
	}

	private ToggleButton createTab(ToggleGroup group, String text, SubmissionStatus status, boolean selected) {
		ToggleButton btn = new ToggleButton(text);
		btn.getStyleClass().add("filter-tab");
		btn.setToggleGroup(group);
		btn.setSelected(selected);
		btn.selectedProperty().addListener((o, old, val) -> {
			if (val) {
				btn.getStyleClass().add("filter-tab-active");
				currentFilter = status;
				refresh();
			} else {
				btn.getStyleClass().remove("filter-tab-active");
			}
		});
		if (selected) {
			btn.getStyleClass().add("filter-tab-active");
		}
		return btn;
	}

	private void setupTable() {
		TableColumn<PayrollSubmission, String> emp = new TableColumn<>("EMPLOYEE");
		emp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().employeeName()));
		emp.setCellFactory(col -> employeeCell());
		TableColumn<PayrollSubmission, String> cutoff = new TableColumn<>("CUTOFF PERIOD");
		cutoff.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cutoffLabel()));
		cutoff.setCellFactory(col -> plainCell());
		TableColumn<PayrollSubmission, String> days = new TableColumn<>("DAYS");
		days.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().daysWorked())));
		days.setCellFactory(col -> plainCell());
		TableColumn<PayrollSubmission, String> ot = new TableColumn<>("OT HRS");
		ot.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().filedOtHours() + "h"));
		ot.setCellFactory(col -> otCell());
		TableColumn<PayrollSubmission, String> leaves = new TableColumn<>("LEAVES USED");
		leaves.setCellValueFactory(c -> new SimpleStringProperty("SL:" + (int) c.getValue().sickLeaveUsed() + "  VL:"
				+ (int) c.getValue().vacationLeaveUsed() + "  EL:" + (int) c.getValue().emergencyLeaveUsed()));
		leaves.setCellFactory(col -> plainCell());
		TableColumn<PayrollSubmission, String> loans = new TableColumn<>("LOANS");
		loans.setCellValueFactory(c -> new SimpleStringProperty(MoneyFormat.format(c.getValue().companyLoanDeduction()
				+ c.getValue().sssLoanDeduction() + c.getValue().pagibigLoanDeduction())));
		loans.setCellFactory(col -> plainCell());
		TableColumn<PayrollSubmission, String> status = new TableColumn<>("STATUS");
		status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));
		status.setCellFactory(col -> statusCell());
		TableColumn<PayrollSubmission, Void> actions = new TableColumn<>("ACTIONS");
		actions.setCellFactory(col -> actionCell());
		table.getColumns().addAll(emp, cutoff, days, ot, leaves, loans, status, actions);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}

	private TableCell<PayrollSubmission, String> plainCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					Label lbl = new Label(item);
					lbl.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
					setGraphic(lbl);
					setText(null);
				}
			}
		};
	}

	private TableCell<PayrollSubmission, String> employeeCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				PayrollSubmission s = getTableRow().getItem();
				VBox box = new VBox(2);
				Label name = new Label(s.employeeName());
				name.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
				Label id = new Label(s.employeeId());
				id.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 11px;");
				box.getChildren().addAll(name, id);
				setGraphic(box);
				setText(null);
			}
		};
	}

	private TableCell<PayrollSubmission, String> otCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				double hours = getTableRow().getItem().filedOtHours();
				Label lbl = new Label(hours + "h");
				lbl.setStyle(hours > 0 ? "-fx-text-fill: #d29922;" : "-fx-text-fill: #7d8590;");
				setGraphic(lbl);
				setText(null);
			}
		};
	}

	private TableCell<PayrollSubmission, String> statusCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				Label badge = new Label(item);
				badge.getStyleClass()
						.add(SubmissionStatus.PENDING.name().equals(item)
								? "badge-pending"
								: SubmissionStatus.APPROVED.name().equals(item) ? "badge-active" : "badge-rejected");
				setGraphic(badge);
				setText(null);
			}
		};
	}

	private TableCell<PayrollSubmission, Void> actionCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				PayrollSubmission s = getTableRow().getItem();
				HBox box = new HBox(8);
				if (s.status() == SubmissionStatus.PENDING) {
					Button approve = new Button("Approve");
					approve.setStyle(
							"-fx-background-color: #21262d; -fx-text-fill: #3fb950; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 5 10; -fx-border-color: #3fb950; -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
					approve.setOnAction(e -> {
						try {
							AppContext.payroll().approveSubmission(s.id());
							refresh();
						} catch (Exception ex) {
							new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
									ex.getMessage() != null ? ex.getMessage() : "Unknown error").showAndWait();
						}
					});
					Button reject = new Button("Reject");
					reject.setStyle(
							"-fx-background-color: #21262d; -fx-text-fill: #f85149; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 5 10; -fx-border-color: #f85149; -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
					reject.setOnAction(e -> {
						try {
							AppContext.payroll().rejectSubmission(s.id());
							refresh();
						} catch (Exception ex) {
							new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
									ex.getMessage() != null ? ex.getMessage() : "Unknown error").showAndWait();
						}
					});
					box.getChildren().addAll(approve, reject);
				} else if (s.status() == SubmissionStatus.APPROVED) {
					Button view = new Button("Payslip");
					view.setStyle(
							"-fx-background-color: #21262d; -fx-text-fill: #e6edf3; -fx-font-size: 12px; -fx-background-radius: 6; -fx-padding: 5 10; -fx-border-color: #30363d; -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
					view.setOnAction(e -> {
						try {
							com.example.payrollsystemui.ui.payslip.PayslipDialog
									.show(AppContext.payroll().buildPayslip(s.id()));
						} catch (Exception ex) {
							new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
									ex.getMessage() != null ? ex.getMessage() : "Unknown error").showAndWait();
						}
					});
					box.getChildren().add(view);
				}
				setGraphic(box);
				setText(null);
			}
		};
	}

	private void refresh() {
		try {
			if (currentFilter == null) {
				table.setItems(FXCollections.observableArrayList(AppContext.payroll().listSubmissions(null)));
			} else {
				table.setItems(FXCollections.observableArrayList(AppContext.payroll().listSubmissions(currentFilter)));
			}
		} catch (Exception ex) {
			new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
		}
	}
}
