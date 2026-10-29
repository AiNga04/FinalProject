package org.vti.jamie.com.testingsystem.dto.requests;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;

@Getter
@Setter
@NoArgsConstructor
public class CreateAccountRequest {
    @NotBlank @Size(max = 50)
    private String username;
    @NotBlank @Email @Size(max = 254)
    private String email;
    @NotBlank @Size(min = 6, max = 72)
    private String password;
    @NotBlank @Size(max = 50)
    private String firstName;
    @NotBlank @Size(max = 50)
    private String lastName;
    @Schema(description = "Không truyền thì mặc định EMPLOYEE", defaultValue = "EMPLOYEE")
    private EN_AccountType accountType = EN_AccountType.EMPLOYEE;
    @Positive
    @Schema(description = "Không truyền thì không thuộc phòng ban nào")
    private Long departmentId;
}
