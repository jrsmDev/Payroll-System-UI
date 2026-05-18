package com.example.payrollsystemui.service;

import com.example.payrollsystemui.model.GovernmentContribution;

/**
 * Philippine government contributions (simplified 2025/2026 rates for academic
 * use).
 */
public final class ContributionCalculator {
	private ContributionCalculator() {
	}

	public static GovernmentContribution compute(double monthlyBasic, double grossForPeriod) {
		double monthlyEquivalent = grossForPeriod * 2;
		double basis = Math.max(monthlyBasic, monthlyEquivalent);

		double sssEmployee = sssEmployeeShare(basis);
		double sssEmployer = sssEmployerShare(basis);
		double ecc = 10;
		double philHealthEmployee = basis * 0.025 / 2;
		double philHealthEmployer = basis * 0.05 / 2;
		double pagibigEmployee = Math.min(basis * 0.02, 200);
		double pagibigEmployer = Math.min(basis * 0.02, 200);
		double tax = withholdingTax(grossForPeriod);

		return new GovernmentContribution(sssEmployee, sssEmployer, philHealthEmployee, philHealthEmployer,
				pagibigEmployee, pagibigEmployer, ecc, tax);
	}

	private static double sssEmployeeShare(double monthlySalary) {
		if (monthlySalary <= 5249.99) {
			return 250;
		}
		if (monthlySalary <= 5749.99) {
			return 275;
		}
		if (monthlySalary <= 6249.99) {
			return 300;
		}
		if (monthlySalary <= 6749.99) {
			return 325;
		}
		if (monthlySalary <= 7249.99) {
			return 350;
		}
		if (monthlySalary <= 7749.99) {
			return 375;
		}
		if (monthlySalary <= 8249.99) {
			return 400;
		}
		if (monthlySalary <= 8749.99) {
			return 425;
		}
		if (monthlySalary <= 9249.99) {
			return 450;
		}
		if (monthlySalary <= 9749.99) {
			return 475;
		}
		if (monthlySalary <= 10249.99) {
			return 500;
		}
		if (monthlySalary <= 10749.99) {
			return 525;
		}
		if (monthlySalary <= 11249.99) {
			return 550;
		}
		if (monthlySalary <= 11749.99) {
			return 575;
		}
		if (monthlySalary <= 12249.99) {
			return 600;
		}
		if (monthlySalary <= 12749.99) {
			return 625;
		}
		if (monthlySalary <= 13249.99) {
			return 650;
		}
		if (monthlySalary <= 13749.99) {
			return 675;
		}
		if (monthlySalary <= 14249.99) {
			return 700;
		}
		if (monthlySalary <= 14749.99) {
			return 725;
		}
		if (monthlySalary <= 15249.99) {
			return 750;
		}
		if (monthlySalary <= 15749.99) {
			return 775;
		}
		if (monthlySalary <= 16249.99) {
			return 800;
		}
		if (monthlySalary <= 16749.99) {
			return 825;
		}
		if (monthlySalary <= 17249.99) {
			return 850;
		}
		if (monthlySalary <= 17749.99) {
			return 875;
		}
		if (monthlySalary <= 18249.99) {
			return 900;
		}
		if (monthlySalary <= 18749.99) {
			return 925;
		}
		if (monthlySalary <= 19249.99) {
			return 950;
		}
		if (monthlySalary <= 19749.99) {
			return 975;
		}
		return 1000;
	}

	private static double sssEmployerShare(double monthlySalary) {
		return sssEmployeeShare(monthlySalary) + 10;
	}

	private static double withholdingTax(double taxableMonthly) {
		double annual = taxableMonthly * 12;
		if (annual <= 250000) {
			return 0;
		}
		if (annual <= 400000) {
			return (annual - 250000) * 0.15 / 12;
		}
		if (annual <= 800000) {
			return (22500 + (annual - 400000) * 0.20) / 12;
		}
		if (annual <= 2000000) {
			return (102500 + (annual - 800000) * 0.25) / 12;
		}
		if (annual <= 8000000) {
			return (402500 + (annual - 2000000) * 0.30) / 12;
		}
		return (2202500 + (annual - 8000000) * 0.35) / 12;
	}
}
