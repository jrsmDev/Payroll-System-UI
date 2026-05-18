package com.example.payrollsystemui.ui.admin;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.model.AttendanceRecord;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.PayrollPeriod;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.util.DateTimeUtil;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminAttendanceView extends VBox {
	private final TableView<AttendanceRecord> table = new TableView<>();

	public AdminAttendanceView() {
		setSpacing(16);
		setPadding(new Insets(28));
		getChildren().add(new PageHeader("Attendance Records", "Daily time records", null));

		TextField search = new TextField();
		search.setPromptText("Search by name or ID...");
		search.getStyleClass().add("text-field");
		DatePicker datePicker = new DatePicker();
		Button mockBtn = new Button("Generate Mock Attendance");
		mockBtn.getStyleClass().add("secondary-button");
		ComboBox<Employee> employeeBox = new ComboBox<>();
		ComboBox<PayrollPeriod.Period> periodBox = new ComboBox<>();
		try {
			employeeBox.setItems(FXCollections.observableArrayList(AppContext.payroll().listEmployees()));
			periodBox.setItems(FXCollections.observableArrayList(PayrollPeriod.recentPeriods(4)));
			employeeBox.getSelectionModel().selectFirst();
			periodBox.getSelectionModel().selectFirst();
		} catch (Exception ignored) {
			// handled on action
		}
		mockBtn.setOnAction(e -> {
			try {
				AppContext.payroll().generateMockAttendance(employeeBox.getValue().getEmployeeId(),
						periodBox.getValue());
				refresh(search.getText(), datePicker.getValue());
			} catch (Exception ex) {
				new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
			}
		});
		HBox tools = new HBox(12, search, datePicker, employeeBox, periodBox, mockBtn);
		HBox.setHgrow(search, Priority.ALWAYS);
		search.textProperty().addListener((o, a, b) -> refresh(b, datePicker.getValue()));
		datePicker.valueProperty().addListener((o, a, b) -> refresh(search.getText(), b));

		setupTable();
		VBox card = new VBox(table);
		card.getStyleClass().add("card");
		VBox.setVgrow(card, Priority.ALWAYS);
		getChildren().addAll(tools, card);
		refresh("", null);
	}

	private void setupTable() {
		TableColumn<AttendanceRecord, String> emp = new TableColumn<>("EMPLOYEE");
		emp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().employeeName()));
		emp.setCellFactory(col -> nameCell());
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
		table.getColumns().addAll(emp, date, in, out, hours, status);
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

	private TableCell<AttendanceRecord, String> nameCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				AttendanceRecord r = getTableRow().getItem();
				VBox box = new VBox(2);
				Label name = new Label(r.employeeName());
				name.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
				Label id = new Label(r.employeeId());
				id.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 11px;");
				box.getChildren().addAll(name, id);
				setGraphic(box);
				setText(null);
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
					AttendanceRecord r = getTableRow().getItem();
					setText("Late (" + r.lateMinutes() + "m)");
				} else {
					setStyle("-fx-text-fill: #3fb950;");
					setText("On Time");
				}
			}
		};
	}

	private void refresh(String search, java.time.LocalDate date) {
		try {
			table.setItems(FXCollections.observableArrayList(AppContext.payroll().listAttendance(search, date)));
		} catch (Exception ex) {
			new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
		}
	}
}
