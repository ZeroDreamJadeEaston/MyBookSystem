package com.ZeroDreamJadeEaston.domain.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class Condition implements Serializable {
    private String bookName;
    private String author;
    private String isbn;
    private Integer number;
    private Integer currentPage;
    private Integer pageSize;

    // 接收排序参数
    private String sort;  // 排序字段 ("entryDate", "number")
    private String order; // 排序方式 ("ascending" 或 "descending")
}