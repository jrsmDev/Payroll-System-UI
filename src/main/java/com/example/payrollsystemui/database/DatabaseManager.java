package com.example.payrollsystemui.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.mindrot.jbcrypt.BCrypt;

public final class DatabaseManager {
	private static HikariDataSource dataSource;

	private DatabaseManager() {
	}

	public static void initialize() {
		if (dataSource != null) {
			return;
		}
		Properties props = loadProperties();
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(props.getProperty("db.url"));
		config.setUsername(props.getProperty("db.user"));
		config.setPassword(props.getProperty("db.password", ""));
		config.setMaximumPoolSize(10);
		config.setPoolName("PayrollPool");
		dataSource = new HikariDataSource(config);
		runScript("db/schema.sql");
		seedIfEmpty();
	}

	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			initialize();
		}
		return dataSource.getConnection();
	}

	public static void shutdown() {
		if (dataSource != null) {
			dataSource.close();
			dataSource = null;
		}
	}

	private static Properties loadProperties() {
		Properties props = new Properties();
		try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (in != null) {
				props.load(in);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load application.properties", e);
		}
		props.putIfAbsent("db.url", "jdbc:mysql://localhost:3306/payroll_db?useSSL=false&allowPublicKeyRetrieval=true");
		props.putIfAbsent("db.user", "root");
		props.putIfAbsent("db.password", "");
		// Environment variables override the properties file
		String envUser = System.getenv("PAYROLL_DB_USER");
		String envPassword = System.getenv("PAYROLL_DB_PASSWORD");
		if (envUser != null && !envUser.isBlank()) {
			props.setProperty("db.user", envUser);
		}
		if (envPassword != null && !envPassword.isBlank()) {
			props.setProperty("db.password", envPassword);
		}
		return props;
	}

	private static void runScript(String resourcePath) {
		String sql = readResource(resourcePath);
		for (String statement : sql.split(";")) {
			String trimmed = statement.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
				stmt.execute(trimmed);
			} catch (SQLException e) {
				throw new IllegalStateException("Failed executing SQL script: " + resourcePath, e);
			}
		}
	}

	private static void seedIfEmpty() {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM users")) {
			if (rs.next() && rs.getInt("c") > 0) {
				return;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Failed checking seed state", e);
		}
		runScript("db/seed.sql");
		String hash = BCrypt.hashpw("admin123", BCrypt.gensalt(10));
		try (Connection conn = getConnection();
				PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ?")) {
			ps.setString(1, hash);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Failed updating password hashes", e);
		}
	}

	private static String readResource(String path) {
		try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream(path)) {
			if (in == null) {
				throw new IllegalStateException("Missing resource: " + path);
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read resource: " + path, e);
		}
	}
}
