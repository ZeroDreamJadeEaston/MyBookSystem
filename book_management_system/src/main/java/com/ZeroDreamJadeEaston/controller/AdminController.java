package com.ZeroDreamJadeEaston.controller;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.entity.Admin;
import com.ZeroDreamJadeEaston.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping; // 🔥 记得导入这个
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;


    /**
     * 更新管理员信息
     *
     * @param admin 管理员对象
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Admin admin) {
        log.info("admin:{}", admin);

        return adminService.updateById(admin);
    }

    /**
     * 注销（删除）管理员
     * @param id 管理员ID
     */
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam Integer id) {
        log.info("正在注销管理员 ID: {}", id);
        return adminService.delete(id);
    }

}
