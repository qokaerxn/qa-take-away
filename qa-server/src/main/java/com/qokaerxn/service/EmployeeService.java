package com.qokaerxn.service;


import com.qokaerxn.dto.EmployeeDTO;
import com.qokaerxn.dto.EmployeeLoginDTO;
import com.qokaerxn.dto.EmployeePageQueryDTO;
import com.qokaerxn.entity.Employee;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.result.Result;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    Employee getEmployeeById(Long id);

    void update(EmployeeDTO employeeDTO);
}
