package org.vti.jamie.com.testingsystem.dto.requests;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;

@Getter
@Setter
@NoArgsConstructor
public class UpdateAccountRequest {
    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = "(?sU).*\\S.*", message = "Username không được để trống")
    @Size(max = 50)
    private String username;
    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = "(?sU).*\\S.*", message = "Email không được để trống")
    @Email @Size(max = 254)
    private String email;
    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = "(?sU).*\\S.*", message = "First name không được để trống")
    @Size(max = 50)
    private String firstName;
    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = "(?sU).*\\S.*", message = "Last name không được để trống")
    @Size(max = 50)
    private String lastName;
    @JsonSetter(nulls = Nulls.FAIL)
    private EN_AccountType accountType;
    @Positive
    private Long departmentId;

    @JsonIgnore
    private boolean departmentIdPresent;

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
        this.departmentIdPresent = true;
    }
}
