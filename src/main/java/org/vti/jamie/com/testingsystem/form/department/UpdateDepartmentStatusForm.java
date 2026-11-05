package org.vti.jamie.com.testingsystem.form.department;

import jakarta.validation.constraints.NotNull;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

public record UpdateDepartmentStatusForm(
        @NotNull(message = "Status không được để trống") EN_Status status
) {
}
