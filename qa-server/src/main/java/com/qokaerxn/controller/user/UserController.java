package com.qokaerxn.controller.user;

import com.qokaerxn.constant.JwtClaimsConstant;
import com.qokaerxn.dto.UserLoginDTO;
import com.qokaerxn.entity.User;
import com.qokaerxn.properties.JwtProperties;
import com.qokaerxn.result.Result;
import com.qokaerxn.service.OrderService;
import com.qokaerxn.service.UserService;
import com.qokaerxn.utils.JwtUtil;
import com.qokaerxn.vo.UserLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private OrderService orderService;

    @PostMapping("/login")
    @ApiOperation("用户登录接口")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        //用户微信登录
        User user = userService.wxLogIn(userLoginDTO);

        //给用户生成jwt
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);

    }

}
