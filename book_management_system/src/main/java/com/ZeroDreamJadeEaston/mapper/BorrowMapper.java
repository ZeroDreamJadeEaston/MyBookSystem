package com.ZeroDreamJadeEaston.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ZeroDreamJadeEaston.domain.Borrow;
import com.ZeroDreamJadeEaston.domain.vo.BorrowVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.Date;
import java.util.List;

@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {

    // 原有的查询：普通读者只查自己的（查视图 borrowvo）
    @Select("SELECT * FROM borrowvo WHERE reader_id = #{readerId}")
    List<BorrowVo> selectByReaderId(Integer readerId);

    // 查询所有借阅记录（管理员用）
    // 关联查询：borrow + book + reader
    @Select("SELECT " +
            "b.id, " +
            "b.isbn, " +
            "bk.title as bookName, " +       // 书名
            "b.reader_id as readerId, " +
            "r.username as username, " +     // 借阅人账号
            "r.nickname as nickname, " +     // 借阅人昵称
            "b.borrow_date as borrowDate, " +
            "b.due_date as dueDate, " +
            "b.return_date as returnDate, " +
            "CASE WHEN b.return_date IS NULL THEN 0 ELSE 1 END as status " + // 计算状态：没还书日期就是未还(0/false)
            "FROM borrow b " +
            "LEFT JOIN book bk ON b.isbn = bk.isbn " +        // 关联图书表
            "LEFT JOIN reader r ON b.reader_id = r.id " +     // 关联读者表
            "ORDER BY b.borrow_date DESC")
    List<BorrowVo> selectAllBorrow();

    // 更新还书日期
    void updateReturnDateById(Integer id, Date now);
}