package org.vti.jamie.com.testingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.vti.jamie.com.testingsystem.util.common.EN_Status;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query(value = "SELECT COUNT(*) FROM department WHERE id = :id AND status = 'ACTIVE'", nativeQuery = true)
    long countActiveDepartment(@Param("id") Long id);

    long countByDepartmentIdAndStatusNot(Long departmentId, EN_Status status);

    @Query("select account.departmentId as departmentId, count(account) as totalMember "
            + "from Account account where account.departmentId in :ids and account.status <> :status "
            + "group by account.departmentId")
    List<DepartmentMemberCount> countMembersByDepartmentIds(@Param("ids") Collection<Long> ids,
                                                           @Param("status") EN_Status status);
}
