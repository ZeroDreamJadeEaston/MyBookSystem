package com.ZeroDreamJadeEaston.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class Recommendation implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String isbn;
    private String reason;
    private Date createTime;
}