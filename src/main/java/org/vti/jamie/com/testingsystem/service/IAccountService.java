package org.vti.jamie.com.testingsystem.service;

import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.testingsystem.dto.requests.account.AccountSearchRequest;
import org.vti.jamie.com.testingsystem.dto.requests.account.CreateAccountRequest;
import org.vti.jamie.com.testingsystem.dto.requests.account.UpdateAccountRequest;
import org.vti.jamie.com.testingsystem.dto.responses.account.AccountResponse;
import org.springframework.data.domain.Page;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import java.util.List;

public interface IAccountService {
    AccountResponse getMe(String subject);

    Page<AccountResponse> getAccounts(AccountSearchRequest request, Pageable pageable);

    AccountResponse getAccount(Long id);

    AccountResponse create(CreateAccountRequest request, String subject);

    AccountResponse update(Long id, UpdateAccountRequest request, String subject);

    List<AccountResponse> updateStatusList(List<Long> ids, EN_Status status, String subject);

    AccountResponse updateStatus(Long id, EN_Status status, String subject);
}
