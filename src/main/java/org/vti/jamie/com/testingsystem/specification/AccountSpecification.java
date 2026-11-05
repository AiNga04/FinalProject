package org.vti.jamie.com.testingsystem.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.vti.jamie.com.testingsystem.dto.requests.account.AccountSearchRequest;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.util.common.EN_AccountType;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.Locale;

public final class AccountSpecification {

    private AccountSpecification() {
    }

    public static Specification<Account> buildWhere(
            AccountSearchRequest request) {

        // Luôn có điều kiện ban đầu, không cần where = null.
        Specification<Account> where =
                request.getStatus() == null
                        ? new CustomSpecification("excludeDeleted", EN_Status.DELETED)
                        : new CustomSpecification("status", request.getStatus());

        if (StringUtils.hasText(request.getSearch())) {
            where = where.and(
                    new CustomSpecification("search", request.getSearch())
            );
        }

        if (request.getAccountType() != null) {
            where = where.and(
                    new CustomSpecification(
                            "accountType",
                            request.getAccountType()
                    )
            );
        }

        if (request.getDepartmentId() != null) {
            where = where.and(
                    new CustomSpecification(
                            "departmentId",
                            request.getDepartmentId()
                    )
            );
        }

        return where;
    }

    @RequiredArgsConstructor
    private static class CustomSpecification
            implements Specification<Account> {

        private final String field;
        private final Object value;

        @Override
        public Predicate toPredicate(
                Root<Account> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) {

            switch (field) {
                case "excludeDeleted":
                    return cb.notEqual(
                            root.get("status"),
                            (EN_Status) value
                    );

                case "status":
                    return cb.equal(
                            root.get("status"),
                            (EN_Status) value
                    );

                case "accountType":
                    return cb.equal(
                            root.get("accountType"),
                            (EN_AccountType) value
                    );

                case "departmentId":
                    return cb.equal(
                            root.get("departmentId"),
                            (Long) value
                    );

                case "search":
                    String search = ((String) value)
                            .strip()
                            .toLowerCase(Locale.ROOT)
                            .replace("\\", "\\\\")
                            .replace("%", "\\%")
                            .replace("_", "\\_");

                    String pattern = "%" + search + "%";

                    return cb.or(
                            cb.like(
                                    cb.lower(root.get("username")),
                                    pattern,
                                    '\\'
                            ),
                            cb.like(
                                    cb.lower(root.get("firstName")),
                                    pattern,
                                    '\\'
                            ),
                            cb.like(
                                    cb.lower(root.get("lastName")),
                                    pattern,
                                    '\\'
                            )
                    );

                default:
                    throw new IllegalArgumentException(
                            "Điều kiện Account không được hỗ trợ: " + field
                    );
            }
        }
    }
}