package com.ZeroDreamJadeEaston.service;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.dto.LoginDto;
import com.ZeroDreamJadeEaston.domain.entity.Reader;
import com.ZeroDreamJadeEaston.domain.vo.ReaderVo;

import java.util.List;

public interface ReaderService {

    Result<ReaderVo>login(LoginDto reader);

    Result<Reader> updateReader(Reader reader);

    Result<String> register(Reader reader);

    Result<List<Reader>> getAllReader(Reader condition);

    Result<String> deleteById(Integer id);
}
