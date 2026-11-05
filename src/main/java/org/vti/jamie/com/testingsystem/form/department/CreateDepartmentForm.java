package org.vti.jamie.com.testingsystem.form.department;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateDepartmentForm {
    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "Loại phòng ban không được để trống")
    @Size(max = 30)
    private String type;

    @Size(max = 100)
    private List<@NotNull @Positive Long> accountIds = List.of();
}
