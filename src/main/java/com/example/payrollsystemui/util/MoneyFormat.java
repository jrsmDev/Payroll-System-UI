package com.example.payrollsystemui.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormat {
	private static final NumberFormat PHP = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

	private MoneyFormat() {
	}

	public static String format(double amount) {
		return PHP.format(amount);
	}

	public static String formatHours(double hours) {
		return String.format(Locale.US, "%.2fh", hours);
	}
}
