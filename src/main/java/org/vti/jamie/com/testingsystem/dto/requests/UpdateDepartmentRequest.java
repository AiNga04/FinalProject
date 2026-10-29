package org.vti.jamie.com.testingsystem.dto.requests;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDepartmentRequest {
    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = "(?sU).*\\S.*", message = "Tên phòng ban không được để trống")
    @Size(max = 50)
    private String name;

    @JsonSetter(nulls = Nulls.FAIL)
    @Pattern(regexp = "(?sU).*\\S.*", message = "Loại phòng ban không được để trống")
    @Size(max = 30)
    private String type;
}
