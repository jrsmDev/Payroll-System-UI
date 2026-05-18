package com.example.payrollsystemui.ui.admin;

import com.example.payrollsystemui.app.Session;
import com.example.payrollsystemui.ui.components.Sidebar;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class AdminShell extends BorderPane {
	private final StackPane content = new StackPane();
	private final Runnable onSignOut;

	public AdminShell(Runnable onSignOut) {
		this.onSignOut = onSignOut;
		Map<String, FontAwesomeSolid> items = new LinkedHashMap<>();
		items.put("Dashboard", FontAwesomeSolid.TH);
		items.put("Employees", FontAwesomeSolid.USERS);
		items.put("Attendance", FontAwesomeSolid.CLOCK);
		items.put("Approvals", FontAwesomeSolid.CHECK_CIRCLE);
		items.put("Reports", FontAwesomeSolid.CHART_BAR);

		Sidebar sidebar = new Sidebar("ADMINISTRATION", items);
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
			case "Employees" -> new AdminEmployeesView();
			case "Attendance" -> new AdminAttendanceView();
			case "Approvals" -> new AdminApprovalsView();
			case "Reports" -> new AdminReportsView();
			default -> new AdminDashboardView();
		});
	}
}
