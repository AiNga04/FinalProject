package org.vti.jamie.com.testingsystem.dto.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AccountResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private EN_AccountType accountType;
    private EN_Status status;
    private Long departmentId;
    private LocalDateTime createdAt;
}
