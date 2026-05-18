package com.example.payrollsystemui.ui.employee;

import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.ui.components.Sidebar;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class EmployeeShell extends BorderPane {
	private final StackPane content = new StackPane();
	private final Runnable onSignOut;

	public EmployeeShell(Runnable onSignOut) {
		this.onSignOut = onSignOut;
		Map<String, FontAwesomeSolid> items = new LinkedHashMap<>();
		items.put("Dashboard", FontAwesomeSolid.TH);
		items.put("My Attendance", FontAwesomeSolid.CLOCK);
		items.put("File Payroll", FontAwesomeSolid.FILE_ALT);
		items.put("My Payslips", FontAwesomeSolid.FILE_INVOICE);

		Sidebar sidebar = new Sidebar("MY WORKSPACE", items);
		sidebar.setOnNavigate(this::showPage);
		sidebar.setOnSignOut(v -> {
			Session.clear();
			onSignOut.run();
		});

		setLeft(sidebar);
		setCenter(content);
		showPage("Dashboard");
		sidebar.select("Dashboard");
	}

	private void showPage(String name) {
		content.getChildren().setAll(switch (name) {
			case "My Attendance" -> new EmployeeAttendanceView();
			case "File Payroll" -> new EmployeeFilePayrollView();
			case "My Payslips" -> new EmployeePayslipsView();
			default -> new EmployeeDashboardView();
		});
	}
}
