package org.vti.jamie.com.testingsystem.dto.account;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AccountExportRow {
    private Long id;
    private String username;
    private String email;
    private String lastName;
    private String firstName;
    private String accountType;
    private Long departmentId;
    private String status;
    private LocalDateTime createdAt;
}
