package com.qokaerxn.service.Imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.qokaerxn.constant.MessageConstant;
import com.qokaerxn.dto.UserLoginDTO;
import com.qokaerxn.entity.User;
import com.qokaerxn.exception.LoginFailedException;
import com.qokaerxn.mapper.UserMapper;
import com.qokaerxn.properties.WeChatProperties;
import com.qokaerxn.service.UserService;
import com.qokaerxn.utils.HttpClientUtil;
import com.qokaerxn.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl  implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;


    public User wxLogIn(UserLoginDTO userLoginDTO) {
        log.info("userLoginDto:"+userLoginDTO.toString());
        String openid = getOpenId(userLoginDTO.getCode());
        log.info("openid:"+openid);
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getByOpenid(openid);

        System.out.println(user);
        //如果是新用户，那么就创建新用户加入数据库当中
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;

    }

    private String getOpenId(String code) {
        Map<String,String> map = new HashMap<>();
        //按要求包装请求数据
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        //Get请求
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        //将返回的字符串变成JSONObject
        JSONObject jsonObject = JSON.parseObject(json);

        //获取字符串openid的值
        String openid = jsonObject.getString("openid");

        log.info(openid);
        return openid;
    }
}
