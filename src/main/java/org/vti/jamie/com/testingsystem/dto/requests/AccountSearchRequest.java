package org.vti.jamie.com.testingsystem.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

@Getter
@Setter
public class AccountSearchRequest {
    @Min(0)
    private int page = 0;
    @Min(1) @Max(100)
    private int size = 10;
    @Size(max = 100)
    private String search;
    private EN_AccountType accountType;
    @Positive
    private Long departmentId;
    private EN_Status status;
}
