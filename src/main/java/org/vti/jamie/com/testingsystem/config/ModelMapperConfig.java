package org.vti.jamie.com.testingsystem.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.Conditions;
import org.vti.jamie.com.testingsystem.dto.requests.UpdateAccountRequest;
import org.vti.jamie.com.testingsystem.entity.Account;
import org.vti.jamie.com.testingsystem.entity.Department;
import org.vti.jamie.com.testingsystem.dto.requests.UpdateDepartmentRequest;
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
        modelMapper.typeMap(UpdateAccountRequest.class, Account.class)
                .setPropertyCondition(Conditions.isNotNull())
                .addMappings(mapping -> mapping.skip(Account::setDepartmentId));

        modelMapper.typeMap(UpdateDepartmentRequest.class, Department.class)
                .setPropertyCondition(Conditions.isNotNull());

        return modelMapper;
    }
}
