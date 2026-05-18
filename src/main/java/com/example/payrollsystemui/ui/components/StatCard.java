package com.example.payrollsystemui.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StatCard extends VBox {
	private final Label valueLabel = new Label();

	public StatCard(String title, String value, String valueStyleClass) {
		getStyleClass().add("card");
		setSpacing(4);
		setStyle(getStyle() + "-fx-padding: 16 20;");

		Label titleLabel = new Label(title.toUpperCase());
		titleLabel.getStyleClass().add("stat-label");

		valueLabel.setText(value);
		valueLabel.getStyleClass().add("stat-value");
		if (valueStyleClass != null) {
			valueLabel.getStyleClass().add(valueStyleClass);
		}
		getChildren().addAll(titleLabel, valueLabel);
	}

	public void setValue(String value) {
		valueLabel.setText(value);
	}
}
