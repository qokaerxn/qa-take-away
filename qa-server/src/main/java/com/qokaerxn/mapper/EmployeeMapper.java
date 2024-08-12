package com.qokaerxn.mapper;

import com.github.pagehelper.Page;
import com.qokaerxn.annotation.AutoFill;
import com.qokaerxn.dto.EmployeePageQueryDTO;
import com.qokaerxn.entity.Employee;
import com.qokaerxn.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EmployeeMapper {

    @Select("Select * from employee where username = #{username}")
    Employee getByUserName(String username);

    @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user) " +
            "values" +
            "(#{name},#{username},#{password}, #{phone}, #{sex}, #{idNumber},  #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    @Select("select * from employee where id = #{id}")
    Employee getEmployeeById(Long id);

}
