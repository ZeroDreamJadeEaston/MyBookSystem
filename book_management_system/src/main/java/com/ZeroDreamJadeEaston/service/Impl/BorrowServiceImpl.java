package com.ZeroDreamJadeEaston.service.Impl;

import com.ZeroDreamJadeEaston.constant.Common;
import com.ZeroDreamJadeEaston.domain.Borrow;
import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.vo.BorrowVo;
import com.ZeroDreamJadeEaston.mapper.BookMapper;
import com.ZeroDreamJadeEaston.mapper.BorrowMapper;
import com.ZeroDreamJadeEaston.service.BorrowService;
import com.ZeroDreamJadeEaston.util.MyUtils;
import com.ZeroDreamJadeEaston.util.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {
    private final BorrowMapper borrowMapper;
    private final BookMapper bookMapper;

    /**
     * 根据用户id查询借书记录 (个人)
     */
    @Override
    public Result<List<BorrowVo>> getBorrowByReaderId() {
        Map<String, Object> reader = ThreadLocalUtil.get();
        Integer readerId = MyUtils.objToInt(reader.get(Common.ID));
        List<BorrowVo> result = borrowMapper.selectByReaderId(readerId);
        return Result.success(result);
    }

    /**
     * 查询所有借阅记录 (管理员)
     */
    @Override
    public Result<List<BorrowVo>> getAllBorrow() {
        List<BorrowVo> result = borrowMapper.selectAllBorrow();
        return Result.success(result);
    }

    /**
     * 借书逻辑 (解决并发超卖 Bug)
     */
    @Transactional // 开启事务
    @Override
    public Result<String> borrow(String isbn, Date dueDate) {
        // 1. 获取当前登录的读者 ID
        Map<String, Object> reader = ThreadLocalUtil.get();

        String readerId = String.valueOf(reader.get(Common.ID));

        // 2. 原子性扣减库存
        int rows = bookMapper.decreaseStock(isbn);

        if (rows <= 0) {
            // 如果返回 0，说明库存不足 (number <= 0) 或者 ISBN 错误，扣减失败
            return Result.error("手慢了！该书已被借光（库存不足）");
        }

        // 3. 库存扣减成功后，生成借阅记录
        Borrow borrow = Borrow.builder()
                .isbn(isbn)
                .dueDate(dueDate)
                .borrowDate(new java.sql.Date(System.currentTimeMillis())) // 使用当前时间
                .readerId(readerId)
                .build();

        borrowMapper.insert(borrow);

        return Result.success("借阅成功");
    }

    /**
     * 还书逻辑
     */
    @Transactional
    @Override
    public Result<String> returnBook(Integer id, String isbn) {
        // 1. 更新借阅记录的归还时间
        borrowMapper.updateReturnDateById(id, MyUtils.now());

        // 2. 还书时调用 increaseStock 恢复库存 (原子性 +1)
        bookMapper.increaseStock(isbn);

        return Result.success();
    }

    @Override
    public Result<String> deleteById(Integer id) {
        borrowMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<String> deleteBatchByIds(List<Integer> ids) {
        borrowMapper.deleteBatchIds(ids);
        return Result.success();
    }

    @Override
    public Result<String> updateDueDate(Integer id, String newDateStr) {
        if (id == null || newDateStr == null) {
            return Result.error("参数不能为空");
        }
        try {
            java.sql.Date newDate = java.sql.Date.valueOf(newDateStr);
            Borrow borrow = new Borrow();
            borrow.setId(id);
            borrow.setDueDate(newDate);
            int rows = borrowMapper.updateById(borrow);
            if (rows > 0) {
                return Result.success("修改成功");
            } else {
                return Result.error("修改失败，未找到该记录");
            }
        } catch (Exception e) {
            return Result.error("日期格式错误或系统异常");
        }
    }
}