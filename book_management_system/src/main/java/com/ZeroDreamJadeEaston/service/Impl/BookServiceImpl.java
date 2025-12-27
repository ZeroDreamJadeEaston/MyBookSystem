package com.ZeroDreamJadeEaston.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ZeroDreamJadeEaston.domain.Borrow;
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.dto.Condition;
import com.ZeroDreamJadeEaston.domain.entity.Book;
import com.ZeroDreamJadeEaston.mapper.BookMapper;
import com.ZeroDreamJadeEaston.mapper.BorrowMapper;
import com.ZeroDreamJadeEaston.service.BookService;
import com.ZeroDreamJadeEaston.util.MyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BorrowMapper borrowMapper;

    @Override
    public Page<Book> getBookPage(Condition condition) {
        String isbn = condition.getIsbn();
        String bookName = condition.getBookName();
        String author = condition.getAuthor();
        Integer currentPage = condition.getCurrentPage();
        Integer pageSize = condition.getPageSize();
        String sortField = condition.getSort();
        String sortOrder = condition.getOrder();

        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(MyUtils.StrUtil(isbn), Book::getIsbn, isbn)
                .like(MyUtils.StrUtil(bookName), Book::getTitle, bookName)
                .like(MyUtils.StrUtil(author), Book::getAuthor, author);

        boolean isAsc = "ascending".equals(sortOrder);
        if ("entryDate".equals(sortField)) {
            queryWrapper.orderBy(true, isAsc, Book::getEntryDate);
        } else if ("number".equals(sortField)) {
            queryWrapper.orderBy(true, isAsc, Book::getNumber);
        } else if ("isbn".equals(sortField)) {
            queryWrapper.orderBy(true, isAsc, Book::getIsbn);
        } else {
            queryWrapper.orderByDesc(Book::getEntryDate);
        }

        return bookMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
    }

    @Override
    public Result<String> deleteBookByIsbn(String isbn) {
        LambdaQueryWrapper<Borrow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Borrow::getIsbn, isbn).isNull(Borrow::getReturnDate);
        Long count = borrowMapper.selectCount(queryWrapper);

        if (count > 0) {
            return Result.error("删除失败：该书已被借出且尚未归还！");
        }
        bookMapper.deleteById(isbn);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> add(Book book) {
        String isbn = book.getIsbn();
        String title = book.getTitle();
        Integer number = book.getNumber();


        // 1. 基础校验
        if (!MyUtils.StrUtil(isbn)) {
            return Result.error("添加失败：ISBN不能为空！");
        }
        if (!MyUtils.StrUtil(title)) {
            return Result.error("添加失败：书名不能为空！");
        }
        if (number == null || number < 0) {
            return Result.error("添加失败：库存量不能为空且必须大于等于0！");
        }

        // 2. 查重校验
        Long count = bookMapper.getByIsbn(isbn);
        if (count > 0) {
            return Result.error("库中已有此书，无法添加！");
        }

        // 3. 正常业务
        if (book.getEntryDate() == null) {
            book.setEntryDate(new Date());
        }

        bookMapper.insert(book);
        return Result.success();
    }

    @Override
    public Result<String> updateBook(Book book) {
        String isbn = book.getIsbn();
        Book b = bookMapper.selectById(isbn);
        // return Result.error
        if (b == null) {
            return Result.error("修改失败：找不到该ISBN对应的书籍");
        }
        bookMapper.updateById(book);
        return Result.success();
    }
}