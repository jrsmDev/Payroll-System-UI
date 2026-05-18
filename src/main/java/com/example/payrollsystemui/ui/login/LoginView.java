package com.example.payrollsystemui.ui.login;

import com.example.payrollsystemui.app.AppContext;
import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.User;
import com.example.payrollsystemui.model.UserRole;
import com.example.payrollsystemui.service.PayrollService;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginView extends StackPane {
	private final Consumer<UserRole> onSuccess;
	private final TextField employeeIdField = new TextField();
	private final PasswordField passwordField = new PasswordField();

	public LoginView(Consumer<UserRole> onSuccess) {
		this.onSuccess = onSuccess;

		// Full-screen dark background
		setStyle("-fx-background-color: #0d1117;");
		setAlignment(Pos.CENTER);

		// ── Card ────────────────────────────────────────────────────
		VBox card = new VBox(0);
		card.setMaxWidth(400);
		card.setMinWidth(360);
		card.setMaxHeight(Region.USE_PREF_SIZE);
		card.setStyle("-fx-background-color: #161b22; -fx-background-radius: 10;"
				+ " -fx-border-color: #30363d; -fx-border-radius: 10; -fx-border-width: 1;"
				+ " -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 8);");

		// ── Brand header (darker strip) ─────────────────────────────
		VBox brandBlock = new VBox(4);
		brandBlock.setAlignment(Pos.CENTER);
		brandBlock.setPadding(new Insets(28, 32, 22, 32));
		brandBlock.setStyle("-fx-background-color: #0d1117; -fx-background-radius: 8 8 0 0;"
				+ " -fx-border-color: transparent transparent #30363d transparent;" + " -fx-border-width: 0 0 1 0;");
		Label brand = new Label("ABC Company");
		brand.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e6edf3;");
		Label sub = new Label("Employee Payroll System");
		sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #7d8590;");
		brandBlock.getChildren().addAll(brand, sub);

		// ── Form block ──────────────────────────────────────────────
		VBox formBlock = new VBox(16);
		formBlock.setPadding(new Insets(28, 32, 32, 32));

		Label title = new Label("Sign in to your account");
		title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e6edf3;");

		// Employee ID
		VBox idGroup = new VBox(6);
		Label idLabel = new Label("Employee ID");
		idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b949e;");
		HBox idWrapper = iconField(FontAwesomeSolid.USER, employeeIdField);
		employeeIdField.setPromptText("e.g. EMP001");
		employeeIdField.setStyle("-fx-background-color: transparent; -fx-text-fill: #e6edf3;"
				+ " -fx-prompt-text-fill: #484f58; -fx-border-color: transparent;"
				+ " -fx-padding: 9 12 9 4; -fx-font-size: 13px;");
		idGroup.getChildren().addAll(idLabel, idWrapper);

		// Password
		VBox passGroup = new VBox(6);
		Label passLabel = new Label("Password");
		passLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b949e;");
		HBox passWrapper = iconField(FontAwesomeSolid.LOCK, passwordField);
		passwordField.setPromptText("Enter your password");
		passwordField.setStyle("-fx-background-color: transparent; -fx-text-fill: #e6edf3;"
				+ " -fx-prompt-text-fill: #484f58; -fx-border-color: transparent;"
				+ " -fx-padding: 9 12 9 4; -fx-font-size: 13px;");
		passGroup.getChildren().addAll(passLabel, passWrapper);

		// Sign In button
		Button signIn = new Button("Sign In");
		signIn.setStyle("-fx-background-color: #1f6feb; -fx-text-fill: white;"
				+ " -fx-font-weight: bold; -fx-font-size: 14px;" + " -fx-background-radius: 6; -fx-padding: 11 18;"
				+ " -fx-cursor: hand; -fx-border-color: #388bfd;" + " -fx-border-radius: 6; -fx-border-width: 1;");
		signIn.setMaxWidth(Double.MAX_VALUE);
		signIn.setOnAction(e -> attemptLogin());
		signIn.setOnMouseEntered(e -> signIn.setStyle(signIn.getStyle().replace("#1f6feb", "#388bfd")));
		signIn.setOnMouseExited(e -> signIn.setStyle(signIn.getStyle().replace("#388bfd", "#1f6feb")));

		// Enter key shortcuts
		passwordField.setOnAction(e -> attemptLogin());
		employeeIdField.setOnAction(e -> passwordField.requestFocus());

		formBlock.getChildren().addAll(title, idGroup, passGroup, signIn);
		card.getChildren().addAll(brandBlock, formBlock);

		VBox wrapper = new VBox(card);
		wrapper.setAlignment(Pos.CENTER);

		getChildren().add(wrapper);
	}

	private HBox iconField(FontAwesomeSolid iconCode, javafx.scene.control.Control field) {
		FontIcon icon = new FontIcon(iconCode);
		icon.setIconSize(13);
		icon.setIconColor(Color.web("#484f58"));

		HBox wrapper = new HBox(6);
		wrapper.setAlignment(Pos.CENTER_LEFT);
		wrapper.setPadding(new Insets(0, 0, 0, 12));
		wrapper.setStyle("-fx-background-color: #0d1117; -fx-background-radius: 6;"
				+ " -fx-border-color: #30363d; -fx-border-radius: 6; -fx-border-width: 1;");
		HBox.setHgrow(field, Priority.ALWAYS);
		wrapper.getChildren().addAll(icon, field);
		return wrapper;
	}

	private void attemptLogin() {
		try {
			Optional<User> user = AppContext.auth().authenticate(employeeIdField.getText(), passwordField.getText());
			if (user.isEmpty()) {
				showError("Invalid employee ID or password.");
				return;
			}
			PayrollService payroll = AppContext.payroll();
			Employee employee = payroll.findEmployee(user.get().employeeId())
					.orElseThrow(() -> new SQLException("Employee profile not found."));
			Session.set(user.get(), employee);
			onSuccess.accept(user.get().role());
		} catch (Exception ex) {
			showError("Login failed: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
		}
	}

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
