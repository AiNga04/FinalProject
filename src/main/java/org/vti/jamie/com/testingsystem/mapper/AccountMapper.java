package org.vti.jamie.com.testingsystem.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.vti.jamie.com.testingsystem.form.account.CreateAccountForm;
import org.vti.jamie.com.testingsystem.form.account.UpdateAccountForm;
import org.vti.jamie.com.testingsystem.dto.account.AccountResponse;
import org.vti.jamie.com.testingsystem.dto.account.AccountExportRow;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final ModelMapper modelMapper;

    public Account toEntity(CreateAccountForm request) {
        Account account = modelMapper.map(request, Account.class);
        account.setUsername(request.getUsername().strip());
        account.setEmail(request.getEmail().strip());
        account.setFirstName(request.getFirstName().strip());
        account.setLastName(request.getLastName().strip());
        account.setAccountType(request.getAccountType() == null
                ? EN_AccountType.EMPLOYEE : request.getAccountType());
        return account;
    }

    public void updateEntity(
            UpdateAccountForm request,
            Account account) {

        modelMapper.map(request, account);
        if (request.getUsername() != null) {
            account.setUsername(request.getUsername().strip());
        }
        if (request.getEmail() != null) {
            account.setEmail(request.getEmail().strip());
        }
        if (request.getFirstName() != null) {
            account.setFirstName(request.getFirstName().strip());
        }
        if (request.getLastName() != null) {
            account.setLastName(request.getLastName().strip());
        }
        if (request.isDepartmentIdPresent()) {
            account.setDepartmentId(request.getDepartmentId());
        }
    }

    public AccountResponse toResponse(Account account) {
        return modelMapper.map(account, AccountResponse.class);
    }

    public AccountExportRow toExportRow(Account account) {
        AccountExportRow row = new AccountExportRow();
        row.setId(account.getId());
        row.setUsername(account.getUsername());
        row.setEmail(account.getEmail());
        row.setLastName(account.getLastName());
        row.setFirstName(account.getFirstName());
        row.setAccountType(account.getAccountType().name());
        row.setDepartmentId(account.getDepartmentId());
        row.setStatus(account.getStatus().name());
        row.setCreatedAt(account.getCreatedAt());
        return row;
    }
}
