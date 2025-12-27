package com.ZeroDreamJadeEaston;

import com.ZeroDreamJadeEaston.constant.Common;
import com.ZeroDreamJadeEaston.service.BorrowService;
import com.ZeroDreamJadeEaston.util.ThreadLocalUtil; // 导入这个
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class BorrowTest {

    @Autowired
    private BorrowService borrowService;

    // 在测试开始前，模拟用户登录
    @BeforeEach
    public void setUp() {
        Map<String, Object> map = new HashMap<>();
        map.put(Common.ID, 1);       // 假设当前用户ID是 1
        map.put(Common.USERNAME, "admin"); // 假设用户名
        // 手动塞入 ThreadLocal，假装已经登录了
        ThreadLocalUtil.set(map);
    }

    // 测试结束后清理，防止影响其他测试
    @AfterEach
    public void tearDown() {
        ThreadLocalUtil.remove();
    }

    @Test
    public void testBorrowService() {

    }

    @Test
    public void testBorrowView() {
        // ...
    }
}