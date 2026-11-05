package org.vti.jamie.com.testingsystem.dto.requests.department;

import jakarta.validation.constraints.NotNull;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

public record UpdateDepartmentStatusRequest(
        @NotNull(message = "Status không được để trống") EN_Status status
) {
}
