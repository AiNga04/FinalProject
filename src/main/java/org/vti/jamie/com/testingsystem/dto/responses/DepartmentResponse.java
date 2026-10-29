package org.vti.jamie.com.testingsystem.dto.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private String type;
    private long totalMember;
    private EN_Status status;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}
