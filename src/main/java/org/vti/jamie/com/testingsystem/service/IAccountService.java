package org.vti.jamie.com.testingsystem.service;

import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.testingsystem.form.account.AccountSearchForm;
import org.vti.jamie.com.testingsystem.form.account.CreateAccountForm;
import org.vti.jamie.com.testingsystem.form.account.UpdateAccountForm;
import org.vti.jamie.com.testingsystem.dto.account.AccountResponse;
import org.springframework.data.domain.Page;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;
import java.util.List;

public interface IAccountService {
    AccountResponse getMe(String subject);

    Page<AccountResponse> getAccounts(AccountSearchForm request, Pageable pageable);

    AccountResponse getAccount(Long id);

    AccountResponse create(CreateAccountForm request, String subject);

    AccountResponse update(Long id, UpdateAccountForm request, String subject);

    List<AccountResponse> updateStatusList(List<Long> ids, EN_Status status, String subject);

    AccountResponse updateStatus(Long id, EN_Status status, String subject);
}
