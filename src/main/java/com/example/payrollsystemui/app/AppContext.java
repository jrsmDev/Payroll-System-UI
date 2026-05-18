package com.example.payrollsystemui.app;

import com.example.payrollsystemui.service.AuthService;
import com.example.payrollsystemui.service.PayrollService;

public final class AppContext {
	private static final AuthService AUTH_SERVICE = new AuthService();
	private static final PayrollService PAYROLL_SERVICE = new PayrollService();

	private AppContext() {
	}

	public static AuthService auth() {
		return AUTH_SERVICE;
	}

	public static PayrollService payroll() {
		return PAYROLL_SERVICE;
	}
}
