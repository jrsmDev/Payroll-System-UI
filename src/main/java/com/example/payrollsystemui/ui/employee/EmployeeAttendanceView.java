package com.example.payrollsystemui.ui.employee;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.model.AttendanceRecord;
import com.example.payrollsystemui.service.PayrollService;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.ui.components.StatCard;
import com.example.payrollsystemui.util.DateTimeUtil;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.time.YearMonth;

public class EmployeeAttendanceView extends VBox {
	public EmployeeAttendanceView() {
		setSpacing(16);
		setPadding(new Insets(28));
		getChildren().add(new PageHeader("My Attendance", "Your daily time records", null));
		ComboBox<String> monthBox = new ComboBox<>();
		YearMonth current = YearMonth.now();
		monthBox.getItems().add(current.getMonth() + " " + current.getYear());
		monthBox.getSelectionModel().select(0);

		HBox stats = new HBox(16);
		TableView<AttendanceRecord> table = new TableView<>();
		Runnable refresh = () -> {
			try {
				PayrollService.AttendanceSummary summary = AppContext.payroll().attendanceSummary(
						Session.employee().getEmployeeId(), current.getYear(), current.getMonthValue());
				stats.getChildren().clear();
				stats.getChildren().addAll(new StatCard("Days Present", String.valueOf(summary.daysPresent()), null),
						new StatCard("Total Hours", MoneyFormat.formatHours(summary.totalHours()), "stat-value-blue"),
						new StatCard("Late Days", String.valueOf(summary.lateDays()), "stat-value-orange"));
				for (var n : stats.getChildren()) {
					HBox.setHgrow(n, Priority.ALWAYS);
				}
				table.setItems(FXCollections.observableArrayList(AppContext.payroll().listEmployeeAttendance(
						Session.employee().getEmployeeId(), current.getYear(), current.getMonthValue())));
			} catch (Exception ex) {
				new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, ex.getMessage())
						.showAndWait();
			}
		};

		setupTable(table);
		VBox card = new VBox(table);
		card.getStyleClass().add("card");
		VBox.setVgrow(card, Priority.ALWAYS);
		getChildren().addAll(stats, monthBox, card);
		refresh.run();
	}

	private void setupTable(TableView<AttendanceRecord> table) {
		TableColumn<AttendanceRecord, String> date = new TableColumn<>("DATE");
		date.setCellValueFactory(c -> new SimpleStringProperty(DateTimeUtil.formatDate(c.getValue().date())));
		date.setCellFactory(col -> plainCell());
		TableColumn<AttendanceRecord, String> in = new TableColumn<>("TIME IN");
		in.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().timeIn() == null ? "-" : c.getValue().timeIn().format(DateTimeUtil.TIME)));
		in.setCellFactory(col -> plainCell());
		TableColumn<AttendanceRecord, String> out = new TableColumn<>("TIME OUT");
		out.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().timeOut() == null ? "-" : c.getValue().timeOut().format(DateTimeUtil.TIME)));
		out.setCellFactory(col -> plainCell());
		TableColumn<AttendanceRecord, String> hours = new TableColumn<>("HOURS");
		hours.setCellValueFactory(c -> new SimpleStringProperty(MoneyFormat.formatHours(c.getValue().hoursWorked())));
		hours.setCellFactory(col -> plainCell());
		TableColumn<AttendanceRecord, String> status = new TableColumn<>("STATUS");
		status.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
		status.setCellFactory(col -> statusCell());
		table.getColumns().addAll(date, in, out, hours, status);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}

	private TableCell<AttendanceRecord, String> plainCell() {
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

	private TableCell<AttendanceRecord, String> statusCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				if ("LATE".equals(item)) {
					setStyle("-fx-text-fill: #d29922;");
					setText("Late (" + getTableRow().getItem().lateMinutes() + "m)");
				} else {
					setStyle("-fx-text-fill: #3fb950;");
					setText("On Time");
				}
			}
		};
	}
}
