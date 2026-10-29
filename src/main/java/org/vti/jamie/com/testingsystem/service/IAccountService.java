package org.vti.jamie.com.testingsystem.service;

import org.vti.jamie.com.testingsystem.dto.responses.AccountResponse;

public interface IAccountService {
    AccountResponse getMe(String subject);
}
