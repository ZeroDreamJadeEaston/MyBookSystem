package com.ZeroDreamJadeEaston.service;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.dto.LoginDto;
import com.ZeroDreamJadeEaston.domain.entity.Admin;
import com.ZeroDreamJadeEaston.domain.vo.AdminVo;

public interface AdminService {

    Result<AdminVo> login(LoginDto admin);

    Result<String> register(Admin admin);

    Result<String> updateById(Admin admin);

    Result<String> delete(Integer id);
}
