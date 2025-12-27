package com.ZeroDreamJadeEaston.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.dto.Condition;
import com.ZeroDreamJadeEaston.domain.entity.Book;

public interface BookService {

    Page<Book> getBookPage(Condition condition);

    Result<String> deleteBookByIsbn(String isbn);

    Result<String> add(Book book);

    Result<String> updateBook(Book book);



}