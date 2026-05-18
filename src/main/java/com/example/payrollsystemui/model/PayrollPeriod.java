package com.example.payrollsystemui.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public final class PayrollPeriod {
	private PayrollPeriod() {
	}

	public record Period(String label, LocalDate start, LocalDate end) {
	}

	public static List<Period> recentPeriods(int count) {
		List<Period> periods = new ArrayList<>();
		YearMonth month = YearMonth.now();
		for (int i = 0; i < count; i++) {
			periods.add(firstCutoff(month));
			periods.add(secondCutoff(month));
			month = month.minusMonths(1);
		}
		return periods;
	}

	public static Period firstCutoff(YearMonth month) {
		LocalDate start = month.atDay(1);
		LocalDate end = month.atDay(Math.min(15, month.lengthOfMonth()));
		String label = month + " 1st Cutoff (1-15)";
		return new Period(label, start, end);
	}

	public static Period secondCutoff(YearMonth month) {
		int last = month.lengthOfMonth();
		LocalDate start = month.atDay(16);
		LocalDate end = month.atDay(last);
		String label = month + " 2nd Cutoff (16-" + last + ")";
		return new Period(label, start, end);
	}

	public static boolean contains(Period period, LocalDate date) {
		return !date.isBefore(period.start()) && !date.isAfter(period.end());
	}
}
