package org.vti.jamie.com.testingsystem.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import org.vti.jamie.com.testingsystem.form.department.DepartmentSearchForm;
import org.vti.jamie.com.testingsystem.entity.Department;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

public final class DepartmentSpecification {

    private DepartmentSpecification() {
    }

    public static Specification<Department> buildWhere(
            DepartmentSearchForm request) {

        Specification<Department> where =
                request.getStatus() == null
                        ? new CustomSpecification("excludeDeleted", EN_Status.DELETED)
                        : new CustomSpecification("status", request.getStatus());

        if (StringUtils.hasText(request.getSearch())) {
            where = where.and(
                    new CustomSpecification("search", request.getSearch())
            );
        }

        if (StringUtils.hasText(request.getType())) {
            where = where.and(
                    new CustomSpecification("type", request.getType())
            );
        }

        if (request.getMinCreatedDate() != null) {
            where = where.and(
                    new CustomSpecification(
                            "minCreatedDate",
                            request.getMinCreatedDate()
                    )
            );
        }

        if (request.getMaxCreatedDate() != null) {
            where = where.and(
                    new CustomSpecification(
                            "maxCreatedDate",
                            request.getMaxCreatedDate()
                    )
            );
        }

        return where;
    }

    @RequiredArgsConstructor
    private static class CustomSpecification
            implements Specification<Department> {

        private final String field;
        private final Object value;

        @Override
        public Predicate toPredicate(
                Root<Department> root,
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

                case "search":
                    String search = ((String) value)
                            .strip()
                            .toLowerCase(Locale.ROOT)
                            .replace("\\", "\\\\")
                            .replace("%", "\\%")
                            .replace("_", "\\_");

                    return cb.like(
                            cb.lower(root.get("name")),
                            "%" + search + "%",
                            '\\'
                    );

                case "type":
                    return cb.equal(
                            cb.lower(root.get("type")),
                            ((String) value)
                                    .strip()
                                    .toLowerCase(Locale.ROOT)
                    );

                case "minCreatedDate":
                    return cb.greaterThanOrEqualTo(
                            root.get("createdAt"),
                            ((LocalDate) value).atStartOfDay()
                    );

                case "maxCreatedDate":
                    return cb.lessThanOrEqualTo(
                            root.get("createdAt"),
                            ((LocalDate) value).atTime(LocalTime.MAX)
                    );

                default:
                    throw new IllegalArgumentException(
                            "Điều kiện Department không được hỗ trợ: " + field
                    );
            }
        }
    }
}