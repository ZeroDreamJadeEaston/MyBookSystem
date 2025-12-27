package com.ZeroDreamJadeEaston.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date; // 引入 Date

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book implements Serializable {
    @TableId
    private String isbn;
    private String title;
    private String cover;
    private String introduction;
    private Integer number;
    private String author;

    // 入库日期
    // 对应数据库字段 entry_date (MyBatisPlus会自动驼峰映射)
    private Date entryDate;
}