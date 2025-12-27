package com.ZeroDreamJadeEaston.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ZeroDreamJadeEaston.constant.Common;
import com.ZeroDreamJadeEaston.constant.Excep;
import com.ZeroDreamJadeEaston.constant.Jwt;
import com.ZeroDreamJadeEaston.domain.Borrow; // 🔥 必须导入这个
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.dto.LoginDto;
import com.ZeroDreamJadeEaston.domain.entity.Reader;
import com.ZeroDreamJadeEaston.domain.vo.ReaderVo;
import com.ZeroDreamJadeEaston.exception.AlreadyExistException;
import com.ZeroDreamJadeEaston.exception.BaseException;
import com.ZeroDreamJadeEaston.mapper.BorrowMapper; // 🔥 必须导入这个
import com.ZeroDreamJadeEaston.mapper.ReaderMapper;
import com.ZeroDreamJadeEaston.service.ReaderService;
import com.ZeroDreamJadeEaston.util.JwtUtils;
import com.ZeroDreamJadeEaston.util.MyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReaderServiceImpl implements ReaderService {

    private final ReaderMapper readerMapper;
    private final BorrowMapper borrowMapper; // 注入 BorrowMapper 用于查询借阅记录
    private final StringRedisTemplate redisTemplate;

    @Override
    public Result<ReaderVo> login(LoginDto reader) {
        Reader r = readerMapper.selectByName(reader.getUsername());
        //用户不存在
        if (r == null) {
            return Result.error(Excep.USER_NOT_EXIST);
        }
        String pwd = r.getPassword();
        //密码错误
        if (!pwd.equals(reader.getPassword())) {
            return Result.error(Excep.WRONG_PASSWORD);
        }

        //生成令牌,在有效载荷中存储用户名和id
        Map<String, Object> claims = new HashMap<>();
        claims.put(Common.ID, r.getId());
        claims.put(Common.USERNAME, reader.getUsername());
        String token = JwtUtils.generateJwt(claims);

        // 将令牌保存到redis中
        redisTemplate.opsForValue().set(token, token, Jwt.EXPIRE_TIME);

        // 封装信息并返回
        ReaderVo readerVo = new ReaderVo();
        readerVo.setToken(token);
        BeanUtils.copyProperties(r, readerVo);

        return Result.success(readerVo);
    }

    @Override
    public Result<Reader> updateReader(Reader reader) {
        readerMapper.updateById(reader);
        return Result.success();
    }


    @Override
    public Result<String> register(Reader reader) {
        String username = reader.getUsername();
        String tel = reader.getTel();

        // 用户名，密码，电话都不能空
        if (!MyUtils.StrUtil(username) || !MyUtils.StrUtil(reader.getPassword()) || !MyUtils.StrUtil(tel)) {
            throw new BaseException(Excep.REGISTER_ERROR);
        }

        // 查询用户名是否存在
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reader::getUsername, username);
        Long l = readerMapper.selectCount(wrapper);
        if (l != 0) {
            throw new AlreadyExistException(Excep.USER_ALREADY_EXIST);
        }

        //查询电话是否存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reader::getTel, tel);
        l = readerMapper.selectCount(wrapper);
        if (l != 0) {
            throw new AlreadyExistException(Excep.TEL_ALREADY_EXIST);
        }

        readerMapper.register(reader);
        return Result.success();
    }

    @Override
    public Result<List<Reader>> getAllReader(Reader condition) {
        LambdaQueryWrapper<Reader> wrapper = new LambdaQueryWrapper<>();
        String username = condition.getUsername();
        String nickname = condition.getNickname();
        String tel = condition.getTel();

        wrapper
                .like(MyUtils.StrUtil(username), Reader::getUsername, username)
                .like(MyUtils.StrUtil(nickname), Reader::getNickname, nickname)
                .like(MyUtils.StrUtil(tel), Reader::getTel, tel);

        List<Reader> readers = readerMapper.selectList(wrapper);

        return Result.success(readers);
    }

    @Override
    public Result<String> deleteById(Integer id) {
        // 1. 查询用户是否存在
        Reader reader = readerMapper.selectById(id);
        if (reader == null) {
            throw new BaseException(Excep.USER_NOT_EXIST);
        }

        //检查该用户是否有未归还的图书
        LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Borrow::getReaderId, id)        // 查这个人的记录
                .isNull(Borrow::getReturnDate);     // returnDate 为空说明还没还

        Long unReturnedCount = borrowMapper.selectCount(queryWrapper);

        if (unReturnedCount > 0) {
            // 如果有未还的书，直接报错并返回提示
            return Result.error("该用户仍有 " + unReturnedCount + " 本书未归还，无法删除！");
        }

        // 2. 如果都还了，才允许删除
        readerMapper.deleteById(id);

        return Result.success();
    }

}