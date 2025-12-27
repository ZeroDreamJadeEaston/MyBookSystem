package com.ZeroDreamJadeEaston.controller;

import com.ZeroDreamJadeEaston.constant.Cache;
import com.ZeroDreamJadeEaston.constant.Excep;
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.vo.BorrowVo; // ⚠️ 必须要有这个 import
import com.ZeroDreamJadeEaston.exception.BaseException;
import com.ZeroDreamJadeEaston.service.BorrowService;
import com.ZeroDreamJadeEaston.util.MyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.sql.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * 查询用户借阅记录 (普通读者只能看自己的)
     */
    @GetMapping
    public Result<List<BorrowVo>> getBorrowByReaderId() {
        return borrowService.getBorrowByReaderId();
    }

    /**
     * 查询所有借阅记录 (管理员)
     * 对应前端的 getAllBorrowService -> /borrow/all
     */
    @GetMapping("/all")
    public Result<List<BorrowVo>> getAllBorrow() {
        return borrowService.getAllBorrow();
    }

    /**
     * 用户借阅图书
     */
    @GetMapping("/borrowBook")
    @CacheEvict(value = Cache.BOOK_PAGE, allEntries = true)
    public Result<String> borrow(String isbn, String dueDate) {
        log.info("isbn:{} dueDate:{}", isbn, dueDate);

        if (!MyUtils.StrUtil(dueDate)) {
            throw new BaseException(Excep.RETURN_DATE_IS_NULL);
        }

        Date date = MyUtils.StrToDate(dueDate);
        return borrowService.borrow(isbn, date);
    }

    /**
     * 归还书籍
     */
    @GetMapping("/returnBook")
    @CacheEvict(value = Cache.BOOK_PAGE, allEntries = true)
    public Result<String> returnBook(Integer id, String isbn) {
        log.info("returnBook id:{}", id);
        return borrowService.returnBook(id, isbn);
    }

    /**
     * 根据借阅号删除借阅记录
     */
    @DeleteMapping
    public Result<String> deleteBorrow(Integer id) {
        log.info("deleteBorrow id:{}", id);
        return borrowService.deleteById(id);
    }

    /**
     * 批量删除借阅记录
     */
    @PostMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Integer> ids) {
        log.info("ids:{}", ids);
        return borrowService.deleteBatchByIds(ids);
    }


    @PostMapping("/updateDueDate")
    public Result<String> updateDueDate(@RequestBody Map<String, Object> map) {
        // 使用 Map 接收前端传来的 JSON 参数
        Integer id = (Integer) map.get("id");
        String newDate = (String) map.get("newDate");
        return borrowService.updateDueDate(id, newDate);
    }
}