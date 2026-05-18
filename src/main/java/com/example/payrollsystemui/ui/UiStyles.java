package com.example.payrollsystemui.ui;

import com.example.payrollsystemui.model.EmploymentType;
import javafx.scene.control.Label;

public final class UiStyles {
	private UiStyles() {
	}

	public static void applyBadge(Label label, String styleClass) {
		label.getStyleClass().add(styleClass);
	}

	public static String typeBadgeClass(EmploymentType type) {
		return switch (type) {
			case REGULAR -> "badge-type-regular";
			case CONTRACTUAL -> "badge-type-contractual";
			case PART_TIME -> "badge-type-contractual";
			case PROBATIONARY -> "badge-type-probationary";
		};
	}
}
