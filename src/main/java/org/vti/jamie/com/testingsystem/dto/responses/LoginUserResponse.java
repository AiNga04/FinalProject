package org.vti.jamie.com.testingsystem.dto.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;

@Getter
@Setter
@NoArgsConstructor
public class LoginUserResponse{
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private EN_AccountType accountType;
}