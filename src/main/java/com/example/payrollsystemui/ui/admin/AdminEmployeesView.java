package com.example.payrollsystemui.ui.admin;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.EmploymentType;
import com.example.payrollsystemui.service.InputValidator;
import com.example.payrollsystemui.ui.UiStyles;
import com.example.payrollsystemui.ui.components.PageHeader;
import com.example.payrollsystemui.util.MoneyFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Admin screen for managing employees — view, search, filter, add, and delete employees
public class AdminEmployeesView extends VBox {

	private final TableView<Employee> table = new TableView<>();

	// masterList holds all employees from the DB; filtered is what the table
	// actually shows
	private final javafx.collections.ObservableList<Employee> masterList = FXCollections.observableArrayList();
	private final FilteredList<Employee> filtered = new FilteredList<>(masterList);

	private final PageHeader pageHeader;

	public AdminEmployeesView() {
		setSpacing(16);
		setPadding(new Insets(28));

		// "+ Add Employee" button lives in the page header
		Button addBtn = new Button("+ Add Employee");
		addBtn.getStyleClass().add("primary-button");
		addBtn.setOnAction(e -> showAddDialog());
		pageHeader = new PageHeader("Employee Management", "Loading...", addBtn);
		getChildren().add(pageHeader);

		// Search bar — filters by name or employee ID as you type
		TextField search = new TextField();
		search.setPromptText("Search by name or ID...");
		search.getStyleClass().add("text-field");

		// Dropdown to filter by employment type
		ComboBox<String> typeFilter = new ComboBox<>(
				FXCollections.observableArrayList("All", "Regular", "Contractual", "Part-Time", "Probationary"));
		typeFilter.getSelectionModel().select(0); // Default to "All"

		HBox filters = new HBox(12, search, typeFilter);
		HBox.setHgrow(search, Priority.ALWAYS); // Search bar takes up the remaining space

		// Re-apply filter whenever the search text or type selection changes
		search.textProperty().addListener((o, a, b) -> applyFilter(b, typeFilter.getValue()));
		typeFilter.valueProperty().addListener((o, a, b) -> applyFilter(search.getText(), b));

		setupTable();

		// Wrap the table in a card-styled container
		VBox tableCard = new VBox(table);
		tableCard.getStyleClass().add("card");
		VBox.setVgrow(tableCard, Priority.ALWAYS); // Let the table fill remaining vertical space

		getChildren().addAll(filters, tableCard);
		refresh(); // Load employees from DB on startup
	}

	// Defines all table columns and how to populate them from an Employee object
	private void setupTable() {
		// EMPLOYEE column — shows name + ID stacked (custom cell renderer below)
		TableColumn<Employee, String> empCol = new TableColumn<>("EMPLOYEE");
		empCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
		empCol.setCellFactory(col -> employeeCell());
		empCol.setPrefWidth(220);

		TableColumn<Employee, String> deptCol = new TableColumn<>("DEPARTMENT");
		deptCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartment()));
		deptCol.setCellFactory(col -> plainCell());

		// TYPE column — shows a colored badge (Regular, Contractual, etc.)
		TableColumn<Employee, String> typeCol = new TableColumn<>("TYPE");
		typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmploymentType().displayName()));
		typeCol.setCellFactory(col -> typeCell());

		TableColumn<Employee, String> rateCol = new TableColumn<>("BASIC RATE");
		rateCol.setCellValueFactory(c -> new SimpleStringProperty(MoneyFormat.format(c.getValue().getBasicRate())));
		rateCol.setCellFactory(col -> plainCell());

		// LEAVES column — shows remaining SL, VL, EL balances in one cell
		TableColumn<Employee, String> leaveCol = new TableColumn<>("LEAVES");
		leaveCol.setCellValueFactory(c -> new SimpleStringProperty("SL: " + (int) c.getValue().getSickLeaveBalance()
				+ "  VL: " + (int) c.getValue().getVacationLeaveBalance() + "  EL: "
				+ (int) c.getValue().getEmergencyLeaveBalance()));
		leaveCol.setCellFactory(col -> plainCell());

		// STATUS column — shows a green "ACTIVE" or red badge
		TableColumn<Employee, String> statusCol = new TableColumn<>("STATUS");
		statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
		statusCol.setCellFactory(col -> statusCell());
		statusCol.setPrefWidth(100);

		// Action column — just a Delete button per row (no header text)
		TableColumn<Employee, Void> actions = new TableColumn<>("");
		actions.setCellFactory(col -> deleteCell());
		actions.setPrefWidth(90);

		table.setItems(filtered); // Bind table to the filtered list
		table.getColumns().addAll(empCol, deptCol, typeCol, rateCol, leaveCol, statusCol, actions);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
	}

	// Simple styled cell — just shows text in the app's default text color
	private TableCell<Employee, String> plainCell() {
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

	// Employee cell — shows the full name on the first line and the ID below in
	// smaller gray text
	private TableCell<Employee, String> employeeCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				Employee e = getTableRow().getItem();
				Label name = new Label(e.getFullName());
				name.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
				Label id = new Label(e.getEmployeeId());
				id.setStyle("-fx-text-fill: #7d8590; -fx-font-size: 11px;"); // Subtle gray for the ID
				VBox box = new VBox(2, name, id);
				setGraphic(box);
				setText(null);
			}
		};
	}

	// Employment type cell — wraps the text in a colored CSS badge
	private TableCell<Employee, String> typeCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				try {
					Label badge = new Label(item);
					// UiStyles picks the right CSS class based on employment type
					badge.getStyleClass().add(UiStyles.typeBadgeClass(getTableRow().getItem().getEmploymentType()));
					setGraphic(badge);
					setText(null);
				} catch (Exception ex) {
					// Fallback: just show the text if badge styling fails
					setText(item);
					setGraphic(null);
				}
			}
		};
	}

	// Status cell — green badge for ACTIVE, red badge for everything else
	private TableCell<Employee, String> statusCell() {
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
				badge.getStyleClass().add("ACTIVE".equalsIgnoreCase(item) ? "badge-active" : "badge-rejected");
				setGraphic(badge);
				setText(null);
			}
		};
	}

	// Delete button cell — each row has its own delete button that confirms before
	// removing
	private TableCell<Employee, Void> deleteCell() {
		return new TableCell<>() {
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				Button delete = new Button("Delete");
				delete.setStyle("-fx-background-color: #21262d; -fx-text-fill: #f85149;"
						+ " -fx-font-size: 12px; -fx-background-radius: 6;"
						+ " -fx-padding: 5 10; -fx-border-color: #f85149;"
						+ " -fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
				delete.setOnAction(e -> {
					if (getTableRow() == null || getTableRow().getItem() == null) {
						return;
					}
					Employee emp = getTableRow().getItem();
					// Ask for confirmation before deleting
					Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
					confirm.setContentText("Delete employee " + emp.getFullName() + "?");
					confirm.showAndWait().ifPresent(btn -> {
						if (btn == ButtonType.OK) {
							try {
								AppContext.payroll().deleteEmployee(emp.getEmployeeId());
								refresh(); // Reload the table after deletion
							} catch (Exception ex) {
								showError(ex.getMessage() != null ? ex.getMessage() : "Unknown error");
							}
						}
					});
				});
				setGraphic(delete);
				setText(null);
			}
		};
	}

	// Opens the "Add Employee" form dialog
	private void showAddDialog() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Add Employee");
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		// Grid form with all the fields needed to create an employee
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(16));

		TextField id = new TextField();
		TextField name = new TextField();
		TextField dept = new TextField();
		TextField position = new TextField();
		ComboBox<EmploymentType> type = new ComboBox<>(FXCollections.observableArrayList(EmploymentType.values()));
		type.getSelectionModel().select(EmploymentType.REGULAR); // Default to Regular

		// Pre-fill reasonable defaults for numeric fields
		TextField rate = new TextField("35000");
		TextField schedule = new TextField("08:00 - 17:00");
		TextField sick = new TextField("15");
		TextField vacation = new TextField("15");
		TextField emergency = new TextField("3");
		TextField companyLoan = new TextField("0");
		TextField sssLoan = new TextField("0");
		TextField pagibigLoan = new TextField("0");

		int row = 0;
		grid.addRow(row++, new Label("Employee ID"), id);
		grid.addRow(row++, new Label("Full Name"), name);
		grid.addRow(row++, new Label("Department"), dept);
		grid.addRow(row++, new Label("Position"), position);
		grid.addRow(row++, new Label("Employment Type"), type);
		grid.addRow(row++, new Label("Basic Rate"), rate);
		grid.addRow(row++, new Label("Work Schedule"), schedule);
		grid.addRow(row++, new Label("Sick Leave"), sick);
		grid.addRow(row++, new Label("Vacation Leave"), vacation);
		grid.addRow(row++, new Label("Emergency Leave"), emergency);
		grid.addRow(row++, new Label("Company Loan"), companyLoan);
		grid.addRow(row++, new Label("SSS Loan"), sssLoan);
		grid.addRow(row++, new Label("Pag-IBIG Loan"), pagibigLoan);

		dialog.getDialogPane().setContent(grid);
		dialog.showAndWait().ifPresent(btn -> {
			if (btn == ButtonType.OK) {
				try {
					// Validate required fields before saving
					InputValidator.requireNonBlank(id.getText(), "Employee ID");
					InputValidator.requireNonBlank(name.getText(), "Name");
					AppContext.payroll().addEmployee(id.getText().trim().toUpperCase(), name.getText().trim(),
							dept.getText().trim(), position.getText().trim(), type.getValue(),
							InputValidator.requireNonNegative(rate.getText(), "Basic rate"), schedule.getText().trim(),
							InputValidator.requireNonNegative(sick.getText(), "Sick leave"),
							InputValidator.requireNonNegative(vacation.getText(), "Vacation leave"),
							InputValidator.requireNonNegative(emergency.getText(), "Emergency leave"),
							InputValidator.requireNonNegative(companyLoan.getText(), "Company loan"),
							InputValidator.requireNonNegative(sssLoan.getText(), "SSS loan"),
							InputValidator.requireNonNegative(pagibigLoan.getText(), "Pag-IBIG loan"), "admin123");
					refresh(); // Reload table to show the newly added employee
				} catch (Exception ex) {
					showError(ex.getMessage());
				}
			}
		});
	}

	// Reloads the employee list from the DB and updates the subtitle count
	private void refresh() {
		try {
			masterList.setAll(AppContext.payroll().listEmployees());
			pageHeader.setSubtitle(filtered.size() + " total employees");
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	// Updates the FilteredList predicate based on search text and type dropdown
	// selection
	private void applyFilter(String search, String type) {
		filtered.setPredicate(e -> {
			// Check if the employee matches the search term (name or ID)
			boolean matchesSearch = search == null || search.isBlank()
					|| e.getFullName().toLowerCase().contains(search.toLowerCase())
					|| e.getEmployeeId().toLowerCase().contains(search.toLowerCase());
			// Check if the employee matches the selected type
			boolean matchesType = type == null || "All".equals(type)
					|| e.getEmploymentType().displayName().equals(type);
			return matchesSearch && matchesType;
		});
	}

	// Shows a simple error dialog — falls back to a generic message if the error
	// has no detail
	private void showError(String msg) {
		String safeMsg = (msg != null && !msg.isBlank()) ? msg : "An unexpected error occurred.";
		Alert alert = new Alert(Alert.AlertType.ERROR, safeMsg, ButtonType.OK);
		alert.showAndWait();
	}

	// Helper to create a label styled as a subtitle (used for section headers
	// inside cards)
	private Label subtitle(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("page-subtitle");
		return label;
	}
}
