package com.example.payrollsystemui.app;

import com.example.payrollsystemui.database.DatabaseManager;
import com.example.payrollsystemui.model.UserRole;
import com.example.payrollsystemui.ui.admin.AdminShell;
import com.example.payrollsystemui.ui.components.FaqOverlay;
import com.example.payrollsystemui.ui.employee.EmployeeShell;
import com.example.payrollsystemui.ui.login.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

// Main entry point of the Payroll System application (JavaFX Application class)
public class PayrollApplication extends Application {

	// Root layout — uses StackPane so we can swap views (login, admin, employee) on
	// top of each other
	private StackPane root;

	@Override
	public void start(Stage stage) {
		// Connect to the database first; if it fails, show an error and stop
		try {
			DatabaseManager.initialize();
		} catch (Exception ex) {
			showFatal(stage,
					"Database connection failed. Ensure MySQL is running and payroll_db exists.\n" + ex.getMessage());
			return;
		}

		root = new StackPane();
		FaqOverlay.attach(root); // Attach the floating FAQ/help button to the root
		showLogin(); // Start with the login screen

		// Set up the main window
		Scene scene = new Scene(root, 1280, 800);
		scene.getStylesheets().add(getClass().getResource("/theme/theme.css").toExternalForm());
		stage.setTitle("ABC Company - Payroll System");
		stage.setMinWidth(1100);
		stage.setMinHeight(700);
		stage.setScene(scene);
		stage.show();
	}

	// Clears the current view and shows the login screen
	private void showLogin() {
		// Remove any existing view (login, admin shell, or employee shell)
		root.getChildren().removeIf(
				node -> node instanceof LoginView || node instanceof AdminShell || node instanceof EmployeeShell);

		// We wrap LoginView in an array so the lambda below can reference it
		LoginView[] loginHolder = new LoginView[1];
		loginHolder[0] = new LoginView(role -> {
			root.getChildren().remove(loginHolder[0]); // Remove login screen after successful login
			if (role == UserRole.ADMIN) {
				root.getChildren().add(new AdminShell(this::showLogin)); // Show admin dashboard
			} else {
				root.getChildren().add(new EmployeeShell(this::showLogin)); // Show employee dashboard
			}
		});
		root.getChildren().add(0, loginHolder[0]); // Add login at the bottom of the stack
	}

	// Shows a blocking error dialog then closes the window (used for fatal startup
	// errors)
	private void showFatal(Stage stage, String message) {
		javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
		alert.setContentText(message);
		alert.showAndWait();
		stage.close();
	}

	@Override
	public void stop() {
		// Called when the app is closing — gracefully release the DB connection pool
		DatabaseManager.shutdown();
	}
}
