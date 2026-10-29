package org.vti.jamie.com.testingsystem.dto.responses;

import org.vti.jamie.com.testingsystem.entity.Account;

import java.time.LocalDateTime;

public record AccountResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String accountType,
    String status,
    Long departmentId,
    LocalDateTime createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getFirstName(),
                account.getLastName(),
                account.getAccountType().name(),
                account.getStatus().name(),
                account.getDepartmentId(),
                account.getCreatedAt()
        );
    }
}
