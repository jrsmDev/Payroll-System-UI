package com.example.payrollsystemui.model;

public record User(String employeeId, String passwordHash, UserRole role) {
}
