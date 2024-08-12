package com.qokaerxn.controller.admin;

import com.qokaerxn.constant.JwtClaimsConstant;
import com.qokaerxn.dto.EmployeeDTO;
import com.qokaerxn.dto.EmployeeLoginDTO;
import com.qokaerxn.dto.EmployeePageQueryDTO;
import com.qokaerxn.entity.Employee;
import com.qokaerxn.properties.JwtProperties;
import com.qokaerxn.result.PageResult;
import com.qokaerxn.result.Result;
import com.qokaerxn.service.EmployeeService;
import com.qokaerxn.utils.JwtUtil;
import com.qokaerxn.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        employeeService.save(employeeDTO);
        return  Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("员工查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        PageResult pageResult  = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result status(@PathVariable Integer status,Long id){
        employeeService.startOrStop(status,id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询员工信息")
    public Result<Employee> getEmployeeById(@PathVariable Long id){
        Employee employee = employeeService.getEmployeeById(id);
        employee.setPassword("****");
        return Result.success(employee);
    }

    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result editEmployeeInfo(@RequestBody EmployeeDTO employeeDTO){
        employeeService.update(employeeDTO);
        return Result.success();
    }

}
