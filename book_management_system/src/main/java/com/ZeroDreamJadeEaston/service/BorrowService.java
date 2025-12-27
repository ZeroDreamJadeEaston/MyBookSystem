package com.ZeroDreamJadeEaston.service;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.vo.BorrowVo;

import java.sql.Date;
import java.util.List;

public interface BorrowService {

    // 查询个人借阅
    Result<List<BorrowVo>> getBorrowByReaderId();

    // 管理员查询所有借阅
    Result<List<BorrowVo>> getAllBorrow();

    // 原有的其他方法
    Result<String> borrow(String isbn, Date dueDate);

    Result<String> returnBook(Integer id, String isbn);

    Result<String> deleteById(Integer id);

    Result<String> deleteBatchByIds(List<Integer> ids);

    // 修改应还日期
    Result<String> updateDueDate(Integer id, String newDate);

}