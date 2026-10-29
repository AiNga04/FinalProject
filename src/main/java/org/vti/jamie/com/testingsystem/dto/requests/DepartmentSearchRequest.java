package org.vti.jamie.com.testingsystem.dto.requests;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.LocalDate;

@Getter
@Setter
public class DepartmentSearchRequest {
    @Min(0)
    private int page = 0;
    @Min(1) @Max(100)
    private int size = 10;
    @Size(max = 100)
    private String search;
    @Size(max = 30)
    private String type;
    private EN_Status status;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate minCreatedDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maxCreatedDate;
}
