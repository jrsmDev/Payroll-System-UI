package com.example.payrollsystemui.ui.components;

import com.example.payrollsystemui.app.Session;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class Sidebar extends VBox {
	private static final String COLOR_ICON_DEFAULT = "#7d8590";
	private static final String COLOR_ICON_ACTIVE = "#388bfd";
	private static final String COLOR_LABEL_DEFAULT = "#8b949e";
	private static final String COLOR_LABEL_ACTIVE = "#e6edf3";

	private final Map<String, Button> navButtons = new LinkedHashMap<>();
	private final Map<String, FontIcon> navIcons = new LinkedHashMap<>();
	private Consumer<String> onNavigate;
	private Consumer<Void> onSignOut;

	public Sidebar(String sectionTitle, Map<String, FontAwesomeSolid> items) {
		getStyleClass().add("sidebar");
		setSpacing(2);

		// ── Brand ──────────────────────────────────────────────────
		VBox brand = new VBox(2);
		brand.setPadding(new Insets(4, 4, 8, 4));
		Label company = new Label("ABC Company");
		company.getStyleClass().add("sidebar-brand-title");
		Label subtitle = new Label("Payroll System");
		subtitle.getStyleClass().add("sidebar-brand-sub");
		brand.getChildren().addAll(company, subtitle);

		// ── Section label ──────────────────────────────────────────
		Label section = new Label(sectionTitle);
		section.getStyleClass().add("sidebar-section");
		section.setPadding(new Insets(12, 4, 6, 4));

		// ── Nav items ──────────────────────────────────────────────
		VBox navBox = new VBox(2);
		items.forEach((name, icon) -> {
			Button btn = createNavButton(name, icon);
			navButtons.put(name, btn);
			navBox.getChildren().add(btn);
		});

		// ── Spacer ─────────────────────────────────────────────────
		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		// ── Profile card ───────────────────────────────────────────
		VBox profile = buildProfileCard();

		// ── Sign out ───────────────────────────────────────────────
		FontIcon signOutIcon = new FontIcon(FontAwesomeSolid.SIGN_OUT_ALT);
		signOutIcon.setIconSize(13);
		signOutIcon.setIconColor(Color.web("#7d8590"));
		Button signOut = new Button("Sign Out", signOutIcon);
		signOut.getStyleClass().add("secondary-button");
		signOut.setMaxWidth(Double.MAX_VALUE);
		signOut.setOnAction(e -> {
			if (onSignOut != null) {
				onSignOut.accept(null);
			}
		});

		getChildren().addAll(brand, section, navBox, spacer, profile, signOut);
	}

	private VBox buildProfileCard() {
		VBox card = new VBox(4);
		card.getStyleClass().add("profile-card");
		card.setMaxWidth(Double.MAX_VALUE);

		String name = Session.employee() != null ? Session.employee().getFullName() : "User";
		String id = Session.user() != null ? Session.user().employeeId() : "";
		String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();

		HBox row = new HBox(10);
		row.setAlignment(Pos.CENTER_LEFT);

		Label avatar = new Label(initial);
		avatar.setStyle("-fx-background-color: #1f6feb; -fx-background-radius: 18; "
				+ "-fx-min-width: 34; -fx-min-height: 34; -fx-max-width: 34; -fx-max-height: 34;"
				+ "-fx-alignment: center; -fx-text-fill: white; -fx-font-weight: bold;" + "-fx-font-size: 14px;");

		VBox texts = new VBox(2);
		Label nameLabel = new Label(name);
		nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e6edf3;");
		nameLabel.setWrapText(true);
		nameLabel.setMaxWidth(140);
		Label idLabel = new Label(id);
		idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7d8590;");
		texts.getChildren().addAll(nameLabel, idLabel);
		HBox.setHgrow(texts, Priority.ALWAYS);

		row.getChildren().addAll(avatar, texts);
		card.getChildren().add(row);
		return card;
	}

	private Button createNavButton(String text, FontAwesomeSolid iconCode) {
		FontIcon icon = new FontIcon(iconCode);
		icon.setIconSize(14);
		icon.setIconColor(Color.web(COLOR_ICON_DEFAULT));
		navIcons.put(text, icon);

		Label label = new Label(text);
		label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + COLOR_LABEL_DEFAULT + ";");

		HBox content = new HBox(10);
		content.setAlignment(Pos.CENTER_LEFT);
		content.setPadding(new Insets(0, 2, 0, 2));
		content.getChildren().addAll(icon, label);

		Button button = new Button();
		button.setGraphic(content);
		button.getStyleClass().add("nav-item");
		button.setMaxWidth(Double.MAX_VALUE);
		button.setOnAction(e -> select(text));
		return button;
	}

	public void setOnNavigate(Consumer<String> handler) {
		this.onNavigate = handler;
	}

	public void setOnSignOut(Consumer<Void> handler) {
		this.onSignOut = handler;
	}

	public void select(String name) {
		navButtons.forEach((key, btn) -> {
			boolean active = key.equals(name);
			btn.getStyleClass().removeAll("nav-item-active");
			if (active) {
				btn.getStyleClass().add("nav-item-active");
			}
			// Update icon and label color
			FontIcon icon = navIcons.get(key);
			if (icon != null) {
				icon.setIconColor(Color.web(active ? COLOR_ICON_ACTIVE : COLOR_ICON_DEFAULT));
			}
			HBox content = (HBox) btn.getGraphic();
			if (content != null && content.getChildren().size() > 1) {
				Label lbl = (Label) content.getChildren().get(1);
				lbl.setStyle(
						"-fx-font-size: 13px; -fx-text-fill: " + (active ? COLOR_LABEL_ACTIVE : COLOR_LABEL_DEFAULT)
								+ ";" + (active ? " -fx-font-weight: bold;" : ""));
			}
		});
		if (onNavigate != null) {
			onNavigate.accept(name);
		}
	}
}
