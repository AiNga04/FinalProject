package org.vti.jamie.com.testingsystem.seeder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.repository.IAccountRepository;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

@Configuration
@Profile("dev")
public class DevAccountSeeder {
    @Bean
    public CommandLineRunner seedDevAccounts(
            IAccountRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${DEV_ADMIN_PASSWORD}") String adminPassword,
            @Value("${DEV_EMPLOYEE_PASSWORD}") String employeePassword) {

        return args -> {
            createIfMissing(
                    repository, passwordEncoder,
                    "admin", "admin@testing.invalid",
                    adminPassword, EN_AccountType.ADMIN
            );

            createIfMissing(
                    repository, passwordEncoder,
                    "employee", "employee@testing.invalid",
                    employeePassword, EN_AccountType.EMPLOYEE
            );
        };
    }

    private void createIfMissing(
            IAccountRepository repository,
            PasswordEncoder encoder,
            String username,
            String email,
            String rawPassword,
            EN_AccountType accountType) {

        if (repository.existsByUsername(username)) {
            return;
        }

        Account account = new Account();

        account.setUsername(username);
        account.setEmail(email);
        account.setPasswordHash(encoder.encode(rawPassword));
        account.setFirstName("Testing");
        account.setLastName(username);
        account.setAccountType(accountType);
        account.setStatus(EN_Status.ACTIVE);

        repository.save(account);
    }
}
