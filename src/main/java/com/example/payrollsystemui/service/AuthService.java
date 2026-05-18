package com.example.payrollsystemui.service;

import com.example.payrollsystemui.model.User;
import com.example.payrollsystemui.repository.AccountRepository;
import java.sql.SQLException;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
	private final AccountRepository accountRepository = new AccountRepository();

	public Optional<User> authenticate(String employeeId, String password) throws SQLException {
		Optional<User> user = accountRepository.findByEmployeeId(employeeId.trim().toUpperCase());
		if (user.isEmpty()) {
			return Optional.empty();
		}
		User found = user.get();
		if (!BCrypt.checkpw(password, found.passwordHash())) {
			return Optional.empty();
		}
		return Optional.of(found);
	}
}
