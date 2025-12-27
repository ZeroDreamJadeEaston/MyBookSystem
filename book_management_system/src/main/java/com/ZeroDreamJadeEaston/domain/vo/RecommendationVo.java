package com.ZeroDreamJadeEaston.domain.vo;

import com.ZeroDreamJadeEaston.domain.entity.Recommendation;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RecommendationVo extends Recommendation {
    // 关联查询出来的书籍信息
    private String bookTitle;
    private String bookCover;
    private String bookAuthor;
    private String bookIntro; // 简介
}