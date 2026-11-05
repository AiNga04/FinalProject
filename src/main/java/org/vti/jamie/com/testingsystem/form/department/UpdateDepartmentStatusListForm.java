package org.vti.jamie.com.testingsystem.form.department;

import jakarta.validation.constraints.*;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.List;

public record UpdateDepartmentStatusListForm(
        @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids,

        @NotNull(message = "Status không được để trống") EN_Status status
) {
}
