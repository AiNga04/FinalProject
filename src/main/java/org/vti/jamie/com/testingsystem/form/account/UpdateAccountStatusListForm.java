package org.vti.jamie.com.testingsystem.form.account;

import jakarta.validation.constraints.*;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.List;

public record UpdateAccountStatusListForm(
        @NotEmpty @Size(max = 100) List<@NotNull @Positive Long> ids,

        @NotNull EN_Status status
) {
}
