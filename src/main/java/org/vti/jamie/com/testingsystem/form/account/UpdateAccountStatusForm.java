package org.vti.jamie.com.testingsystem.form.account;

import jakarta.validation.constraints.NotNull;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

public record UpdateAccountStatusForm(
        @NotNull(message = "Status không được để trống") EN_Status status
) {
}
