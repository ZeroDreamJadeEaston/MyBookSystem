package com.ZeroDreamJadeEaston.controller;

import com.ZeroDreamJadeEaston.constant.Cache;
import com.ZeroDreamJadeEaston.constant.Common; // 🔥 新增
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.entity.Reader;
import com.ZeroDreamJadeEaston.service.ReaderService;
import com.ZeroDreamJadeEaston.util.MyUtils; // 🔥 新增
import com.ZeroDreamJadeEaston.util.ThreadLocalUtil; // 🔥 新增
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // 🔥 新增

@Validated
@Slf4j
@RestController
@RequestMapping("/reader")
public class ReaderController {
    private final ReaderService readerService;

    public ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    /**
     * 查询所有用户信息
     */
    @PostMapping
    @Cacheable(cacheNames = Cache.READER, key = "#condition")
    public Result<List<Reader>> getAllReader(@RequestBody Reader condition) {
        log.debug("condition:{}", condition);
        return readerService.getAllReader(condition);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    @CacheEvict(value = Cache.READER, allEntries = true)
    public Result<Reader> updateReader(@RequestBody @Validated Reader reader) {
        log.info("reader:{}", reader);
        return readerService.updateReader(reader);
    }

    /**
     * 用户注销自己的账号
     * 逻辑：从 Token 拿自己的 ID -> 调用删除逻辑
     */
    @DeleteMapping("/my")
    @CacheEvict(value = Cache.READER, allEntries = true)
    public Result<String> deleteMyAccount() {
        // 1. 获取当前登录用户的 ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer myId = MyUtils.objToInt(map.get(Common.ID));

        log.info("用户申请注销账号 id:{}", myId);

        // 2. 复用 Service 层的 deleteById (还书检查)
        return readerService.deleteById(myId);
    }

    /**
     * 根据id删除用户 (管理员用)
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = Cache.READER, allEntries = true)
    public Result<String> deleteById(@PathVariable Integer id) {
        log.debug("id:{}", id);
        return readerService.deleteById(id);
    }

}