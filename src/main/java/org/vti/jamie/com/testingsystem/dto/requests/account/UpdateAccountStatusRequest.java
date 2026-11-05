package org.vti.jamie.com.testingsystem.dto.requests;

import jakarta.validation.constraints.NotNull;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

public record UpdateAccountStatusRequest(
        @NotNull(message = "Status không được để trống") EN_Status status
) {
}
