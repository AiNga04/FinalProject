package org.vti.jamie.com.testingsystem.form.account;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class AccountSearchForm {
    @Size(max = 100)
    private String search;

    private EN_AccountType accountType;

    @Positive
    private Long departmentId;

    private EN_Status status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate minCreatedDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maxCreatedDate;
}
