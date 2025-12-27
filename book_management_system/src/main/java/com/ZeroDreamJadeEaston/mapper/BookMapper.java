package com.ZeroDreamJadeEaston.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ZeroDreamJadeEaston.domain.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BookMapper extends BaseMapper<Book> {


    @Select("SELECT title FROM book WHERE ISBN = #{isbn}")
    String getTitleByIsbn(String isbn);


    @Update("UPDATE book SET number = number + #{num} WHERE ISBN = #{isbn}")
    void updateNumberByIsbn(@Param("isbn") String isbn, @Param("num") Integer num);

    @Select("SELECT COUNT(*) FROM book WHERE ISBN = #{isbn}")
    Long getByIsbn(String isbn);



    /**
     * 原子性扣减库存
     * 只有当 number > 0 时才会执行减 1，利用数据库行锁解决并发超卖问题
     * 返回值 int 代表受影响的行数：1=成功，0=失败(库存不足)
     */
    @Update("UPDATE book SET number = number - 1 WHERE isbn = #{isbn} AND number > 0")
    int decreaseStock(@Param("isbn") String isbn);

    /**
     * 还书时增加库存
     */
    @Update("UPDATE book SET number = number + 1 WHERE isbn = #{isbn}")
    void increaseStock(@Param("isbn") String isbn);
}