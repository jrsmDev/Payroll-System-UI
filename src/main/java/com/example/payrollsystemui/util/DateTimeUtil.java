package com.example.payrollsystemui.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
	public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	public static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

	private DateTimeUtil() {
	}

	public static String formatDate(LocalDate date) {
		return date.format(DATE);
	}

	public static String formatLongDate(LocalDate date) {
		return date.format(LONG_DATE);
	}

	public static String formatDateTime(LocalDateTime dateTime) {
		return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}
