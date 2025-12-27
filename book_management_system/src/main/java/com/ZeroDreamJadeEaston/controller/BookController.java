package com.ZeroDreamJadeEaston.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ZeroDreamJadeEaston.constant.Cache;
import com.ZeroDreamJadeEaston.domain.PageResult;
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.dto.Condition;
import com.ZeroDreamJadeEaston.domain.entity.Book;
import com.ZeroDreamJadeEaston.service.BookService;
import com.ZeroDreamJadeEaston.util.AiUtils;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
@Validated
public class BookController {
    private final BookService bookService;

    /**
     * AI 自动补全图书信息接口
     */
    @GetMapping("/ai")
    public Result<Book> aiAutoFill(@RequestParam String title) {
        log.info("正在请求AI查询书籍: {}", title);

        try {
            // 1. 调用工具类
            String jsonStr = AiUtils.getBookInfoFromAI(title);

            if (jsonStr == null || jsonStr.isEmpty()) {
                return Result.error("AI 暂时没找到这本书，请手动输入吧");
            }

            // 2. 将 AI 返回的 JSON 字符串转为 Book 对象
            Book book = JSON.parseObject(jsonStr, Book.class);

            // 3. 补充默认库存 (AI 不知道你有多少库存)
            book.setNumber(10);

            return Result.success(book);

        } catch (Exception e) {
            log.error("AI解析失败", e);
            return Result.error("AI 接口繁忙，请稍后再试");
        }
    }

    /**
     * 书籍信息的分页查询
     */
    @PostMapping
    @Cacheable(cacheNames = Cache.BOOK_PAGE, key = "#condition.hashCode()")
    public PageResult<List<Book>> getBookPage(@RequestBody Condition condition) {
        log.debug("查询条件:{}", condition);

        Page<Book> bookPage = bookService.getBookPage(condition);

        List<Book> data = bookPage.getRecords();
        Long total = bookPage.getTotal();

        return PageResult.success(data, total);
    }

    /**
     * 根据isbn删除书籍信息
     */
    @CacheEvict(value = Cache.BOOK_PAGE, allEntries = true)
    @DeleteMapping("/{isbn}")
    public Result<String> deleteBookByIsbn(@PathVariable @Pattern(regexp = "^\\S{1,20}$") String isbn) {
        log.debug("isbn:{}", isbn);

        // 直接返回 Service 层的处理结果（成功或失败信息）
        return bookService.deleteBookByIsbn(isbn);
    }

    /**
     * 添加图书
     */
    @PostMapping("/add")
    @CacheEvict(value = Cache.BOOK_PAGE, allEntries = true)
    public Result<String> addBook(@RequestBody Book book) {
        log.debug("book:{}", book);

        return bookService.add(book);
    }

    /**
     * 根据isbn更新图书信息
     */
    @PutMapping
    @CacheEvict(value = Cache.BOOK_PAGE, allEntries = true)
    public Result<String> updateBook(@RequestBody Book book) {
        log.debug("book:{}", book);

        return bookService.updateBook(book);
    }
}