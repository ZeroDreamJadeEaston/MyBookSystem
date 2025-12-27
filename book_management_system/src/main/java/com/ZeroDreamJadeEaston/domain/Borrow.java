package com.ZeroDreamJadeEaston.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;

/**
 * 借阅记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Borrow implements Serializable {

    // 设置 ID 类型为数据库自增 (AUTO)
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String isbn;

    @TableField("reader_id")
    private String readerId;

    private Date borrowDate;
    private Date returnDate;
    private Date dueDate;
}