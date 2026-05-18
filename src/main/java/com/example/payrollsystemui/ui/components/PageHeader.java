package com.example.payrollsystemui.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PageHeader extends VBox {
	private final Label subtitleLabel;

	public PageHeader(String title, String subtitle, Node rightAction) {
		setSpacing(3);
		setPrefHeight(USE_COMPUTED_SIZE);

		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("page-title");

		subtitleLabel = new Label(subtitle);
		subtitleLabel.getStyleClass().add("page-subtitle");

		if (rightAction == null) {
			getChildren().addAll(titleLabel, subtitleLabel);
		} else {
			HBox row = new HBox();
			row.setAlignment(Pos.CENTER_LEFT);
			VBox left = new VBox(3, titleLabel, subtitleLabel);
			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);
			row.getChildren().addAll(left, spacer, rightAction);
			getChildren().add(row);
		}
	}

	public void setSubtitle(String subtitle) {
		subtitleLabel.setText(subtitle);
	}
}
