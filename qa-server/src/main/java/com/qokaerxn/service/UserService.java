package com.qokaerxn.service;

import com.qokaerxn.dto.UserLoginDTO;
import com.qokaerxn.entity.User;
import com.qokaerxn.vo.UserLoginVO;

public interface UserService {
    User wxLogIn(UserLoginDTO userLoginDTO);
}
