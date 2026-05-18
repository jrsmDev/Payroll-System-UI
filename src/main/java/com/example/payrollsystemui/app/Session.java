package com.example.payrollsystemui.app;

import com.example.payrollsystemui.model.Employee;
import com.example.payrollsystemui.model.User;

// Holds the currently logged-in user and employee for the entire app session.
// All fields are static so any part of the app can access them without passing references around.
public final class Session {

	private static User currentUser; // The logged-in User (contains role, username, etc.)
	private static Employee currentEmployee; // The Employee record linked to that user (null for admin)

	// Prevent instantiation — this is a utility class with only static methods
	private Session() {
	}

	// Call this right after a successful login to store who is logged in
	public static void set(User user, Employee employee) {
		currentUser = user;
		currentEmployee = employee;
	}

	// Call this on logout to wipe the session data
	public static void clear() {
		currentUser = null;
		currentEmployee = null;
	}

	// Returns the currently logged-in User object
	public static User user() {
		return currentUser;
	}

	// Returns the Employee linked to the logged-in user (may be null for admin
	// accounts)
	public static Employee employee() {
		return currentEmployee;
	}
}
