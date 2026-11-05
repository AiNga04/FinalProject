package org.vti.jamie.com.testingsystem.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.Conditions;
import org.vti.jamie.com.testingsystem.form.account.UpdateAccountForm;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.Department;
import org.vti.jamie.com.testingsystem.form.department.UpdateDepartmentForm;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Update từng trường: bỏ qua trường không truyền, xử lý departmentId riêng
        // để phân biệt không truyền với chủ động gửi null (bỏ phòng ban).
        modelMapper.typeMap(UpdateAccountForm.class, Account.class)
                .setPropertyCondition(Conditions.isNotNull())
                .addMappings(mapping -> mapping.skip(Account::setDepartmentId));

        modelMapper.typeMap(UpdateDepartmentForm.class, Department.class)
                .setPropertyCondition(Conditions.isNotNull());

        return modelMapper;
    }
}
