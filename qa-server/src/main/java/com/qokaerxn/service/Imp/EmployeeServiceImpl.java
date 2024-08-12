package com.qokaerxn.service.Imp;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qokaerxn.constant.MessageConstant;
import com.qokaerxn.constant.PasswordConstant;
import com.qokaerxn.constant.StatusConstant;
import com.qokaerxn.dto.EmployeeDTO;
import com.qokaerxn.dto.EmployeeLoginDTO;
import com.qokaerxn.dto.EmployeePageQueryDTO;
import com.qokaerxn.entity.Employee;
import com.qokaerxn.exception.AccountLockedException;
import com.qokaerxn.exception.AccountNotFoundException;
import com.qokaerxn.exception.PasswordErrorException;
import com.qokaerxn.mapper.EmployeeMapper;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        Employee employee = employeeMapper.getByUserName(username);

        if(employee == null){
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

         password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())){
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if(employee.getStatus() == StatusConstant.DISABLE){
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

       //复制employeeDTO中的属性到employee中
        BeanUtils.copyProperties(employeeDTO,employee);

        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {

        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> result = page.getResult();

        return new PageResult(total,result);

    }

    /**
     * 启用禁用员工
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * 通过员工ID查询员工
     * @param id
     * @return
     */
    @Override
    public Employee getEmployeeById(Long id) {
        Employee employee = employeeMapper.getEmployeeById(id);
        return employee;
    }

    /**
     * 编辑（更新）员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO,employee);

        //因为已经用公共的切面方法，所以不用单独的赋值了

        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

}
