package com.example.payrollsystemui.model;

public enum EmploymentType {
	REGULAR, CONTRACTUAL, PART_TIME, PROBATIONARY;

	public static EmploymentType fromDb(String value) {
		return EmploymentType.valueOf(value.toUpperCase());
	}

	public String displayName() {
		return switch (this) {
			case REGULAR -> "Regular";
			case CONTRACTUAL -> "Contractual";
			case PART_TIME -> "Part-Time";
			case PROBATIONARY -> "Probationary";
		};
	}
}
